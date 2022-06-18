package cut.the.crap.qreverywhere.stuff

import android.app.Activity
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import cut.the.crap.qreverywhere.QrReaderActivity
import cut.the.crap.qreverywhere.R

class CameraReadDelegateImpl : CameraReadDelegate {

    private lateinit var fragment: Fragment
    private lateinit var barcodeScannerLauncher: androidx.activity.result.ActivityResultLauncher<Intent>
    private lateinit var cameraPermissionLauncher: androidx.activity.result.ActivityResultLauncher<String>

    override fun readQrcodeWithCamera() {
        if (fragment.requireContext().hasPermission(android.Manifest.permission.CAMERA)) {
            readBarcode()
        } else {
            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }

    override fun attachCameraReadDelegate(fragment: Fragment) {
        this.fragment = fragment

        barcodeScannerLauncher = fragment.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val barcodeRawValue = result.data?.getStringExtra(
                    QrReaderActivity.EXTRA_QR_DATA
                ) ?: ""
                createIntent(barcodeRawValue, fragment.requireContext())?.let {
                    fragment.startActivity(it)
                } ?: run {
                    //todo inform and display content   (callback(text))
                }
            }
        }

        cameraPermissionLauncher =
            fragment.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    readBarcode()
                } else {
                    fragment.requireContext().showShortToast(R.string.permission_denied_text)
                }
            }
    }

    private fun readBarcode() {
        val intent = Intent(fragment.requireContext(), QrReaderActivity::class.java)
        barcodeScannerLauncher.launch(intent)
    }
}