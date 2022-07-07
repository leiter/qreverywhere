package cut.the.crap.qreverywhere.qrhistory

import android.os.Bundle
import android.view.Menu
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.progressindicator.LinearProgressIndicator
import cut.the.crap.qreverywhere.MainActivityViewModel
import cut.the.crap.qreverywhere.R
import cut.the.crap.qreverywhere.data.State
import cut.the.crap.qreverywhere.databinding.FragmentQrHistoryBinding
import cut.the.crap.qreverywhere.db.QrCodeItem
import cut.the.crap.qreverywhere.qrcodedetailview.DetailViewFragment
import cut.the.crap.qreverywhere.stuff.*
import cut.the.crap.qreverywhere.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class QrHistoryFragment : Fragment(R.layout.fragment_qr_history) {

    private val viewBinding by viewBinding { FragmentQrHistoryBinding.bind(requireView()) }

    private val activityViewModel by activityViewModels<MainActivityViewModel>()

    @Inject
    lateinit var acquireDateFormatter: AcquireDateFormatter

    private fun getProgressIndicator(): LinearProgressIndicator {
        return requireActivity().findViewById(R.id.top_progress_indicator)
    }

    private val progress by lazy {
        getProgressIndicator()
    }

    private fun getBottomNavigationView(): BottomNavigationView {
        return requireActivity().findViewById(R.id.nav_view)
    }

    private val detailViewItemClicked: (QrCodeItem) -> Unit = {
        activityViewModel.setDetailViewItem(it)
        findNavController().navigate(
            R.id.action_qrHistoryFragment_to_detailViewFragment, bundleOf(
                DetailViewFragment.ORIGIN_FLAG to DetailViewFragment.FROM_HISTORY_LIST
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
                "itemPosition" to pos
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
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
            activityViewModel.removeItemSingleLiveDataEvent.observe(viewLifecycleOwner){
                when(it){
                    is State.Error -> {
                        progress.hide()
                    }
                    is State.Loading -> progress.show()
                    is State.Success -> {
                        progress.hide()
                        viewBinding.root.showSnackBar(
                            UiEvent.SnackBar(
                                message = R.string.item_deleted,
                                anchorView = getBottomNavigationView(),
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

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
        super.onPrepareOptionsMenu(menu)
    }

}