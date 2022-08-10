package cut.the.crap.qreverywhere.qrfullscreen

import android.os.Bundle
import android.view.Menu
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import cut.the.crap.qreverywhere.MainActivityViewModel
import cut.the.crap.qreverywhere.R
import cut.the.crap.qreverywhere.databinding.FragmentQrFullscreenBinding
import cut.the.crap.qreverywhere.utils.viewBinding

class QrFullscreenFragment : Fragment(R.layout.fragment_qr_fullscreen) {

    private val activityViewModel by activityViewModels<MainActivityViewModel>()

    private val viewBinding by viewBinding { FragmentQrFullscreenBinding.bind(requireView()) }

    private val args: QrFullscreenFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activityViewModel.historyAdapterData.observe(viewLifecycleOwner){
            viewBinding.qrFullScreenImage.setImageBitmap(it[args.itemPosition].img)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
        super.onPrepareOptionsMenu(menu)
    }
}