package com.projectx.app.feature.campusmap

import android.content.Context
import com.google.android.gms.common.moduleinstall.ModuleInstall
import com.google.android.gms.common.moduleinstall.ModuleInstallRequest
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.projectx.app.navmap.qr.QrPayload
import com.projectx.app.navmap.qr.QrPayloadParser
import com.projectx.app.navmap.qr.QrResult

/**
 * Launches the Google Play Services system barcode scanner. No camera permission is
 * required because the scanner UI runs in the Play Services process. The scanner itself
 * is delivered via Play Services, so no 16 KB-sensitive native libraries ship in the APK.
 */
object QrScanLauncher {

    fun scan(
        context: Context,
        onPayload: (QrPayload) -> Unit,
        onError: (String) -> Unit,
        onCancel: () -> Unit = {},
    ) {
        val options = GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .enableAutoZoom()
            .build()
        val scanner: GmsBarcodeScanner = GmsBarcodeScanning.getClient(context, options)

        // Pre-install the scanner module if it hasn't been downloaded yet; some devices
        // do this lazily and the first scan would otherwise fail with "module not available".
        val moduleInstall = ModuleInstall.getClient(context)
        val installRequest = ModuleInstallRequest.newBuilder()
            .addApi(scanner)
            .build()
        moduleInstall.installModules(installRequest)
            .addOnCompleteListener { startScan(scanner, onPayload, onError, onCancel) }
    }

    private fun startScan(
        scanner: GmsBarcodeScanner,
        onPayload: (QrPayload) -> Unit,
        onError: (String) -> Unit,
        onCancel: () -> Unit,
    ) {
        scanner.startScan()
            .addOnSuccessListener { barcode ->
                val raw = barcode.rawValue
                when (val parsed = QrPayloadParser.parse(raw)) {
                    is QrResult.Valid -> onPayload(parsed.payload)
                    is QrResult.Invalid -> onError(parsed.reason)
                }
            }
            .addOnCanceledListener { onCancel() }
            .addOnFailureListener { onError("Scanner unavailable: ${it.message ?: "unknown"}") }
    }
}
