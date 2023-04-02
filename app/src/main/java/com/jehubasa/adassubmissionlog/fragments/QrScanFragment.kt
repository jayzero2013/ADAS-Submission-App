package com.jehubasa.adassubmissionlog.fragments

import android.Manifest
import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
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
import com.google.android.material.transition.MaterialSharedAxis
import com.jehubasa.adassubmissionlog.FirebaseDatabase
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
    private var tempLrType: String = ""
    private var oldData: List<SubmissionDataClass> = listOf()
    private var isQrdataprocessingLoaded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, /* forward = */ true).setDuration(500)
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, /* forward = */ false).setDuration(500)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, /* forward = */ true).setDuration(500)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, /* forward = */ false).setDuration(500)

        cameraPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    // Permission granted, start the camera
                    binding.qrScanOpenCamera.isEnabled = true
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
            checkOldData()
            if (oldData.isEmpty() && isDataIsFilled()) {
                saveData()
            }

            scannedData?.let {

                if (isDataIsFilled()) {

                    getCheckedTypeOfLr()

                    if ((it.tos == binding.qrScanTimesSubmitted.text.toString().toInt()) && (
                                it.typ == tempLrType
                                )
                    ) {
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Attention")
                            .setMessage(
                                "Base on your Time of submission and Liquidation type, " +
                                        "I detected similar data for ${it.sch}. By tapping 'Proceed' " +
                                        "will update the current data"
                            )
                            .setPositiveButton("Proceed") { _, _ ->
                                updateData(it.id)
                            }
                            .setNegativeButton("Go back") { d, _ ->
                                d.dismiss()
                            }
                            .show()
                    } else {
                        saveData()
                    }
                } else {
                    Toast.makeText(
                        requireContext(), "Please fill up required fields.",
                        Toast.LENGTH_LONG
                    ).show()
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
                binding.qrScanSubmittedDivisionDate.setText("")
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

        binding.rgTypeOfLr.setOnCheckedChangeListener { _, _ ->
            binding.qrScanDateSubmission.setText("")
            binding.qrScanDateRelease.setText("")
            binding.qrScanSubmittedBy.setText("")
            binding.qrScanReleasedTo.setText("")
            binding.qrScanReleasedToSamePersonCheckbox.isChecked = false
            binding.qrScanSubmittedDivCheckBox.isChecked = false
            binding.qrScanSubmittedDivisionDate.setText("")
            binding.qrScanTimesSubmitted.setText("")
        }

        binding.qrScanClearSubmission.setOnClickListener {
            binding.qrScanDateSubmission.setText("")
        }

        binding.qrScanClearRelease.setOnClickListener {
            binding.qrScanDateRelease.setText("")
        }

        binding.qrScanOpenCamera.setOnClickListener {
            startCamera()
            binding.qrScanOpenCamera.visibility = View.GONE
        }
    }

    private fun isDataIsFilled(): Boolean {

        if (binding.qrScanSubmittedDivCheckBox.isChecked) {
            if (binding.rgTypeOfLr.checkedRadioButtonId > -1 &&
                !binding.qrScanDateSubmission.text.isNullOrEmpty() &&
                !binding.qrScanSubmittedBy.text.isNullOrEmpty() &&
                !binding.qrScanTimesSubmitted.text.isNullOrEmpty() &&
                !binding.qrScanSubmittedDivisionDate.text.isNullOrEmpty()
            ) {
                return true
            }
        } else if (binding.rgTypeOfLr.checkedRadioButtonId > -1 &&
            !binding.qrScanDateSubmission.text.isNullOrEmpty() &&
            !binding.qrScanSubmittedBy.text.isNullOrEmpty() &&
            !binding.qrScanTimesSubmitted.text.isNullOrEmpty()
        ) {
            return true
        }

        Toast.makeText(
            requireContext(), "Please fill up required fields.",
            Toast.LENGTH_LONG
        ).show()
        return false
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
        isQrdataprocessingLoaded = false
        scannedData = null
        //hide the keyboard after pressing the generate button
        val inputMethodManager =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(binding.qrScanSave.windowToken, 0)

        //binding.qrScanScrollView.scrollY = binding.qrScanScrollView.scrollY - 1500
    }

    private fun updateData(id: String?) {

        val dbref = com.google.firebase.database.FirebaseDatabase.getInstance()
            .getReference(getString(R.string.firebase_liquidationLog_ref))

        FirebaseDatabase().updateDataSubmission(
            dbref, getString(R.string.firebase_liquidationLog_ref), id!!,
            SubmissionDataClass(
                id,
                binding.qrScanSchoolName.text.toString(),
                scannedData?.typ,
                binding.qrScanDateSubmission.text.toString(),
                binding.qrScanDateRelease.text.toString(),
                binding.qrScanTimesSubmitted.text.toString().toInt(),
                binding.qrScanSubmittedBy.text.toString(),
                binding.qrScanReleasedTo.text.toString(),
                chkIfSubmittedToDiv(),
                binding.qrScanSubmittedDivisionDate.text.toString()
            )
        ) {
            if (it) {
                Toast.makeText(requireContext(), "Updated successfully", Toast.LENGTH_LONG).show()
                resetViews()
            } else {
                Toast.makeText(requireContext(), "Update failed", Toast.LENGTH_LONG).show()
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


            if (!binding.qrScanDateSubmission.text.isNullOrEmpty()) {
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
            }

            if (!binding.qrScanDateRelease.text.isNullOrEmpty()) {
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
            }

            if (!binding.qrScanDateRelease.text.isNullOrEmpty()) {
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
            }

            val date = dateFormat.format(Date(it))
            et.setText(date)
        }

    }

    private fun saveData() {

        val dbRef = com.google.firebase.database.FirebaseDatabase.getInstance()
            .getReference(getString(R.string.firebase_liquidationLog_ref))

        val id = dbRef.push().key!!
        FirebaseDatabase().initLiquidationDatabase(
            dbRef,
            SubmissionDataClass(
                id,
                qrRead[0],
                tempLrType,
                binding.qrScanDateSubmission.text.toString(),
                binding.qrScanDateRelease.text.toString(),
                binding.qrScanTimesSubmitted.text.toString().toInt(),
                binding.qrScanSubmittedBy.text.toString(),
                binding.qrScanReleasedTo.text.toString(),
                chkIfSubmittedToDiv(),
                binding.qrScanSubmittedDivisionDate.text.toString()
            )
        ) {
            if (it) {
                Toast.makeText(requireContext(), "Saved successfully", Toast.LENGTH_LONG).show()
                resetViews()
            } else {
                Toast.makeText(requireContext(), "Saving failed", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun chkIfSubmittedToDiv(): String {
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
                    binding.qrScanProgress.visibility = View.VISIBLE
                    stopCamera()
                    checkOldData()
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
        if (oldData.isNotEmpty()) {
            val bundle = Bundle().apply {
                putParcelableArrayList(
                    getString(R.string.old_data),
                    oldData as ArrayList<out Parcelable>
                )
            }

            val dialog = QrScanDialogFragment().apply {
                arguments = bundle
            }
            dialog.isCancelable = false
            dialog.show(parentFragmentManager, "Showing Records")
            dialog.setOnDialogExitListener(this)
        } else {
            binding.qrScanSchoolName.text = qrRead[0]
            binding.qrScanHead.text = qrRead[1]

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
            tempLrType = qrRead[2]
            if (qrRead.size > 3) {
                binding.qrScanDateSubmission.setText(qrRead[3])
            }
        }
        isQrdataprocessingLoaded = true
    }

    private fun checkOldData() {
        val dbRef = com.google.firebase.database.FirebaseDatabase.getInstance()
            .getReference(getString(R.string.firebase_liquidationLog_ref))

        FirebaseDatabase().fetchDataSubmission(dbRef, qrRead[0]) {
            binding.qrScanProgress.visibility = View.GONE
            oldData = it
            if (!isQrdataprocessingLoaded) {
                qrDataProcessing()
            }
        }
    }

    private fun stopCamera() {
        cameraProvider.unbindAll()
        binding.qrScanReopen.visibility = View.VISIBLE
        binding.qrScanTint.visibility = View.VISIBLE
        cameraExecutor.shutdown()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
    private fun requestCameraPermission() {
        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    override fun exitListener(data: SubmissionDataClass) {
        scannedData = data
        binding.qrScanSchoolName.text = data.sch
        checkHead(data.sch!!){
            binding.qrScanHead.text = it
        }
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

        binding.qrScanDateSubmission.setText(data.ds)
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

    private fun checkHead(sch: String, callback : (String?) -> Unit){
        FirebaseDatabase().fetchDataQR(
            com.google.firebase.database.FirebaseDatabase.getInstance()
                .getReference(getString(R.string.firebase_qrdata_ref))
        ) {

            for (data in it) {
                if (data.sch_name == sch) {
                    callback(data.sch_head)
                }
            }
        }
    }
}