package proton.android.pass.autofill.ui.autofill

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.autofill.AutofillManager
import android.widget.RemoteViews
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.os.bundleOf
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import proton.android.pass.autofill.DatasetBuilderOptions
import proton.android.pass.autofill.DatasetUtils
import proton.android.pass.autofill.Utils
import proton.android.pass.autofill.entities.AutofillData
import proton.android.pass.autofill.entities.AutofillMappings
import proton.android.pass.autofill.entities.asAndroid
import proton.android.pass.autofill.extensions.marshalParcelable
import proton.android.pass.autofill.extensions.toAutofillItem
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.toOption
import proton.pass.domain.Item

@AndroidEntryPoint
class AutofillActivity : FragmentActivity() {

    private val viewModel: AutofillActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collectLatest(::onStateReceived)
            }
        }
    }

    private fun onStateReceived(autofillUiState: AutofillUiState) {
        when (autofillUiState) {
            AutofillUiState.NotValidAutofillUiState -> onAutofillCancel()
            is AutofillUiState.StartAutofillUiState -> {
                WindowCompat.setDecorFitsSystemWindows(window, false)
                setContent {
                    AutofillApp(
                        autofillUiState = autofillUiState,
                        onAutofillSuccess = ::onAutofillSuccess,
                        onAutofillCancel = ::onAutofillCancel
                    )
                }
            }
            AutofillUiState.UninitialisedAutofillUiState -> {}
        }
    }

    private fun onAutofillCancel() {
        setResult(RESULT_CANCELED)
        finishApp()
    }

    private fun onAutofillSuccess(autofillMappings: AutofillMappings) {
        val intent = prepareAutofillSuccessIntent(autofillMappings)
        setResult(RESULT_OK, intent)
        finishApp()
    }

    private fun finishApp() {
        finish()
    }

    private fun prepareAutofillSuccessIntent(autofillMappings: AutofillMappings): Intent {
        val remoteView = RemoteViews(packageName, android.R.layout.simple_list_item_1).toOption()
        val datasetBuilderOptions = DatasetBuilderOptions(
            // Autofill presentations cannot be empty on 33, or it will throw an IllegalStateException
            authenticateView = remoteView
        )

        val res = Intent()
        if (!autofillMappings.mappings.isEmpty()) {
            val dataset = DatasetUtils.buildDataset(
                context = this,
                dsbOptions = datasetBuilderOptions,
                autofillMappings = autofillMappings.toOption(),
                assistFields = emptyList()
            )
            res.putExtra(AutofillManager.EXTRA_AUTHENTICATION_RESULT, dataset)
        }

        return res
    }

    companion object {
        const val ARG_AUTOFILL_IDS = "arg_autofill_ids"
        const val ARG_AUTOFILL_TYPES = "arg_autofill_types"
        const val ARG_PACKAGE_NAME = "arg_package_name"
        const val ARG_APP_NAME = "arg_app_name"
        const val ARG_WEB_DOMAIN = "arg_web_domain"
        const val ARG_TITLE = "arg_title"
        const val ARG_INLINE_SUGGESTION_AUTOFILL_ITEM = "arg_inline_suggestion_autofill_item"

        fun newIntent(
            context: Context,
            data: AutofillData,
            itemOption: Option<Item> = None
        ): Intent =
            Intent(context, AutofillActivity::class.java).apply {
                if (data.assistInfo.url is Some) {
                    putExtra(ARG_WEB_DOMAIN, data.assistInfo.url.value)
                }
                val fields = data.assistInfo.fields
                putExtras(
                    bundleOf(
                        ARG_AUTOFILL_IDS to fields.map { it.id.asAndroid().autofillId },
                        ARG_AUTOFILL_TYPES to fields.map { it.type?.toString() },
                        ARG_PACKAGE_NAME to data.packageInfo.map { it.packageName.value }.value(),
                        ARG_APP_NAME to data.packageInfo.map { it.appName.value }.value(),
                        ARG_TITLE to Utils.getTitle(
                            data.assistInfo.url,
                            data.packageInfo.map { it.appName.value }
                        )
                    )
                )
                if (itemOption is Some) {
                    val autofillItem = itemOption.value.toAutofillItem()
                    if (autofillItem is Some) {
                        putExtra(
                            ARG_INLINE_SUGGESTION_AUTOFILL_ITEM,
                            marshalParcelable(autofillItem.value)
                        )
                    }
                }
            }
    }
}
