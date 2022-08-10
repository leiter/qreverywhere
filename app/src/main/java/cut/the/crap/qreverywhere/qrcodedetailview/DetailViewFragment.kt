package cut.the.crap.qreverywhere.qrcodedetailview

import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
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
import cut.the.crap.qreverywhere.db.QrCodeItem
import cut.the.crap.qreverywhere.utils.*
import cut.the.crap.qreverywhere.utils.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DetailViewFragment : Fragment(R.layout.fragment_detail_view) {

    private val activityViewModel by activityViewModels<MainActivityViewModel>()

    private val args by navArgs<DetailViewFragmentArgs>()

    @Inject
    lateinit var acquireDateFormatter: AcquireDateFormatter

    private fun getProgressIndicator(): LinearProgressIndicator {
        return requireActivity().findViewById(R.id.top_progress_indicator)
    }

    private val progress by lazy {
        getProgressIndicator()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

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
        val launchText = getString(R.string.qr_detail_launch_template).format(
            getString(
                getQrLaunchText(item.textContent)
            )
        )

        with(viewBinding) {
            Glide.with(root.context).load(item.img).into(detailViewContentPreviewImage)
            detailViewContentTextView.text = Uri.decode(item.textContent)
            detailViewAcquiredAt.text = acquireDateFormatter.getTimeTemplate(item)
            if (determineType(activityViewModel.detailViewQrCodeItem.textContent) != QrCode.UNKNOWN_CONTENT) {
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
                is State.Loading<QrCodeItem> -> {
                    progress.show()
                }
                is State.Success<QrCodeItem> -> {
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

    private fun launchClicked() {
        if (determineType(activityViewModel.detailViewQrCodeItem.textContent) != QrCode.UNKNOWN_CONTENT) {
            val intent = createOpenIntent(
                activityViewModel.detailViewQrCodeItem.textContent, requireContext()
            )
            startActivity(intent)
        } else {
            viewBinding.detailViewLaunchActionButton.gone()
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
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
        menu.clear()
        inflater.inflate(R.menu.menu_detail_view, menu)
//        super.onCreateOptionsMenu(menu, inflater)
    }


}