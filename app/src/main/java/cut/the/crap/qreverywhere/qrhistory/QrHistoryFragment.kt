package cut.the.crap.qreverywhere.qrhistory

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import cut.the.crap.qreverywhere.MainActivityViewModel
import cut.the.crap.qreverywhere.R
import cut.the.crap.qreverywhere.databinding.FragmentQrHistoryBinding
import cut.the.crap.qreverywhere.db.QrCodeItem
import cut.the.crap.qreverywhere.stuff.gone
import cut.the.crap.qreverywhere.stuff.visible
import cut.the.crap.qreverywhere.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class QrHistoryFragment : Fragment(R.layout.fragment_qr_history) {

    private val viewBinding by viewBinding { FragmentQrHistoryBinding.bind(requireView()) }

    private val activityViewModel by activityViewModels<MainActivityViewModel>()

    private val detailViewItemClicked: (QrCodeItem) -> Unit = {
        activityViewModel.setDetailViewItem(it)
        findNavController().navigate(R.id.action_qrHistoryFragment_to_detailViewFragment)
    }

    private val removeHistoryItem: (Int) -> Unit = { pos ->
        activityViewModel.removeHistoryItem(pos)
    }

    private val historyListAdapter by lazy {
        QrHistoryAdapter(requireContext(),detailViewItemClicked)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(viewBinding) {
            val dragManager = ItemDragManager(removeHistoryItem,0, ItemTouchHelper.END or ItemTouchHelper.START)
            ItemTouchHelper(dragManager).attachToRecyclerView(qrHistoryList)
            qrHistoryList.adapter = historyListAdapter
            activityViewModel.historyAdapterData.observe(viewLifecycleOwner){
                if(it.isNotEmpty()){
                    qrHistoryEmptyMessage.gone()
                } else {
                    qrHistoryEmptyMessage.visible()
                }
                historyListAdapter.setData(it)
            }
        }
    }

}