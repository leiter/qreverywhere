package cut.the.crap.qreverywhere.qrcodedetailview

import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import cut.the.crap.qreverywhere.MainActivityViewModel
import cut.the.crap.qreverywhere.R
import cut.the.crap.qreverywhere.databinding.FragmentDetailViewBinding
import cut.the.crap.qreverywhere.stuff.*
import cut.the.crap.qreverywhere.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetailViewFragment : Fragment(R.layout.fragment_detail_view) {

    private val activityViewModel by activityViewModels<MainActivityViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    private val viewBinding by viewBinding {
        FragmentDetailViewBinding.bind(requireView())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val item = activityViewModel.detailViewQrCodeItem

        val launchText = getString(R.string.qr_detail_launch_template).format(getString(
            getQrLaunchText(item.textContent)))

        with(viewBinding){
            Glide.with(root.context).load(item.img).into(detailViewContentPreviewImage)
            detailViewContentTextView.text = Uri.decode(item.textContent)
            if(determineType(activityViewModel.detailViewQrCodeItem.textContent)!= QrCode.UNKNOWN_CONTENT){
                detailViewActionTypeTextView.text = launchText
                detailViewLaunchActionButton.setOnClickListener {
                    launchClicked()
                }
            } else {
                detailViewLaunchActionButton.gone()
            }

        }
    }

    private fun launchClicked(){
        if(determineType(activityViewModel.detailViewQrCodeItem.textContent)!= QrCode.UNKNOWN_CONTENT){
            val intent = createOpenIntent(
                activityViewModel.detailViewQrCodeItem.textContent, requireContext()
            )
            startActivity(intent)
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.menu_delete -> {
                activityViewModel.deleteCurrentDetailView()
                findNavController().navigate(R.id.action_detailViewFragment_to_qrHistoryFragment)
                return true
            }
            R.id.menu_save_to_file -> {
                activityViewModel.saveQrImageOfDetailView(requireContext())
                return true
            }
            else -> false
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.removeItem(R.id.action_about)
        inflater.inflate(R.menu.menu_detail_view,menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

}