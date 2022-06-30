package cut.the.crap.qreverywhere.qrfullscreen

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import cut.the.crap.qreverywhere.MainActivityViewModel
import cut.the.crap.qreverywhere.R
import cut.the.crap.qreverywhere.databinding.FragmentQrFullscreenBinding
import cut.the.crap.qreverywhere.viewBinding

class QrFullscreenFragment : Fragment(R.layout.fragment_qr_fullscreen) {

    private val activityViewModel by activityViewModels<MainActivityViewModel>()

    private val viewBinding by viewBinding { FragmentQrFullscreenBinding.bind(requireView()) }

    private val args: QrFullscreenFragmentArgs by navArgs<QrFullscreenFragmentArgs>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(viewBinding){
            activityViewModel.provideListItem(args.itemPosition)?.let {
                    qrFullScreenImage.setImageBitmap(it.img)
            }
        }

    }
}