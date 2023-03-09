package cut.the.crap.qreverywhere.qrcodecreate

import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
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
import cut.the.crap.qreverywhere.utils.FROM_CREATE_CONTEXT
import cut.the.crap.qreverywhere.utils.IntentGenerator
import cut.the.crap.qreverywhere.utils.ORIGIN_FLAG
import cut.the.crap.qreverywhere.utils.ui.activityView
import cut.the.crap.qreverywhere.utils.ui.focusEditText
import cut.the.crap.qreverywhere.utils.ui.setTitle
import cut.the.crap.qreverywhere.utils.startIntentGracefully
import cut.the.crap.qreverywhere.utils.ui.UiEvent
import cut.the.crap.qreverywhere.utils.ui.gone
import cut.the.crap.qreverywhere.utils.ui.hideKeyboardInput
import cut.the.crap.qreverywhere.utils.ui.showSnackBar
import cut.the.crap.qreverywhere.utils.ui.textChanges
import cut.the.crap.qreverywhere.utils.ui.visible
import cut.the.crap.qreverywhere.utils.ui.viewBinding
import cut.the.crap.qrrepository.QrItem
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@AndroidEntryPoint
class CreateOneLinerFragment : Fragment(R.layout.fragment_create_one_liner),

    ImeActionDelegate by ImeActionDelegateImpl() {

    private val args: CreateOneLinerFragmentArgs by navArgs()

    private val viewBinding by viewBinding { FragmentCreateOneLinerBinding.bind(requireView()) }

    private val viewModel by viewModels<CreateOneLinerViewModel>()

    private val activityViewModel: MainActivityViewModel by viewModels()

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

    override fun onAttach(context: Context) {
        super.onAttach(context)
        attachImeActionDelegate(this, openImeAction, closeImeAction)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        when (args.useCaseMode) {
            CREATE_PHONE -> setupCreateCallQrcode()
            CREATE_SMS -> setupCreateTextQrcode()
            CREATE_WEB -> setupCreateWebQrcode()
            else -> throw IllegalArgumentException("No fragment associated with this id=${args.useCaseMode}")
        }

        with(viewBinding) {
            createOneLinerTest.setOnClickListener {
                hideKeyboardInput()
                viewModel.testClicked(args.useCaseMode, resources)
            }

            createOneLinerCreate.setOnClickListener {
                hideKeyboardInput()
                viewModel.createClicked(args.useCaseMode, resources, activityViewModel)
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
                    is State.Success -> {
                        showLoading(false)
                        state.data?.let {
                            IntentGenerator.QrStartIntent(it.textContent).getIntent().startIntentGracefully(requireContext())
                        } ?: runCatching {
                            viewBinding.createOneLinerNumberInputField.setText("")
                            viewBinding.createOneLinerInputField.setText("")
                            viewBinding.createOneLinerHeaderGroup.visible()
                            viewBinding.createOneLinerQrImagePreview.setOnClickListener {
                                findNavController().navigate(
                                    R.id.action_createOneLinerFragment_to_qrFullscreenFragment2,
                                    bundleOf(
                                        "itemPosition" to 0,
                                        ORIGIN_FLAG to FROM_CREATE_CONTEXT
                                    )
                                )
                            }
                            viewBinding.createOneLinerQrImagePreview.setImageBitmap(
                                activityViewModel.detailViewQrCodeItem.img
                            )
                            viewBinding.createOneLinerButtonSaveQrToFile.setOnClickListener {
                                activityViewModel.saveQrImageOfDetailView(requireContext())
                            }
                            val anchor = activityView<BottomNavigationView>(R.id.nav_view)
                            viewBinding.root.showSnackBar(
                                UiEvent.SnackBar(
                                    message = R.string.saved_in_history,
                                    anchorView = anchor,
                                    actionTextColor = R.color.accent,
                                    actionLabel = R.string.undo_delete,
                                )
                            )
                        }
                    }
                    is State.Error -> {
                        handleError(state)
                        showLoading(false)
                    }

                    is State.Loading -> {
                        resetError()
                        showLoading(true)
                    }
                }
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
            is NoTextInput -> viewBinding.createOneLinerInputLayout.error =
                getString(R.string.error_msg_invalide_phone_number)
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
            focusEditText(createOneLinerNumberInputField)
        }
    }

    private fun setupCreateTextQrcode() {
        setTitle(R.string.create_title_sms)
        with(viewBinding) {
            createOneLinerNumberInputLayout.gone()
            createOneLinerTest.gone()
            val params = createOneLinerInputLayout.layoutParams as ConstraintLayout.LayoutParams
            params.matchConstraintMinHeight =
                (160 * Resources.getSystem().displayMetrics.density).toInt()
            createOneLinerInputLayout.layoutParams = params
            createOneLinerInputLayout.setHint(R.string.create_one_liner_input_layout_hint_text_message)
            focusEditText(createOneLinerInputField)
        }
    }

    private fun setupCreateWebQrcode() {
        setTitle(R.string.create_title_web)
        with(viewBinding) {
            createOneLinerNumberInputLayout.gone()
            createOneLinerInputLayout.setHint(R.string.create_one_liner_input_layout_hint_web)
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