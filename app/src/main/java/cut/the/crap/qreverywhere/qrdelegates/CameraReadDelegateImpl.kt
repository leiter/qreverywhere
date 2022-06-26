package cut.the.crap.qreverywhere.qrdelegates

import android.app.Activity
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import cut.the.crap.qreverywhere.QrReaderActivity
import cut.the.crap.qreverywhere.R
import cut.the.crap.qreverywhere.scanqrcode.HomeFragment
import cut.the.crap.qreverywhere.stuff.createIntent
import cut.the.crap.qreverywhere.stuff.hasPermission
import cut.the.crap.qreverywhere.stuff.showShortToast

interface GetIt<T>{

    fun readQrcodeWithCamera()

    fun attachCameraReadDelegate(fragment: Fragment)

}

inline fun <reified T : Activity> implDelegate(permissionDeniedAction: () -> Unit) : GetIt<T>{ //

    return  object : GetIt<T> {

        var fragment: Fragment? = null
        var barcodeScannerLauncher: androidx.activity.result.ActivityResultLauncher<Intent>? = null
        var cameraPermissionLauncher: androidx.activity.result.ActivityResultLauncher<String>? = null

        private fun readBarcode() {
            val intent = Intent(fragment?.requireContext(), T::class.java)
            barcodeScannerLauncher?.launch(intent)
        }

        override fun readQrcodeWithCamera() {
            if (fragment?.requireContext()?.hasPermission(android.Manifest.permission.CAMERA) == true) {
                readBarcode()
            } else {
                cameraPermissionLauncher?.launch(android.Manifest.permission.CAMERA)
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
                    if(barcodeRawValue.isNotEmpty()){
                        createIntent(barcodeRawValue, fragment.requireContext())?.let {
                            fragment.startActivity(it)
                        } ?: run {
                            //todo inform and display content   (callback(text))
                        }
                    }


                    // Start file picker
                    val readFromFile = result.data?.getBooleanExtra(
                        QrReaderActivity.EXTRA_START_IMAGE_PICKER, false
                    ) ?: false
                    if (readFromFile) (fragment as HomeFragment).readQrcodeFromFile()
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

    }
}

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
                if(barcodeRawValue.isNotEmpty()){
                    createIntent(barcodeRawValue, fragment.requireContext())?.let {
                        fragment.startActivity(it)
                    } ?: run {
                        //todo inform and display content   (callback(text))
                    }
                }

                val readFromFile = result.data?.getBooleanExtra(
                    QrReaderActivity.EXTRA_START_IMAGE_PICKER, false
                ) ?: false
                if (readFromFile) (fragment as HomeFragment).readQrcodeFromFile()
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