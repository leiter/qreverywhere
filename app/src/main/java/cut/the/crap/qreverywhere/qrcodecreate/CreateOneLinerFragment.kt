package cut.the.crap.qreverywhere.qrcodecreate

import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomnavigation.BottomNavigationView
import cut.the.crap.qreverywhere.MainActivityViewModel
import cut.the.crap.qreverywhere.R
import cut.the.crap.qreverywhere.data.State
import cut.the.crap.qreverywhere.databinding.FragmentCreateOneLinerBinding
import cut.the.crap.qreverywhere.db.QrCodeItem
import cut.the.crap.qreverywhere.qrdelegates.ImeActionDelegate
import cut.the.crap.qreverywhere.qrdelegates.ImeActionDelegateImpl
import cut.the.crap.qreverywhere.utils.*
import cut.the.crap.qreverywhere.utils.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class CreateOneLinerFragment : Fragment(R.layout.fragment_create_one_liner),

    ImeActionDelegate by ImeActionDelegateImpl() {

    private val args: CreateOneLinerFragmentArgs by navArgs()

    private val viewBinding by viewBinding { FragmentCreateOneLinerBinding.bind(requireView()) }

    private val viewModel by viewModels<CreateOneLinerViewModel>()

    private val activityViewModel: MainActivityViewModel by viewModels()

    private fun getBottomNavigationView(): BottomNavigationView {
        return requireActivity().findViewById(R.id.nav_view)
    }

    private val bottomNav by lazy {
        getBottomNavigationView()
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
                viewBinding.root.hideIme()
                viewModel.testClicked(args.useCaseMode, resources)
            }

            createOneLinerCreate.setOnClickListener {
                viewBinding.root.hideIme()
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

        viewModel.qrCodeItemState.observe(viewLifecycleOwner) { state ->
            if (state != null)
                when (state) {
                    is State.Success -> {
                        showLoading(false)
                        state.data?.let {
                            startActivity(createOpenIntent(it.textContent, requireContext()))
                        } ?: runCatching {
                            viewBinding.createOneLinerNumberInputField.setText("")
                            viewBinding.createOneLinerInputField.setText("")
                            val anchor = getBottomNavigationView()
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

    private fun handleError(error: State.Error<QrCodeItem>) {
        when (error.cause) {
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
            viewBinding.createOnelinerHeaderText.invisible()
            viewBinding.createOneLinerLoading.visible()
        } else {
            viewBinding.createOnelinerHeaderText.visible()
            viewBinding.createOneLinerLoading.invisible()
        }
    }

    private fun setupCreateCallQrcode() {
        setTitle(R.string.create_title_phone)
        with(viewBinding) {
            createOneLinerInputLayout.gone()
            createOnelinerHeaderText.setText(R.string.create_one_liner_header_text_phone)
        }
    }

    private fun setupCreateTextQrcode() {
        setTitle(R.string.create_title_sms)
        with(viewBinding) {
            createOneLinerNumberInputLayout.gone()
            createOneLinerTest.gone()
            val params = createOneLinerInputLayout.layoutParams as ConstraintLayout.LayoutParams
            params.matchConstraintMinHeight = (200 * Resources.getSystem().displayMetrics.density).toInt()
            createOneLinerInputLayout.layoutParams = params
            createOneLinerInputLayout.setHint(R.string.create_one_liner_input_layout_hint_text_message)
            createOnelinerHeaderText.setText(R.string.create_one_liner_header_text_message_text)
        }
    }

    private fun setupCreateWebQrcode() {
        setTitle(R.string.create_title_web)
        with(viewBinding) {
            createOneLinerNumberInputLayout.gone()
            createOneLinerInputLayout.setHint(R.string.create_one_liner_input_layout_hint_web)
            createOnelinerHeaderText.setText(R.string.create_one_liner_header_text_web)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    companion object {
        const val USE_CASE_MODE = "useCaseMode"
        const val CREATE_SMS = 0
        const val CREATE_PHONE = 1
        const val CREATE_WEB = 2
    }

}