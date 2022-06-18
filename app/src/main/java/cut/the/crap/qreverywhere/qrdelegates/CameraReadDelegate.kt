package cut.the.crap.qreverywhere.qrdelegates

import androidx.fragment.app.Fragment


interface CameraReadDelegate {

    fun attachCameraReadDelegate(fragment: Fragment)

    fun readQrcodeWithCamera()

}

