package cut.the.crap.qreverywhere.qrcodecreate

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.progressindicator.LinearProgressIndicator
import cut.the.crap.qreverywhere.MainActivityViewModel
import cut.the.crap.qreverywhere.R
import cut.the.crap.qreverywhere.data.State
import cut.the.crap.qreverywhere.databinding.FragmentCreateOneLinerBinding
import cut.the.crap.qreverywhere.qrdelegates.ImeActionDelegate
import cut.the.crap.qreverywhere.qrdelegates.ImeActionDelegateImpl
import cut.the.crap.qreverywhere.utils.data.IntentGenerator
import cut.the.crap.qreverywhere.utils.ui.FROM_CREATE_CONTEXT
import cut.the.crap.qreverywhere.utils.ui.ORIGIN_FLAG
import cut.the.crap.qreverywhere.utils.ui.UiEvent
import cut.the.crap.qreverywhere.utils.ui.activityView
import cut.the.crap.qreverywhere.utils.ui.clipBoard
import cut.the.crap.qreverywhere.utils.ui.focusEditText
import cut.the.crap.qreverywhere.utils.ui.gone
import cut.the.crap.qreverywhere.utils.ui.hideKeyboardInput
import cut.the.crap.qreverywhere.utils.ui.pasteFromClipBoard
import cut.the.crap.qreverywhere.utils.ui.setTitle
import cut.the.crap.qreverywhere.utils.ui.showShortToast
import cut.the.crap.qreverywhere.utils.ui.showSnackBar
import cut.the.crap.qreverywhere.utils.ui.startIntentGracefully
import cut.the.crap.qreverywhere.utils.ui.textChanges
import cut.the.crap.qreverywhere.utils.ui.viewBinding
import cut.the.crap.qreverywhere.utils.ui.visible
import cut.the.crap.qrrepository.QrItem
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import timber.log.Timber

