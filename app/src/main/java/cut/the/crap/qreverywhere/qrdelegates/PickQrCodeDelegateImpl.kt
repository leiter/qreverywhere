package cut.the.crap.qreverywhere.stuff

import android.content.Intent
import androidx.fragment.app.Fragment

class PickQrCodeDelegateImpl : PickQrCodeDelegate {

    private lateinit var fragment: Fragment
    private lateinit var scanImageLauncher: androidx.activity.result.ActivityResultLauncher<Intent>
    private lateinit var readStoragePermissionLauncher: androidx.activity.result.ActivityResultLauncher<String>


    override fun readQrcodeFromFile() {
        if (fragment.requireContext().hasPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
            readBarcode()
        } else {
            readStoragePermissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    override fun attachPickQrCodeDelegate(fragment: Fragment) {
        this.fragment = fragment


        scanImageLauncher = fragment.registerForActivityResult(
                androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
                ) { result ->
            if (result.resultCode == android.app.Activity.RESULT_OK) {
                val uri = result.data?.data
                uri?.let {
                    val g = scanQrImage(it, fragment.requireContext())
                    g?.let {  android.widget.Toast.makeText(fragment.requireContext(),g,
                        android.widget.Toast.LENGTH_LONG).show() }
                }
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