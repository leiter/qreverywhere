package cut.the.crap.qreverywhere.qrdelegates

import android.app.Activity
import android.content.Intent
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import cut.the.crap.qreverywhere.R
import cut.the.crap.qreverywhere.qrcodescan.HomeFragment
import cut.the.crap.qreverywhere.utils.hasPermission
import cut.the.crap.qreverywhere.utils.scanQrImage
import cut.the.crap.qreverywhere.utils.showShortToast
import cut.the.crap.qrrepository.Acquire

class PickQrCodeDelegateImpl : PickQrCodeDelegate {

    private lateinit var fragment: Fragment
    private lateinit var scanImageLauncher: androidx.activity.result.ActivityResultLauncher<Intent>
    private lateinit var readStoragePermissionLauncher: androidx.activity.result.ActivityResultLauncher<String>

    override fun readQrcodeFromFile() {
        if (fragment.requireContext().hasPermission(permissionByApiVersion())) {
            readBarcode()
        } else {
            readStoragePermissionLauncher.launch(permissionByApiVersion())
        }
    }

    override fun attachPickQrCodeDelegate(fragment: Fragment) {
        this.fragment = fragment

        scanImageLauncher = fragment.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data

                uri?.let {
                    val qrScanResult = scanQrImage(it, fragment.requireContext())
                    qrScanResult?.let { result ->
                        (fragment as HomeFragment).handleQrCode(result, Acquire.FROM_FILE)
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

    private fun permissionByApiVersion(): String {
        return if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2)
            android.Manifest.permission.READ_MEDIA_IMAGES
        else
            android.Manifest.permission.READ_EXTERNAL_STORAGE
    }

    private fun readBarcode() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }
        scanImageLauncher.launch(intent)
    }

}