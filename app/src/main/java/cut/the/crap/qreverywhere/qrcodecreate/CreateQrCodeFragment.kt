package cut.the.crap.qreverywhere.qrcodecreate

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.zxing.WriterException
import cut.the.crap.qreverywhere.R
import cut.the.crap.qreverywhere.databinding.FragmentCreateQrCodeBinding
import cut.the.crap.qreverywhere.viewBinding
import dagger.hilt.android.AndroidEntryPoint

//  createQREmail    createQRUrl    createPhoneCall     readFromFile    readWithCamera

@AndroidEntryPoint
class CreateQrCodeFragment : Fragment(R.layout.fragment_create_qr_code) {


    private val viewBinding: FragmentCreateQrCodeBinding by viewBinding {
        FragmentCreateQrCodeBinding.bind(requireView())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(viewBinding){
            createEmailQrCode.setOnClickListener {
                findNavController().navigate(R.id.action_createQrCodeFragment_to_createEmailQrCodeFragment)
            }
        }


    }

}