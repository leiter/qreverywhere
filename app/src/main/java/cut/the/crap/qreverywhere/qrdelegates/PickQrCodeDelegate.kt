package cut.the.crap.qreverywhere.stuff

import androidx.fragment.app.Fragment

interface PickQrCodeDelegate {

    fun attachPickQrCodeDelegate(fragment: Fragment)
    fun readQrcodeFromFile()
}