@AndroidEntryPoint
class CreateOneLinerFragment : Fragment(R.layout.fragment_create_one_liner),

    ImeActionDelegate by ImeActionDelegateImpl() {

    private val args: CreateOneLinerFragmentArgs by navArgs()

    private val viewBinding by viewBinding { FragmentCreateOneLinerBinding.bind(requireView()) }

    private val viewModel by viewModels<CreateOneLinerViewModel>()

    private val activityViewModel: MainActivityViewModel by activityViewModels()

    private val bottomNav by lazy {
        activityView<BottomNavigationView>(R.id.nav_view)
    }

    private val progress by lazy {
        activityView<LinearProgressIndicator>(R.id.top_progress_indicator)
    }

    private val openImeAction: () -> Unit = {
        bottomNav.gone()
    }

    private val closeImeAction: () -> Unit = {
        Handler(Looper.getMainLooper()).postDelayed({
            bottomNav.visible()
        }, 120)
    }

    private val clip by clipBoard()

    private val messageClipboardEmpty = {
        requireContext().showShortToast(R.string.toast_clipboard_empty)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        attachImeActionDelegate(this, openImeAction, closeImeAction)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(viewBinding) {
            createOneLinerTest.setOnClickListener {
                hideKeyboardInput()
                viewModel.testClicked(args.useCaseMode)
            }

            createOneLinerCreate.setOnClickListener {
                hideKeyboardInput()
                viewModel.createClicked(args.useCaseMode, activityViewModel)
            }
        }

        lifecycleScope.launchWhenResumed {
            if (args.useCaseMode == CREATE_SMS || args.useCaseMode == CREATE_PHONE) {
                viewBinding.createOneLinerNumberInputField.textChanges().collect {
                    viewModel.currentInputNumber = it.toString()
                }
            }
        }
        lifecycleScope.launchWhenResumed {
            viewBinding.createOneLinerInputField.textChanges()
                .collect {
                    viewModel.currentInputText = it.toString()
                }
        }

        activityViewModel.saveDetailViewQrCodeImage.observe(viewLifecycleOwner) {
            when (it) {
                is State.Success<String?> -> {
                    requireView().showSnackBar(
                        UiEvent.SnackBar(
                            message = R.string.saved_as_file,
                            anchorView = bottomNav
                        )
                    )
                    progress.hide()
                }
                is State.Error -> {
                    requireView().showSnackBar(
                        UiEvent.SnackBar(
                            message = R.string.error_saved_as_file,
                            anchorView = bottomNav,
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

        viewModel.qrCodeItemState.observe(viewLifecycleOwner) { state ->
            if (state != null)
                when (state) {
                    is State.Loading -> {
                        resetError()
                        showLoading(true)
                    }
                    is State.Success -> {
                        showLoading(false)
                        state.data?.let {
                            requireContext().startIntentGracefully(IntentGenerator.QrStartIntent(it.textContent).getIntent())
                        } ?: runCatching {
                            viewBinding.createOneLinerNumberInputField.setText("")
                            viewBinding.createOneLinerInputField.setText("")

                            val anchor = activityView<BottomNavigationView>(R.id.nav_view)
                            viewBinding.root.showSnackBar(
                                UiEvent.SnackBar(
                                    message = R.string.saved_in_history,
                                    anchorView = anchor,
                                    actionTextColor = R.color.accent,
                                    actionLabel = R.string.undo_delete,
                                )
                            )
                            findNavController().navigate(
                                R.id.action_createOneLinerFragment_to_detailViewFragment2,
                                bundleOf(ORIGIN_FLAG to FROM_CREATE_CONTEXT)
                            )
                        }
                    }
                    is State.Error -> {
                        handleError(state)
                        showLoading(false)
                    }
                }
        }

    }

    override fun onResume() {
        super.onResume()
        when (args.useCaseMode) {
            CREATE_PHONE -> setupCreateCallQrcode()
            CREATE_SMS -> setupCreateTextQrcode()
            CREATE_WEB -> setupCreateWebQrcode()
            else -> Timber.d("No use case of mode ${args.useCaseMode}")
        }
    }

    private fun resetError() {
        viewBinding.createOneLinerNumberInputLayout.error = null
        viewBinding.createOneLinerInputLayout.error = null
    }

    private fun handleError(error: State.Error<QrItem>) {
        when (error.cause) {
            is EmptyMessage -> viewBinding.createOneLinerInputLayout.error =
                getString(R.string.error_msg_empty_text_message)
            is InvalidPhoneNumber -> viewBinding.createOneLinerNumberInputLayout.error =
                getString(R.string.error_msg_invalide_phone_number)
            is InvalidWebUrl -> viewBinding.createOneLinerInputLayout.error =
                getString(R.string.error_msg_invalide_web_adress)
        }
    }

    private fun showLoading(show: Boolean) {
        if (show) {
            progress.visible()
        } else {
            progress.hide()
        }
    }

    private fun setupCreateCallQrcode() {
        setTitle(R.string.create_title_phone)
        with(viewBinding) {
            createOneLinerInputLayout.gone()
            createOneLinerNumberInputLayout.setStartIconOnClickListener {
                createOneLinerNumberInputField.pasteFromClipBoard(clip, messageClipboardEmpty)
            }
            focusEditText(createOneLinerNumberInputField)
        }
    }

    private fun setupCreateTextQrcode() {
        setTitle(R.string.create_title_sms)
        with(viewBinding) {
            createOneLinerNumberInputLayout.gone()
            createOneLinerTest.gone()
            createOneLinerInputLayout.setHint(R.string.create_one_liner_input_layout_hint_text_message)
            createOneLinerInputLayout.setStartIconOnClickListener {
                createOneLinerInputField.pasteFromClipBoard(clip, messageClipboardEmpty)
            }
            focusEditText(createOneLinerInputField)
        }
    }


    private fun setupCreateWebQrcode() {
        setTitle(R.string.create_title_web)
        with(viewBinding) {
            createOneLinerNumberInputLayout.gone()
            createOneLinerInputLayout.setHint(R.string.create_one_liner_input_layout_hint_web)
            createOneLinerInputLayout.setStartIconOnClickListener {
                createOneLinerInputField.pasteFromClipBoard(clip, messageClipboardEmpty)
            }
            focusEditText(createOneLinerInputField)
        }
    }

    companion object {
        const val USE_CASE_MODE = "useCaseMode"
        const val CREATE_SMS = 0
        const val CREATE_PHONE = 1
        const val CREATE_WEB = 2
    }

}