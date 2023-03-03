package cut.the.crap.qreverywhere.qrdelegates

import androidx.fragment.app.Fragment

interface PickQrCodeDelegate {

    fun attachPickQrCodeDelegate(fragment: Fragment)

    fun readQrcodeFromFile()

    fun permissionByApiVersion(): String

}


