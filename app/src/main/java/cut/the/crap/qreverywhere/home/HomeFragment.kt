package cut.the.crap.qreverywhere.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import cut.the.crap.qreverywhere.R
import cut.the.crap.qreverywhere.databinding.FragmentHomeBinding
import cut.the.crap.qreverywhere.viewBinding

class HomeFragment : Fragment(R.layout.fragment_home) {

    private val viewBinding by viewBinding {
        FragmentHomeBinding.bind(requireView())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        with(viewBinding) {

        }

    }
}