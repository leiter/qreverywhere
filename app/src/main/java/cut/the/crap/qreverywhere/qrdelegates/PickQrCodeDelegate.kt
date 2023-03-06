package cut.the.crap.qreverywhere.qrdelegates

import androidx.fragment.app.Fragment
import cut.the.crap.qreverywhere.MainActivityViewModel

interface PickQrCodeDelegate {

    fun attachPickQrCodeDelegate(fragment: Fragment, activityViewModel: MainActivityViewModel)

    fun readQrcodeFromFile()

    fun permissionByApiVersion(): String

}


