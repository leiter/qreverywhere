package cut.the.crap.qreverywhere.qrcodecreate

import android.os.Bundle
import android.view.Menu
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.zxing.WriterException
import cut.the.crap.qreverywhere.R
import cut.the.crap.qreverywhere.data.State
import cut.the.crap.qreverywhere.databinding.FragmentCreateEmailQrCodeBinding
import cut.the.crap.qreverywhere.db.QrCodeItem
import cut.the.crap.qreverywhere.stuff.UiEvent
import cut.the.crap.qreverywhere.stuff.showSnackBar
import cut.the.crap.qreverywhere.stuff.textChanges
import cut.the.crap.qreverywhere.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class CreateEmailQrCodeFragment : Fragment(R.layout.fragment_create_email_qr_code) {

    private fun getBottomNavigationView(): BottomNavigationView {
        return requireActivity().findViewById(R.id.nav_view)
    }

    private val bottomNav by lazy {
        getBottomNavigationView()
    }

    private fun getProgressIndicator(): LinearProgressIndicator {
        return requireActivity().findViewById(R.id.top_progress_indicator)
    }

    private val progress by lazy {
        getProgressIndicator()
    }

    private val viewModel by viewModels<CreateQrCodeViewModel>()

    private val viewBinding by viewBinding {
        FragmentCreateEmailQrCodeBinding.bind(requireView())
    }

    private fun observeViewModel() {
        with(viewBinding) {
            viewModel.emailQrCodeItem.observe(viewLifecycleOwner) {
                when (it) {
                    is State.Loading<QrCodeItem> -> {
                        progress.show()
//                        createEmailLoader.visibility = View.VISIBLE
                        createEmailAddressTextLayout.error = null
                    }
                    is State.Success<QrCodeItem> -> {
                        if(it.data != null){
                            createEmailQrImagePreview.setImageBitmap(it.data.img)
                        } else {
                            root.showSnackBar(
                                UiEvent.SnackBar(message = R.string.saved_in_history, anchorView = bottomNav)
                            )
                        }
                        createEmailHeaderGroup.visibility = View.VISIBLE
                        progress.hide()
//                        createEmailLoader.visibility = View.INVISIBLE
                    }
                    is State.Error -> {
                        progress.hide()
//                        createEmailLoader.visibility = View.INVISIBLE
                        when (it.cause) {
                            is InvalidEmailException -> createEmailAddressTextLayout.error =
                                getString(R.string.error_invalid_email_address)
                            is WriterException -> {
                                val anchor = bottomNav
                                root.showSnackBar(
                                    UiEvent.SnackBar(message = R.string.error_could_not_create_qr_image)
                                )
                            }
                        }
                    }
                }
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(viewBinding) {

            createEmailAddressText.setText(viewModel.emailAddress)
            createEmailSubjectText.setText(viewModel.emailSubject)
            createEmailBodyText.setText(viewModel.emailText)

            lifecycleScope.launchWhenResumed {
                createEmailAddressText.textChanges().collect {
                    viewModel.emailAddress = it.toString()
                }
                createEmailSubjectText.textChanges().collect {
                    viewModel.emailSubject = it.toString()
                }
                createEmailBodyText.textChanges().collect {
                    viewModel.emailText = it.toString()
                }
            }

            createEmailCreateQrcode.setOnClickListener {
                viewModel.textToQrCodeItem(resources)
            }
            createEmailQrImagePreview.setOnClickListener {
                findNavController().navigate(R.id.action_createEmailQrCodeFragment_to_qrFullscreenFragment, bundleOf(
                    "itemPosition" to 0
                ))
            }

        }
        observeViewModel()
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
        super.onPrepareOptionsMenu(menu)
    }
}
