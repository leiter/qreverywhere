package cut.the.crap.qreverywhere.qrcodecreate

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.zxing.WriterException
import cut.the.crap.qreverywhere.R
import cut.the.crap.qreverywhere.databinding.FragmentCreateEmailQrCodeBinding
import cut.the.crap.qreverywhere.viewBinding

class CreateEmailQrCodeFragment : Fragment(R.layout.fragment_create_email_qr_code) {


    private val viewModel by viewModels<CreateQrCodeViewModel>()

    private val viewBinding by viewBinding {
        FragmentCreateEmailQrCodeBinding.bind(requireView())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(viewBinding) {

            btn.setOnClickListener {

                if (etqr.text.toString().trim().isEmpty()) {
                    Toast.makeText(requireContext(), "Enter String!", Toast.LENGTH_SHORT).show()
                } else {
                    try {
                        Log.e("TEXTLENGTH", "${etqr.text.length}")
                        val bitmap = viewModel.textToImageEncode(etqr.text.toString(), resources)!!
                        iv.setImageBitmap(bitmap)
//                        val path: String? = viewModel.saveImage(bitmap, requireContext())
//                        Toast.makeText(
//                            requireContext(),
//                            "QRCode saved to -> $path",
//                            Toast.LENGTH_SHORT
//                        ).show()
                    } catch (e: WriterException) {
                        e.printStackTrace()
                    }
                }
            }

        }
    }

    companion object {

    }
}