package cut.the.crap.qreverywhere.qrfullscreen

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import cut.the.crap.qreverywhere.MainActivityViewModel
import cut.the.crap.qreverywhere.R
import cut.the.crap.qreverywhere.databinding.FragmentQrFullscreenBinding
import cut.the.crap.qreverywhere.utils.data.AcquireDateFormatter
import cut.the.crap.qreverywhere.utils.ui.setTitle
import cut.the.crap.qreverywhere.utils.ui.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class QrFullscreenFragment : Fragment(R.layout.fragment_qr_fullscreen) {

    private val activityViewModel by activityViewModels<MainActivityViewModel>()

    private val viewBinding by viewBinding { FragmentQrFullscreenBinding.bind(requireView()) }

    @Inject
    lateinit var acquireDateFormatter: AcquireDateFormatter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activityViewModel.historyAdapterData.observe(viewLifecycleOwner){
            val item = activityViewModel.detailViewQrCodeItem
            viewBinding.qrFullScreenImage.setImageBitmap(item.img)
            setTitle(acquireDateFormatter.getTimeTemplate(item))
        }
    }

}