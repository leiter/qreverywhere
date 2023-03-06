package cut.the.crap.qreverywhere.qrcodedetailview

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.google.android.material.progressindicator.LinearProgressIndicator
import cut.the.crap.qreverywhere.MainActivityViewModel
import cut.the.crap.qreverywhere.R
import cut.the.crap.qreverywhere.data.State
import cut.the.crap.qreverywhere.databinding.FragmentDetailViewBinding
import cut.the.crap.qreverywhere.utils.AcquireDateFormatter
import cut.the.crap.qreverywhere.utils.FROM_HISTORY_LIST
import cut.the.crap.qreverywhere.utils.FROM_SCAN_QR
import cut.the.crap.qreverywhere.utils.IntentGenerator
import cut.the.crap.qreverywhere.utils.ORIGIN_FLAG
import cut.the.crap.qreverywhere.utils.ProtocolPrefix.HTTP
import cut.the.crap.qreverywhere.utils.ProtocolPrefix.HTTPS
import cut.the.crap.qreverywhere.utils.ProtocolPrefix.MAILTO
import cut.the.crap.qreverywhere.utils.ProtocolPrefix.TEL
import cut.the.crap.qreverywhere.utils.QrCodeType
import cut.the.crap.qreverywhere.utils.UiEvent
import cut.the.crap.qreverywhere.utils.activityView
import cut.the.crap.qreverywhere.utils.determineType
import cut.the.crap.qreverywhere.utils.gone
import cut.the.crap.qreverywhere.utils.isVcard
import cut.the.crap.qreverywhere.utils.setTitle
import cut.the.crap.qreverywhere.utils.showSnackBar
import cut.the.crap.qreverywhere.utils.startIntentGracefully
import cut.the.crap.qreverywhere.utils.viewBinding
import cut.the.crap.qrrepository.QrItem
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DetailViewFragment : Fragment(R.layout.fragment_detail_view) {

    private val activityViewModel by activityViewModels<MainActivityViewModel>()

    private val args by navArgs<DetailViewFragmentArgs>()

    @Inject
    lateinit var acquireDateFormatter: AcquireDateFormatter

    private val progress by lazy { activityView<LinearProgressIndicator>(R.id.top_progress_indicator) }

    private val viewBinding by viewBinding {
        FragmentDetailViewBinding.bind(requireView())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (args.originFlag == FROM_HISTORY_LIST) {
            setData()
        }
        if (args.originFlag == FROM_SCAN_QR) {
            setObserver()
        }

        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menu.removeItem(R.id.action_about)
                menu.clear()
                menuInflater.inflate(R.menu.menu_detail_view, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
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
        }, viewLifecycleOwner)

        activityViewModel.saveDetailViewQrCodeImage.observe(viewLifecycleOwner) {
            when (it) {
                is State.Success<String?> -> {
                    requireView().showSnackBar(
                        UiEvent.SnackBar(
                            message = R.string.saved_as_file,
                            anchorView = viewBinding.detailViewContentTextView
                        )
                    )
                    progress.hide()
                }
                is State.Error -> {
                    requireView().showSnackBar(
                        UiEvent.SnackBar(
                            message = R.string.error_saved_as_file,
                            anchorView = viewBinding.detailViewContentTextView,
                            backGroundColor = R.color.teal_700
                        )
                    )
                    progress.hide()
                }

                is State.Loading -> progress.show()
                null -> {

                }
            }
        }
    }

    private fun setData() {
        val item = activityViewModel.detailViewQrCodeItem
        val launchText = getQrLaunchButtonText(requireContext(), item.textContent)

        with(viewBinding) {
            Glide.with(root.context).load(item.img).into(detailViewContentPreviewImage)
            detailViewContentTextView.text = Uri.decode(item.textContent)
//            detailViewAcquiredAt.text = acquireDateFormatter.getTimeTemplate(item)
            setTitle(acquireDateFormatter.getTimeTemplate(item))
            if (determineType(activityViewModel.detailViewQrCodeItem.textContent) != QrCodeType.UNKNOWN_CONTENT) {
                detailViewLaunchActionButton.text = launchText
                detailViewLaunchActionButton.setOnClickListener {
                    launchClicked()
                }
            } else {
                detailViewLaunchActionButton.gone()
            }
            detailViewContentPreviewImage.setOnClickListener {
                findNavController().navigate(
                    R.id.action_detailViewFragment_to_qrFullscreenFragment, bundleOf(
                    "itemPosition" to activityViewModel.focusedItemIndex,
                    ORIGIN_FLAG to FROM_HISTORY_LIST

                )
                )
            }

        }
    }

    private fun setObserver() {
        activityViewModel.detailViewLiveQrCodeItem.observe(viewLifecycleOwner) {
            when (it) {
                is State.Loading<QrItem> -> {
                    progress.show()
                }
                is State.Success<QrItem> -> {
                    if (it.data != null) {
                        setData()
                    }
                    progress.hide()
                }
                is State.Error -> {
                    progress.hide()
                }
            }
        }
    }

    private fun getQrLaunchButtonText(context: Context, contentString: String): String {
        val decoded = Uri.decode(contentString)
        val launchTextTemplate = context.getString(R.string.qr_detail_launch_template)
        return when {
            decoded.startsWith(TEL) -> launchTextTemplate.format(context.getString(R.string.ic_open_phone_app))
            decoded.startsWith(MAILTO) -> launchTextTemplate.format(context.getString(R.string.ic_open_mail_app))
            decoded.startsWith(HTTP) -> launchTextTemplate.format(context.getString(R.string.ic_open_in_browser))
            decoded.startsWith(HTTPS) -> launchTextTemplate.format(context.getString(R.string.ic_open_in_browser))
            isVcard(decoded) -> context.getString(R.string.ic_import_contact)
            else -> context.getString(R.string.app_name)
        }
    }

    private fun launchClicked() {
        if (determineType(activityViewModel.detailViewQrCodeItem.textContent) != QrCodeType.UNKNOWN_CONTENT) {
            IntentGenerator.QrStartIntent(
                activityViewModel.detailViewQrCodeItem.textContent
            ).getIntent().startIntentGracefully(requireContext())
        } else {
            viewBinding.detailViewLaunchActionButton.gone()
        }

    }

}