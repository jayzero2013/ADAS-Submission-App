package com.jehubasa.adassubmissionlog.fragments

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.jehubasa.adassubmissionlog.QRCodeAnalyzer
import com.jehubasa.adassubmissionlog.QrGenDataBaseHelper
import com.jehubasa.adassubmissionlog.R
import com.jehubasa.adassubmissionlog.data.SubmissionDataClass
import com.jehubasa.adassubmissionlog.databinding.FragmentQrScanBinding
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class QrScanFragment : Fragment() {

    private lateinit var binding: FragmentQrScanBinding
    private lateinit var imageAnalysis: ImageAnalysis
    private lateinit var camera: Camera
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var cameraPermissionLauncher: ActivityResultLauncher<String>
    private var qrRead: List<String> = listOf()
    private lateinit var cameraProvider: ProcessCameraProvider
    private lateinit var dataBaseHelper: QrGenDataBaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        cameraPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    // Permission granted, start the camera
                    startCamera()
                } else {
                    // Permission denied, show a message to the user
                    Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentQrScanBinding.inflate(inflater, container, false)
        requestCameraPermission()

        dataBaseHelper = QrGenDataBaseHelper(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.qrScanReopen.setOnClickListener {
            startCamera()
            binding.formSection.visibility = View.INVISIBLE
        }

        binding.qrScanSave.setOnClickListener {
            saveData()
        }

        binding.tilQrScanDateSubmission.setEndIconOnClickListener {
            openCalendarPicker("Submission date", binding.qrScanDateSubmission)
        }


        binding.tilQrScanDateRelease.setEndIconOnClickListener {
            openCalendarPicker("Released date", binding.qrScanDateRelease)

        }

        binding.qrScanSubmittedDivCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.tilQrScanSubmittedDivisionDate.visibility = View.VISIBLE
            } else {
                binding.tilQrScanSubmittedDivisionDate.visibility = View.INVISIBLE
            }
        }

        binding.qrScanReleasedToSamePersonCheckbox.setOnCheckedChangeListener { _, c ->
            if (c) {
                binding.qrScanReleasedTo.setText(binding.qrScanSubmittedBy.text.toString())
            } else {
                binding.qrScanReleasedTo.setText("")
            }
        }
    }

    private fun openCalendarPicker(fill: String, et: TextInputEditText) {

        val datePicker =
            MaterialDatePicker.Builder.datePicker().setTitleText("Select date for '$fill'")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build()
        datePicker.show(parentFragmentManager, "ASP")
        datePicker.addOnPositiveButtonClickListener {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            if (fill == "Released date" && it < (dateFormat
                    .parse(binding.qrScanDateSubmission.text.toString())?.time ?: 0)
            ) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Error!")
                    .setMessage("Released date should on or after the submission date")
                    .setPositiveButton("try again") { _, _ ->
                        openCalendarPicker("Released date", et)
                    }
                    .setNegativeButton("Decline") { _, _ ->
                        et.setText("")
                    }.show()
            }

            val date = dateFormat.format(Date(it))
            et.setText(date)
        }

    }

    private fun saveData() {

        val db = dataBaseHelper.readableDatabase
        val data = arrayOf(
            SubmissionDataClass(
                qrRead[0],
                qrRead[2],
                binding.qrScanDateSubmission.text.toString(),
                binding.qrScanDateRelease.text.toString(),
                binding.qrScanTimesSubmitted.text.toString().toInt(),
                binding.qrScanSubmittedBy.text.toString(),
                binding.qrScanReleasedTo.text.toString(),
                binding.qrScanSubmittedDivCheckBox.isChecked,
                binding.qrScanSubmittedDivisionDate.text.toString()
            )
        )
        if (dataBaseHelper.insertDateAtTable2(db, data) >= 0) {
            Toast.makeText(requireContext(), "saved", Toast.LENGTH_LONG).show()
        } else {
            Log.e("ASP", "error on saving from scan page")
        }
    }

    override fun onResume() {
        super.onResume()
    }

    private fun startCamera() {
        cameraExecutor = Executors.newSingleThreadExecutor()
        imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .apply {
                setAnalyzer(cameraExecutor, QRCodeAnalyzer { qrCode ->
                    // QR code scanned successfully, do something with the result
                    qrRead = qrCode[0].split("/")
                    Log.d("ASP", qrRead.toString())
                    stopCamera()
                    qrDataProcessing()
                })
            }

        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }
            val cameraSelector =
                CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()
            camera = cameraProvider.bindToLifecycle(
                viewLifecycleOwner,
                cameraSelector,
                preview,
                imageAnalysis
            )
        }, ContextCompat.getMainExecutor(requireContext()))

        binding.qrScanReopen.visibility = View.GONE
        binding.qrScanTint.visibility = View.GONE
    }

    private fun qrDataProcessing() {
        binding.formSection.visibility = View.VISIBLE
        binding.qrScanSchoolName.text = qrRead[0]
        binding.qrScanHead.text = qrRead[1]
        binding.qrScanDateSubmission.setText(qrRead[3])

        val typeLR = resources.getStringArray(R.array.lr_types)
        when (qrRead[2]) {
            typeLR[0] -> {
                binding.qrScan1q.isChecked = true
            }
            typeLR[1] -> {
                binding.qrScan2q.isChecked = true
            }
            typeLR[2] -> {
                binding.qrScan3q.isChecked = true
            }
            typeLR[3] -> {
                binding.qrScan4q.isChecked = true
            }
            typeLR[4] -> {
                binding.qrScanAdd.isChecked = true
            }
        }
    }

    private fun stopCamera() {
        cameraProvider.unbindAll()
        binding.qrScanReopen.visibility = View.VISIBLE
        binding.qrScanTint.visibility = View.VISIBLE
        cameraExecutor.shutdown()
    }

    private fun requestCameraPermission() {
        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

}