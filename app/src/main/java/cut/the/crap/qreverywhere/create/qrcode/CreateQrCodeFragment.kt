package cut.the.crap.qreverywhere.create.qrcode

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.zxing.WriterException
import cut.the.crap.qreverywhere.R
import cut.the.crap.qreverywhere.databinding.FragmentCreateQrCodeBinding
import cut.the.crap.qreverywhere.viewBinding

//  createQREmail    createQRUrl    createPhoneCall     readFromFile    readWithCamera


class CreateQrCodeFragment : Fragment(R.layout.fragment_create_qr_code) {

    private val viewModel by viewModels<CreateQrCodeViewModel>()

    private val viewBinding: FragmentCreateQrCodeBinding by viewBinding {
        FragmentCreateQrCodeBinding.bind(requireView())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(viewBinding){

            btn.setOnClickListener {

                if (etqr.text.toString().trim().isEmpty()) {
                    Toast.makeText(requireContext(), "Enter String!", Toast.LENGTH_SHORT).show()
                } else {
                    try {
                        Log.e("TEXTLENGTH", "${etqr.text.length}")
                        val bitmap = viewModel.textToImageEncode(etqr.text.toString(), resources)!!
                        iv.setImageBitmap(bitmap)
                        val path: String? = viewModel.saveImage(bitmap, requireContext()) //give read write permission
                        Toast.makeText(
                            requireContext(),
                            "QRCode saved to -> $path",
                            Toast.LENGTH_SHORT
                        ).show()
                    } catch (e: WriterException) {
                        e.printStackTrace()
                    }
                }
            }

        }


    }

}