package cut.the.crap.qreverywhere

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import cut.the.crap.qreverywhere.databinding.FragmentReadResultBinding


class ReadResultFragment : Fragment(R.layout.fragment_read_result) {


    private val viewBinding by viewBinding {
        FragmentReadResultBinding.bind(requireView())
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBinding.buttonSecond.setOnClickListener {
//            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
        }
    }


}