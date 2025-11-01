package cut.the.crap.qreverywhere.qrfullscreen

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import cut.the.crap.qreverywhere.MainActivityViewModel
import cut.the.crap.qreverywhere.R
import cut.the.crap.qreverywhere.databinding.FragmentQrFullscreenBinding
import cut.the.crap.qreverywhere.utils.data.AcquireDateFormatter
import cut.the.crap.qreverywhere.utils.ui.setTitle
import cut.the.crap.qreverywhere.utils.ui.viewBinding
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class QrFullscreenFragment : Fragment(R.layout.fragment_qr_fullscreen) {

    private val activityViewModel by activityViewModel<MainActivityViewModel>()

    private val viewBinding by viewBinding { FragmentQrFullscreenBinding.bind(requireView()) }

    private val acquireDateFormatter: AcquireDateFormatter by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activityViewModel.historyAdapterData.observe(viewLifecycleOwner){
            val item = activityViewModel.detailViewQrCodeItem
            viewBinding.qrFullScreenImage.setImageBitmap(item.img)
            setTitle(acquireDateFormatter.getTimeTemplate(item))
        }
    }

}