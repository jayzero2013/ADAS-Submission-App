package com.jehubasa.adassubmissionlog

import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

class QRCodeAnalyzer(private val onQrCodesDetected: (qrCodes: List<String>) -> Unit) : ImageAnalysis.Analyzer {
    private val barcodeScanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder().setBarcodeFormats(
        Barcode.FORMAT_QR_CODE).build())

    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image?: return@analyze
        @ExperimentalGetImage val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        barcodeScanner.process(image)
            .addOnSuccessListener { barcodes ->
                val qrCodes = barcodes.mapNotNull { barcode ->
                    if (barcode.valueType == Barcode.TYPE_URL ||
                        barcode.valueType == Barcode.TYPE_TEXT) {
                        barcode.rawValue
                    } else {
                        null
                    }
                }
                if (qrCodes.isNotEmpty()) {
                    onQrCodesDetected(qrCodes)
                }
                imageProxy.close()
            }
            .addOnFailureListener {
                imageProxy.close()
            }
    }
}





