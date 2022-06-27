package cut.the.crap.qreverywhere.qrcodedetailview

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cut.the.crap.qreverywhere.R
import cut.the.crap.qreverywhere.databinding.FragmentDetailViewBinding
import cut.the.crap.qreverywhere.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetailViewFragment : Fragment(R.layout.fragment_detail_view) {


    private val viewBinding by viewBinding {
        FragmentDetailViewBinding.bind(requireView())
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(viewBinding){

        }
    }

}