package cut.the.crap.qreverywhere.qrdelegates

import android.content.Intent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import cut.the.crap.qreverywhere.R
import cut.the.crap.qreverywhere.qrcodescan.HomeFragment
import cut.the.crap.qreverywhere.stuff.Acquire
import cut.the.crap.qreverywhere.stuff.hasPermission
import cut.the.crap.qreverywhere.stuff.scanQrImage
import cut.the.crap.qreverywhere.stuff.showShortToast

class PickQrCodeDelegateImpl : PickQrCodeDelegate {

    private lateinit var fragment: Fragment
    private lateinit var scanImageLauncher: androidx.activity.result.ActivityResultLauncher<Intent>
    private lateinit var readStoragePermissionLauncher: androidx.activity.result.ActivityResultLauncher<String>

    override fun readQrcodeFromFile() {
        if (fragment.requireContext()
                .hasPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        ) {
            readBarcode()
        } else {
            readStoragePermissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    override fun attachPickQrCodeDelegate(fragment: Fragment) {
        this.fragment = fragment

        scanImageLauncher = fragment.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == android.app.Activity.RESULT_OK) {
                val uri = result.data?.data
                uri?.let {
                    val g = scanQrImage(it, fragment.requireContext())  // todo use coroutine
                    g?.let {
                        (fragment as HomeFragment).handleQrCode(g, Acquire.FROM_FILE)
                    }
                }
            }
        }

        readStoragePermissionLauncher =
            fragment.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    readBarcode()
                } else {
                    fragment.requireContext().showShortToast(R.string.permission_denied_text)
                }
            }
    }

    private fun readBarcode() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }
        scanImageLauncher.launch(intent)
    }

}