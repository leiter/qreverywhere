package cut.the.crap.qreverywhere.qrcodecreate

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.navArgs
import cut.the.crap.qreverywhere.R
import cut.the.crap.qreverywhere.databinding.FragmentCreateOneLinerBinding
import cut.the.crap.qreverywhere.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CreateOneLinerFragment : Fragment(R.layout.fragment_create_one_liner) {

    private val args: CreateOneLinerFragmentArgs by navArgs()

    private val viewBinding by viewBinding { FragmentCreateOneLinerBinding.bind(requireView()) }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        when(args.useCaseMode){
            CREATE_PHONE -> setupCreateCallQrcode()
            CREATE_SMS -> setupCreateSMSQrcode()
            CREATE_WEB -> setupCreateWebQrcode()
            else -> throw IllegalArgumentException("No fragment associated with this id=${args.useCaseMode}")
        }
    }

    private fun setupCreateCallQrcode() {
        (activity as AppCompatActivity).supportActionBar?.setTitle(R.string.create_title_phone)
    }

    private fun setupCreateSMSQrcode() {
        (activity as AppCompatActivity).supportActionBar?.setTitle(R.string.create_title_sms)

    }

    private fun setupCreateWebQrcode() {
        (activity as AppCompatActivity).supportActionBar?.setTitle(R.string.create_title_web)

    }

    companion object {
        const val USE_CASE_MODE = "useCaseMode"
        const val CREATE_SMS = 0
        const val CREATE_PHONE = 1
        const val CREATE_WEB = 2
    }

}