package cut.the.crap.qreverywhere.qrcodecreate

import android.os.Bundle
import android.view.View
import androidx.annotation.UiContext
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import cut.the.crap.qreverywhere.R
import cut.the.crap.qreverywhere.databinding.FragmentCreateQrCodeBinding
import cut.the.crap.qreverywhere.qrcodecreate.CreateOneLinerFragment.Companion.CREATE_PHONE
import cut.the.crap.qreverywhere.qrcodecreate.CreateOneLinerFragment.Companion.CREATE_SMS
import cut.the.crap.qreverywhere.qrcodecreate.CreateOneLinerFragment.Companion.CREATE_WEB
import cut.the.crap.qreverywhere.qrcodecreate.CreateOneLinerFragment.Companion.USE_CASE_MODE
import cut.the.crap.qreverywhere.viewBinding
import dagger.hilt.android.AndroidEntryPoint

//  createContact    readContact

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

            createPhoneQrCode.setOnClickListener {
                findNavController().navigate(R.id.action_createQrCodeFragment_to_createOneLinerFragment, bundleOf(
                    USE_CASE_MODE to CREATE_PHONE
                ))
            }

            createSmsQrCode.setOnClickListener {
                findNavController().navigate(R.id.action_createQrCodeFragment_to_createOneLinerFragment, bundleOf(
                    USE_CASE_MODE to CREATE_SMS
                ))
            }

            createWebQrCode.setOnClickListener {
                findNavController().navigate(R.id.action_createQrCodeFragment_to_createOneLinerFragment, bundleOf(
                    USE_CASE_MODE to CREATE_WEB
                ))
            }
        }


    }



}