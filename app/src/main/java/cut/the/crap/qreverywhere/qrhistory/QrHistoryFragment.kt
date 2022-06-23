package cut.the.crap.qreverywhere.qrhistory

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import cut.the.crap.qreverywhere.MainActivityViewModel
import cut.the.crap.qreverywhere.R
import cut.the.crap.qreverywhere.databinding.FragmentQrHistoryBinding
import cut.the.crap.qreverywhere.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class QrHistoryFragment : Fragment(R.layout.fragment_qr_history) {

    private val viewBinding by viewBinding { FragmentQrHistoryBinding.bind(requireView()) }

    private val activityViewModel by activityViewModels<MainActivityViewModel>()

    private val historyListAdapter = QrHistoryAdapter()

//    private val viewModel by viewModels<QrHistoryViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(viewBinding) {
            qrHistoryList.adapter = historyListAdapter
            activityViewModel.historyAdapterData.observe(viewLifecycleOwner){
                historyListAdapter.setData(it)
            }

        }

    }

    companion object {

    }
}