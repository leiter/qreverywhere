package cut.the.crap.qreverywhere.qrcodedetailview

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
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
import cut.the.crap.qreverywhere.utils.data.AcquireDateFormatter
import cut.the.crap.qreverywhere.utils.data.IntentGenerator.QrStartIntent
import cut.the.crap.qreverywhere.utils.data.ProtocolPrefix.HTTP
import cut.the.crap.qreverywhere.utils.data.ProtocolPrefix.HTTPS
import cut.the.crap.qreverywhere.utils.data.ProtocolPrefix.MAILTO
import cut.the.crap.qreverywhere.utils.data.ProtocolPrefix.TEL
import cut.the.crap.qreverywhere.utils.data.QrCodeType
import cut.the.crap.qreverywhere.utils.data.detailTitle
import cut.the.crap.qreverywhere.utils.data.determineType
import cut.the.crap.qreverywhere.utils.data.fabLaunchIcon
import cut.the.crap.qreverywhere.utils.data.isVcard
import cut.the.crap.qreverywhere.utils.ui.FROM_CREATE_CONTEXT
import cut.the.crap.qreverywhere.utils.ui.FROM_HISTORY_LIST
import cut.the.crap.qreverywhere.utils.ui.FROM_SCAN_QR
import cut.the.crap.qreverywhere.utils.ui.ORIGIN_FLAG
import cut.the.crap.qreverywhere.utils.ui.UiEvent
import cut.the.crap.qreverywhere.utils.ui.activityView
import cut.the.crap.qreverywhere.utils.ui.ensureBottomNavigation
import cut.the.crap.qreverywhere.utils.ui.gone
import cut.the.crap.qreverywhere.utils.ui.setSubTitle
import cut.the.crap.qreverywhere.utils.ui.setTitle
import cut.the.crap.qreverywhere.utils.ui.setupMenuItems
import cut.the.crap.qreverywhere.utils.ui.showSnackBar
import cut.the.crap.qreverywhere.utils.ui.startIntentGracefully
import cut.the.crap.qreverywhere.utils.ui.viewBinding
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

    private val optionMap by lazy {
        val map = mutableMapOf(
            R.id.menu_save_to_file to {
                activityViewModel.saveQrImageOfDetailView(requireContext())
            },
        )
        if (args.originFlag == FROM_SCAN_QR) {
            map[R.id.menu_delete] = { findNavController().navigateUp() }
        } else {
            map[R.id.menu_delete] = {
                activityViewModel.deleteCurrentDetailView()
                findNavController().navigate(R.id.action_detailViewFragment_to_qrHistoryFragment)
            }

        }
        if (args.originFlag == FROM_CREATE_CONTEXT) {
            map[android.R.id.home] = {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }
        map
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigateUp()
                findNavController().navigate(R.id.qrHistoryFragment)
                isEnabled = false
            }
        }
        if (args.originFlag == FROM_CREATE_CONTEXT) {
            requireActivity().onBackPressedDispatcher.addCallback(callback)
        }
    }

    override fun onResume() {
        super.onResume()
        ensureBottomNavigation()
        setupToolbar()
    }

    private fun setupToolbar() {
        val item = activityViewModel.detailViewQrCodeItem
        setTitle(item.detailTitle)
        setSubTitle(acquireDateFormatter.getTimeTemplate(item))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (args.originFlag == FROM_HISTORY_LIST || args.originFlag == FROM_CREATE_CONTEXT) {
            setData()
        }
        if (args.originFlag == FROM_SCAN_QR) {
            setObserver()
        }

        setupMenuItems(R.menu.menu_detail_view, optionMap)

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
                    progress.hide()
                }
            }
        }
    }

    private fun setData() {
        val item = activityViewModel.detailViewQrCodeItem
        val launchText = getQrLaunchButtonText(requireContext(), item)

        with(viewBinding) {
            Glide.with(root.context).load(item.img).into(detailViewContentPreviewImage)
            detailViewContentTextView.text = Uri.decode(item.textContent)

            if (activityViewModel.detailViewQrCodeItem.determineType() != QrCodeType.UNKNOWN_CONTENT) {
                detailViewLaunchActionButton.text = launchText
                detailViewLaunchActionButton.setIconResource(item.fabLaunchIcon)
                detailViewLaunchActionButton.setOnClickListener {
                    launchClicked()
                }
            } else {
                detailViewLaunchActionButton.gone()
            }
            detailViewContentPreviewImage.setOnClickListener {
                findNavController().navigate(
                    R.id.action_detailViewFragment_to_qrFullscreenFragment,
                    bundleOf(
                        ORIGIN_FLAG to FROM_HISTORY_LIST)
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        setSubTitle("")
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

    private fun getQrLaunchButtonText(context: Context, qrItem: QrItem): String {
        val decoded = Uri.decode(qrItem.textContent)
        val launchTextTemplate = context.getString(R.string.qr_detail_launch_template)
        return when {
            decoded.startsWith(TEL) -> launchTextTemplate.format(context.getString(R.string.ic_open_phone_app))
            decoded.startsWith(MAILTO) -> launchTextTemplate.format(context.getString(R.string.ic_open_mail_app))
            decoded.startsWith(HTTP) -> launchTextTemplate.format(context.getString(R.string.ic_open_in_browser))
            decoded.startsWith(HTTPS) -> launchTextTemplate.format(context.getString(R.string.ic_open_in_browser))
            qrItem.isVcard() -> context.getString(R.string.ic_import_contact)
            else -> context.getString(R.string.app_name)
        }
    }

    private fun launchClicked() {
        if (activityViewModel.detailViewQrCodeItem.determineType() != QrCodeType.UNKNOWN_CONTENT) {
            requireContext().startIntentGracefully(QrStartIntent(activityViewModel.detailViewQrCodeItem.textContent)
                .getIntent())
        } else {
            viewBinding.detailViewLaunchActionButton.gone()
        }
    }

}