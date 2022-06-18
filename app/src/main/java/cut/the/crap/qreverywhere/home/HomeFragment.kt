package cut.the.crap.qreverywhere.home

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import cut.the.crap.qreverywhere.R
import cut.the.crap.qreverywhere.databinding.FragmentHomeBinding
import cut.the.crap.qreverywhere.qrdelegates.CameraReadDelegate
import cut.the.crap.qreverywhere.qrdelegates.CameraReadDelegateImpl
import cut.the.crap.qreverywhere.qrdelegates.PickQrCodeDelegate
import cut.the.crap.qreverywhere.qrdelegates.PickQrCodeDelegateImpl
import cut.the.crap.qreverywhere.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home),
    CameraReadDelegate by CameraReadDelegateImpl(),
        PickQrCodeDelegate by PickQrCodeDelegateImpl()
{

    private val viewBinding by viewBinding {
        FragmentHomeBinding.bind(requireView())
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        attachCameraReadDelegate(this)
        attachPickQrCodeDelegate(this)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(viewBinding) {
            homeReadFromCamera.setOnClickListener {
                readQrcodeWithCamera()
            }
            homeReadFromFile.setOnClickListener {
                readQrcodeFromFile()
            }
            homeCreateQr.setOnClickListener {
//                findNavController().navigate(R.id.action_homeFragment_to_FirstFragment)
            }
        }
    }



}