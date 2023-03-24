package com.jehubasa.adassubmissionlog.fragments

import android.Manifest
import android.os.Bundle
import android.os.Parcelable
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
import com.jehubasa.adassubmissionlog.dialog.QrScanDialogFragment
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class QrScanFragment : Fragment(), QrScanDialogFragment.OnDialogExitListener {

    private lateinit var binding: FragmentQrScanBinding
    private lateinit var imageAnalysis: ImageAnalysis
    private lateinit var camera: Camera
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var cameraPermissionLauncher: ActivityResultLauncher<String>
    private var qrRead: List<String> = listOf()
    private lateinit var cameraProvider: ProcessCameraProvider
    private lateinit var dataBaseHelper: QrGenDataBaseHelper
    private var scannedData: SubmissionDataClass? = null
    private var tempLrType:String = ""

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
            resetViews()
        }

        binding.qrScanSave.setOnClickListener {
            scannedData?.let {
                getCheckedTypeOfLr()

                if ((it.tos == binding.qrScanTimesSubmitted.text.toString().toInt()) && (
                            it.typ == tempLrType
                            )
                ) {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Attention")
                        .setMessage("Base on your Time of submission and Liquidation type, " +
                                "I detected similar data for ${it.Sch}. By tapping 'Proceed' " +
                                "will update the current data")
                        .setPositiveButton("Proceed"){_,_->
                            updateData(it.id)
                        }
                        .setNegativeButton("Go back"){d,_ ->
                            d.dismiss()
                        }
                        .show()
                } else {
                    saveData()
                }
            }
        }

        binding.tilQrScanDateSubmission.setEndIconOnClickListener {
            openCalendarPicker("Submission date", binding.qrScanDateSubmission)
        }


        binding.tilQrScanDateRelease.setEndIconOnClickListener {
            openCalendarPicker("Released date", binding.qrScanDateRelease)

        }

        binding.tilQrScanSubmittedDivisionDate.setEndIconOnClickListener {
            openCalendarPicker("Division submission date", binding.qrScanSubmittedDivisionDate)
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

    private fun resetViews() {
        binding.formSection.visibility = View.INVISIBLE
        binding.rgTypeOfLr.clearCheck()
        binding.qrScanDateSubmission.setText("")
        binding.qrScanDateRelease.setText("")
        binding.qrScanSubmittedBy.setText("")
        binding.qrScanReleasedTo.setText("")
        binding.qrScanReleasedToSamePersonCheckbox.isChecked = false
        binding.qrScanSubmittedDivCheckBox.isChecked = false
        binding.qrScanSubmittedDivisionDate.setText("")
        binding.qrScanTimesSubmitted.setText("")
    }

    private fun updateData(id: Int?) {
        SubmissionDataClass(
            null,
            binding.qrScanSchoolName.text.toString(),
            scannedData?.typ,
            binding.qrScanDateSubmission.text.toString(),
            binding.qrScanDateRelease.text.toString(),
            binding.qrScanTimesSubmitted.text.toString().toInt(),
            binding.qrScanSubmittedBy.text.toString(),
            binding.qrScanReleasedTo.text.toString(),
            chkIfsubmittedToDiv(),
            binding.qrScanSubmittedDivisionDate.text.toString()
        ).also {
            dataBaseHelper.updateDataAtTable2(dataBaseHelper.writableDatabase, id, it)
                .also { helper ->
                    if (helper!! > 0) {
                        Toast.makeText(requireContext(), "Update Successful", Toast.LENGTH_LONG)
                            .show()
                        Log.d("ASP", "Update code $helper. data: $it")
                    } else {
                        Toast.makeText(requireContext(), "Update failed", Toast.LENGTH_LONG).show()
                        Log.d("ASP", "Update code $helper. data: $it")
                    }
                }
        }
    }

    private fun getCheckedTypeOfLr() {
        when (binding.rgTypeOfLr.checkedRadioButtonId) {
            R.id.qr_scan_1q -> {
                tempLrType = resources.getStringArray(R.array.lr_types)[0]
            }
            R.id.qr_scan_2q -> {
                tempLrType = resources.getStringArray(R.array.lr_types)[1]
            }
            R.id.qr_scan_3q -> {
                tempLrType = resources.getStringArray(R.array.lr_types)[2]
            }
            R.id.qr_scan_4q -> {
                tempLrType = resources.getStringArray(R.array.lr_types)[3]
            }
            R.id.qr_scan_add -> {
                tempLrType = resources.getStringArray(R.array.lr_types)[4]
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

            if (fill == "Submission date" && it > (dateFormat
                    .parse(binding.qrScanDateRelease.text.toString())?.time ?: 0)
            ) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Error!")
                    .setMessage("Submission date should on or before the release date")
                    .setPositiveButton("try again") { _, _ ->
                        openCalendarPicker("Submission date", et)
                    }
                    .setNegativeButton("Decline") { _, _ ->
                        et.setText("")
                    }.show()
            }

            if (fill == "Division submission date" && it < (dateFormat
                    .parse(binding.qrScanDateRelease.text.toString())?.time ?: 0)
            ) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Error!")
                    .setMessage("Submission date should on or before the release date")
                    .setPositiveButton("try again") { _, _ ->
                        openCalendarPicker("Division submission date", et)
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
                null,
                qrRead[0],
                tempLrType,
                binding.qrScanDateSubmission.text.toString(),
                binding.qrScanDateRelease.text.toString(),
                binding.qrScanTimesSubmitted.text.toString().toInt(),
                binding.qrScanSubmittedBy.text.toString(),
                binding.qrScanReleasedTo.text.toString(),
                chkIfsubmittedToDiv(),
                binding.qrScanSubmittedDivisionDate.text.toString()
            )
        )
        if (dataBaseHelper.insertDateAtTable2(db, data) >= 0) {
            Toast.makeText(requireContext(), "saved", Toast.LENGTH_LONG).show()
        } else {
            Log.e("ASP", "error on saving from scan page")
        }
    }

    private fun chkIfsubmittedToDiv(): String {
        if (binding.qrScanSubmittedDivCheckBox.isChecked) {
            return "true"
        } else {
            return "false"
        }
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
        val oldData = dataBaseHelper.queryDataAtTable2(
            dataBaseHelper.readableDatabase,
            arrayOf(qrRead[0])
        )

        val bundle = Bundle().apply {
            putParcelableArrayList(
                getString(R.string.old_data),
                oldData as ArrayList<out Parcelable>
            )
        }

        val dialog = QrScanDialogFragment().apply {
            arguments = bundle
        }
        dialog.show(parentFragmentManager, "Showing Records")
        dialog.setOnDialogExitListener(this)

        binding.formSection.visibility = View.VISIBLE
        binding.qrScanSchoolName.text = qrRead[0]
        binding.qrScanHead.text = qrRead[1]

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

    override fun exitListener(data: SubmissionDataClass) {
        scannedData = data
        binding.qrScanDateSubmission.setText(data.ds)

        val typeLR = resources.getStringArray(R.array.lr_types)
        when (data.typ) {
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

        binding.qrScanDateRelease.setText(data.dr)
        binding.qrScanSubmittedBy.setText(data.sb)

        if (data.sb == data.rt) {
            binding.qrScanReleasedToSamePersonCheckbox.isChecked = true
        }
        binding.qrScanReleasedTo.setText(data.rt)
        binding.qrScanTimesSubmitted.setText(data.tos.toString())

        if (data.sd == "true") {
            binding.qrScanSubmittedDivCheckBox.isChecked = true
            binding.qrScanSubmittedDivisionDate.visibility = View.VISIBLE
            binding.qrScanSubmittedDivisionDate.setText(data.tsd)
        }

    }
}