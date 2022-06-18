package cut.the.crap.qreverywhere.stuff

import androidx.fragment.app.Fragment


interface CameraReadDelegate {

    fun attachCameraReadDelegate(fragment: Fragment)

    fun readQrcodeWithCamera()

}

