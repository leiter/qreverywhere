package cut.the.crap.qreverywhere.qrhistory

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import com.google.android.material.progressindicator.LinearProgressIndicator
import cut.the.crap.qreverywhere.MainActivityViewModel
import cut.the.crap.qreverywhere.R
import cut.the.crap.qreverywhere.data.State
import cut.the.crap.qreverywhere.databinding.FragmentQrHistoryBinding
import cut.the.crap.qreverywhere.utils.AcquireDateFormatter
import cut.the.crap.qreverywhere.utils.FROM_HISTORY_LIST
import cut.the.crap.qreverywhere.utils.ORIGIN_FLAG
import cut.the.crap.qreverywhere.utils.UiEvent
import cut.the.crap.qreverywhere.utils.activityView
import cut.the.crap.qreverywhere.utils.gone
import cut.the.crap.qreverywhere.utils.showSnackBar
import cut.the.crap.qreverywhere.utils.viewBinding
import cut.the.crap.qreverywhere.utils.visible
import cut.the.crap.qrrepository.QrItem
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class QrHistoryFragment : Fragment(R.layout.fragment_qr_history) {

    private val viewBinding by viewBinding { FragmentQrHistoryBinding.bind(requireView()) }

    private val activityViewModel by activityViewModels<MainActivityViewModel>()

    @Inject
    lateinit var acquireDateFormatter: AcquireDateFormatter

    private val progress by lazy { activityView<LinearProgressIndicator>(R.id.top_progress_indicator) }

    private val detailViewItemClicked: (QrItem) -> Unit = {
        activityViewModel.setDetailViewItem(it)
        findNavController().navigate(
            R.id.action_qrHistoryFragment_to_detailViewFragment, bundleOf(
            ORIGIN_FLAG to FROM_HISTORY_LIST
        )
        )
    }

    private val handleSwipeAction: (position: Int, direction: Int) -> Unit = { pos, direction ->
        when (direction) {
            ItemTouchHelper.START -> {
                navigateToFullscreen(pos)
            }
            ItemTouchHelper.END -> {
                activityViewModel.removeHistoryItem(pos)

            }
        }
    }

    private val navigateToFullscreen: (Int) -> Unit = { pos ->
        activityViewModel.focusedItemIndex = pos
        findNavController().navigate(
            R.id.action_qrHistoryFragment_to_qrFullscreenFragment, bundleOf(
            "itemPosition" to pos,
            ORIGIN_FLAG to FROM_HISTORY_LIST
            )
        )
    }

    private val historyListAdapter by lazy {
        QrHistoryAdapter(
            detailViewItemClicked,
            navigateToFullscreen,
            acquireDateFormatter
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(viewBinding) {
            val dragManager =
                ItemDragManager(
                    handleSwipeAction, 0,
                    ItemTouchHelper.END or ItemTouchHelper.START
                )
            ItemTouchHelper(dragManager).attachToRecyclerView(qrHistoryList)
            qrHistoryList.adapter = historyListAdapter
            activityViewModel.historyAdapterData.observe(viewLifecycleOwner) {
                if (it.isNotEmpty()) {
                    qrHistoryEmptyMessage.gone()
                } else {
                    qrHistoryEmptyMessage.visible()
                }
                historyListAdapter.setData(it)
            }
            activityViewModel.removeItemSingleLiveDataEvent.observe(viewLifecycleOwner) {
                when (it) {
                    is State.Error -> { progress.hide() }
                    is State.Loading -> progress.show()
                    is State.Success -> {
                        progress.hide()
                        viewBinding.root.showSnackBar(
                            UiEvent.SnackBar(
                                message = R.string.item_deleted,
                                anchorView = requireActivity().findViewById(R.id.nav_view),
                                actionLabel = R.string.undo_delete,
                                actionBlock = { activityViewModel.saveQrItem(it.data!!) }
                            )
                        )
                    }
                    null -> {

                    }
                }
            }
        }
    }

}

