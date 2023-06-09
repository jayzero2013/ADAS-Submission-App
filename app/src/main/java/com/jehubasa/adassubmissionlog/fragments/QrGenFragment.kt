package com.jehubasa.adassubmissionlog.fragments

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.firebase.database.DatabaseReference
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.jehubasa.adassubmissionlog.FirebaseDatabase
import com.jehubasa.adassubmissionlog.R
import com.jehubasa.adassubmissionlog.data.QrInfoDataClass
import com.jehubasa.adassubmissionlog.databinding.FragmentQrGenBinding
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class QrGenFragment : Fragment() {

    private lateinit var binding: FragmentQrGenBinding
    private var storedData: ArrayList<QrInfoDataClass> = arrayListOf()
    private val dbRef: DatabaseReference by lazy {
        com.google.firebase.database.FirebaseDatabase.getInstance()
            .getReference(getString(R.string.firebase_qrdata_ref))
    }

    private val date: String by lazy {
        val calendar = Calendar.getInstance()
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time).also {
            with(calendar) {
                get(Calendar.YEAR)
                get(Calendar.MONTH) + 1
                get(Calendar.DAY_OF_MONTH)
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            shareImage()
        } else {
            Toast.makeText(
                requireContext(),
                "Permission denied. Cannot share image.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentQrGenBinding.inflate(inflater, container, false).also {
            binding = it
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbRef.keepSynced(true)
        //initSpinner()
        initAutoComplete()

        binding.genButton.setOnClickListener {
            if(binding.qrGenSchoolName.text.isNotEmpty()&&
                binding.qrGenSchoolHead.text.isNotEmpty() &&
                    binding.qrGenTypeLq.text.isNotEmpty()){
                makeData()
                saveData()
            } else {
                Toast.makeText(activity, "Fill up the fields", Toast.LENGTH_LONG).show()
            }
        }

        binding.qrGenSend.setOnClickListener {
            checkAndRequestPermission()
        }
    }

    private fun checkAndRequestPermission() {
        val permission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        val permissionGranted = ContextCompat.checkSelfPermission(
            requireContext(),
            permission
        ) == PackageManager.PERMISSION_GRANTED

        if (permissionGranted) {
            shareImage()
        } else {
            if (shouldShowRequestPermissionRationale(permission)) {
                Toast.makeText(
                    requireContext(),
                    "Please grant permission to share image.",
                    Toast.LENGTH_SHORT
                ).show()
            }
            requestPermissionLauncher.launch(permission)
        }
    }

    private fun shareImage() {
        // Save the bitmap to a file
        val view = binding.qrGenCardView // replace with your ImageView
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)

// Save the bitmap to a file
        val filename = "myImage.jpg"
        val file = File(context?.cacheDir, filename)
        file.createNewFile()
        val outputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.flush()
        outputStream.close()

        // Share the image via a messenger app
        val uri = FileProvider.getUriForFile(
            requireContext(),
            "com.jehubasa.adassubmissionlog" + ".provider",
            file
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context?.startActivity(Intent.createChooser(shareIntent, "Share Image"))

    }

    private fun initAutoComplete() {

        val schRecentData = ArrayList<String?>()
        val schHeadRecentData = ArrayList<String?>()
        FirebaseDatabase().fetchDataQR(dbRef
        ) {
            storedData = it
            for (content in it) {
                if (!schRecentData.contains(content.sch_name)) {
                    schRecentData += content.sch_name
                }
                if (!schHeadRecentData.contains(content.sch_head)) {
                    schHeadRecentData += content.sch_head
                }
            }
            schRecentData.let {
                (binding.qrGenSchoolName as? MaterialAutoCompleteTextView)?.setSimpleItems(
                    it.toTypedArray()
                )
            }

            schHeadRecentData.let {
                (binding.qrGenSchoolHead as? MaterialAutoCompleteTextView)?.setSimpleItems(
                    it.toTypedArray()
                )
            }
        }
    }

    private fun saveData() {
        if (!checkIfExisting(
                binding.qrGenSchoolName.text.toString(),
                binding.qrGenSchoolHead.text.toString()
            )
        ) {
            val id = dbRef.push().key!!
            FirebaseDatabase().initQrDatabase(
                dbRef,
                QrInfoDataClass(
                    id,
                    binding.qrGenSchoolName.text.toString(),
                    binding.qrGenSchoolHead.text.toString()
                )
            )
        } else{ Log.e("ASP", "data already exist ( ${binding.qrGenSchoolName.text
        }, ${binding.qrGenSchoolHead.text})")}
    }

    private fun checkIfExisting(name: String, head: String): Boolean {
        if (storedData.isNotEmpty()) {
            for (data in storedData) {
                if (data.sch_name == name && data.sch_head == head) {
                    return true
                }
            }
        }
        return false
    }

    private fun makeData() {

        if (!binding.qrGenSchoolName.text.isNullOrEmpty() &&
            !binding.qrGenSchoolHead.text.isNullOrEmpty() &&
            !binding.qrGenTypeLq.text.isNullOrEmpty()
        ) {

            MaterialAlertDialogBuilder(requireContext())
                .setIcon(R.drawable.baseline_qr_code_scanner_24)
                .setMessage("Do you want to add today's date as DATE OF SUBMISSION?")
                .setPositiveButton("Yes, proceed") { _, _ ->
                    drawQr(
                        "${binding.qrGenSchoolName.text}/${binding.qrGenSchoolHead.text}" +
                                "/${binding.qrGenTypeLq.text}/$date"
                    )
                }
                .setNegativeButton("No") { _, _ ->
                    drawQr(
                        "${binding.qrGenSchoolName.text}/${binding.qrGenSchoolHead.text}" +
                                "/${binding.qrGenTypeLq.text}"
                    )
                }
                .show()

        } else {
            Toast.makeText(activity, "Fill up the fields", Toast.LENGTH_LONG).show()
        }

        //hide the keyboard after pressing the generate button
        val inputMethodManager =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(binding.genButton.windowToken, 0)

    }

    private fun drawQr(s: String) {
        binding.qrViewer.setImageBitmap(
            generateQR(s)
        )
        binding.qrLayoutSchName.text = "School: ${binding.qrGenSchoolName.text}"
        binding.qrLayoutSchHead.text = "Head: ${binding.qrGenSchoolHead.text}"
    }

    private fun generateQR(qrCodeContent: String): Bitmap {
        val size = 512 //pixels
        Log.d("ASP", qrCodeContent)
        hashMapOf<EncodeHintType, Int>().also {
            it[EncodeHintType.MARGIN] = 1
        } // Make the QR code buffer border narrower
        val bits = QRCodeWriter().encode(qrCodeContent, BarcodeFormat.QR_CODE, size, size)

        return Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565).also {
            for (x in 0 until size) {
                for (y in 0 until size) {
                    it.setPixel(x, y, if (bits[x, y]) Color.BLACK else Color.WHITE)
                }
            }
        }
    }
}

