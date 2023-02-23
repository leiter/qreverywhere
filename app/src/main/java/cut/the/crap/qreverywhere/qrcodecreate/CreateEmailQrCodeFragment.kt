package cut.the.crap.qreverywhere.qrcodecreate

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.zxing.WriterException
import cut.the.crap.qreverywhere.MainActivityViewModel
import cut.the.crap.qreverywhere.R
import cut.the.crap.qreverywhere.data.State
import cut.the.crap.qreverywhere.databinding.FragmentCreateEmailQrCodeBinding
import cut.the.crap.qreverywhere.qrdelegates.ImeActionDelegate
import cut.the.crap.qreverywhere.qrdelegates.ImeActionDelegateImpl
import cut.the.crap.qreverywhere.utils.FROM_CREATE_CONTEXT
import cut.the.crap.qreverywhere.utils.ORIGIN_FLAG
import cut.the.crap.qreverywhere.utils.UiEvent
import cut.the.crap.qreverywhere.utils.gone
import cut.the.crap.qreverywhere.utils.showSnackBar
import cut.the.crap.qreverywhere.utils.textChanges
import cut.the.crap.qreverywhere.utils.viewBinding
import cut.the.crap.qreverywhere.utils.visible
import cut.the.crap.qrrepository.QrItem
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@AndroidEntryPoint
class CreateEmailQrCodeFragment : Fragment(R.layout.fragment_create_email_qr_code),
    ImeActionDelegate by ImeActionDelegateImpl() {

    private val bottomNav by lazy {
        requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)
    }

    private val openImeAction: () -> Unit = {
        bottomNav.gone()
    }

    private val closeImeAction: () -> Unit = {
        Handler(Looper.getMainLooper()).postDelayed({
            bottomNav.visible()
        },120)
    }

    private fun getProgressIndicator(): LinearProgressIndicator {
        return requireActivity().findViewById(R.id.top_progress_indicator)
    }

    private val progress by lazy {
        getProgressIndicator()
    }

    private val viewModel by viewModels<CreateQrCodeViewModel>()

    private val activityViewModel: MainActivityViewModel by activityViewModels()

    private val viewBinding by viewBinding {
        FragmentCreateEmailQrCodeBinding.bind(requireView())
    }

    private fun observeViewModel() {
        with(viewBinding) {
            viewModel.emailQrCodeItem.observe(viewLifecycleOwner) {
                when (it) {
                    is State.Loading<QrItem> -> {
                        progress.show()
                        createEmailAddressTextLayout.error = null
                    }
                    is State.Success<QrItem> -> {
                        if (it.data != null) {
                            createEmailQrImagePreview.setImageBitmap(it.data.img)
                        } else {
                            root.showSnackBar(
                                UiEvent.SnackBar(
                                    message = R.string.saved_in_history,
                                    anchorView = bottomNav
                                )
                            )
                        }
                        createEmailHeaderGroup.visibility = View.VISIBLE
                        progress.hide()
                    }
                    is State.Error -> {
                        progress.hide()
                        when (it.cause) {
                            is InvalidEmailException -> createEmailAddressTextLayout.error =
                                getString(R.string.error_invalid_email_address)
                            is WriterException -> {
                                root.showSnackBar(
                                    UiEvent.SnackBar(message = R.string.error_could_not_create_qr_image)
                                )
                            }
                        }
                    }
                }
            }

            activityViewModel.saveDetailViewQrCodeImage.observe(viewLifecycleOwner){
                when (it) {
                    is State.Success<String?> -> {
                        requireView().showSnackBar(
                            UiEvent.SnackBar(
                                message = R.string.saved_as_file,
                                anchorView = viewBinding.createEmailButtonSaveQrToFile
                            )
                        )
                        progress.hide()
                    }
                    is State.Error -> {
                        requireView().showSnackBar(
                            UiEvent.SnackBar(
                                message = R.string.error_saved_as_file,
                                anchorView = viewBinding.createEmailButtonSaveQrToFile,
                                backGroundColor = R.color.teal_700
                            )
                        )
                        progress.hide()
                    }
                    is State.Loading -> progress.show()
                    null -> {

                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        attachImeActionDelegate(this, openImeAction, closeImeAction)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activityViewModel.historyAdapterData.observe(viewLifecycleOwner) {
            if(it.isNotEmpty()) {
                activityViewModel.setDetailViewItem(it[0])
            }
        }

        with(viewBinding) {
            createEmailAddressText.setText(viewModel.emailAddress)
            createEmailSubjectText.setText(viewModel.emailSubject)
            createEmailBodyText.setText(viewModel.emailText)

            lifecycleScope.launchWhenResumed {
                createEmailAddressText.textChanges().collect {
                    viewModel.emailAddress = it.toString()
                }
            }
            lifecycleScope.launchWhenResumed {
                createEmailSubjectText.textChanges().collect {
                    viewModel.emailSubject = it.toString()
                }
            }
            lifecycleScope.launchWhenResumed {
                createEmailBodyText.textChanges().collect {
                    viewModel.emailText = it.toString()
                }
            }
            createEmailCreateQrcode.setOnClickListener {
                viewModel.textToQrCodeItem(resources)
            }
            createEmailButtonSaveQrToFile.setOnClickListener {
                    activityViewModel.saveQrImageOfDetailView(requireContext())
            }
            createEmailQrImagePreview.setOnClickListener {
                findNavController().navigate(
                    R.id.action_createEmailQrCodeFragment_to_qrFullscreenFragment, bundleOf(
                        "itemPosition" to 0,
                        ORIGIN_FLAG to FROM_CREATE_CONTEXT
                    )
                )
            }
        }
        observeViewModel()
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
        super.onPrepareOptionsMenu(menu)
    }
}
