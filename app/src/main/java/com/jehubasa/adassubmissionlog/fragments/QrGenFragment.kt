package com.jehubasa.adassubmissionlog.fragments

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.jehubasa.adassubmissionlog.QrGenDataBaseHelper
import com.jehubasa.adassubmissionlog.data.QrInfoDataClass
import kotlin.text.isNullOrEmpty
import com.jehubasa.adassubmissionlog.databinding.FragmentQrGenBinding

class QrGenFragment : Fragment() {

    private lateinit var binding: FragmentQrGenBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentQrGenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.genButton.setOnClickListener {
            makeData()
            saveData()
        }
    }

    private fun saveData() {
        val dbhelper = QrGenDataBaseHelper(context)
        val db = dbhelper.writableDatabase
        val data = arrayOf(
            QrInfoDataClass(
                null, binding.qrGenSchoolName.text.toString(),
                binding.qrGenSchoolHead.text.toString()
            )
        )

        if (dbhelper.insertDate(db, data) > -1) {
            Toast.makeText(activity, "data saved", Toast.LENGTH_LONG).show()
        }

    }

    private fun makeData() {

        if (!binding.qrGenSchoolName.text.isNullOrEmpty() &&
            !binding.qrGenSchoolHead.text.isNullOrEmpty() &&
            !binding.qrGenLrType.text.isNullOrEmpty()
        ) {
            binding.qrViewer.setImageBitmap(generateQR())
        } else {
            Toast.makeText(activity, "Fill up the fields", Toast.LENGTH_LONG).show()
        }

        //hide the keyboard after pressing the generate button
        val inputMethodManager =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(binding.genButton.windowToken, 0)

    }

    private fun generateQR(): Bitmap {
        val size = 512 //pixels
        val qrCodeContent = "${binding.qrGenSchoolName.text}/${binding.qrGenSchoolHead.text}" +
                "/${binding.qrGenLrType.text}"
        Log.d("ASP", qrCodeContent)
        val hints = hashMapOf<EncodeHintType, Int>().also {
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