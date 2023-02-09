package proton.android.pass.autofill.ui.autofill.inlinesuggestions

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.autofill.AutofillManager
import android.widget.RemoteViews
import androidx.activity.viewModels
import androidx.core.os.bundleOf
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
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.toOption
import proton.pass.domain.Item

@AndroidEntryPoint
class InlineSuggestionsNoUiActivity : FragmentActivity() {

    private val viewModel: InlineSuggestionsActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collectLatest(::onStateReceived)
            }
        }
    }

    private fun onStateReceived(state: InlineSuggestionAutofillNoUiState) {
        when (state) {
            InlineSuggestionAutofillNoUiState.Error -> onAutofillError()
            InlineSuggestionAutofillNoUiState.NotInitialised -> {}
            is InlineSuggestionAutofillNoUiState.Success -> onAutofillSuccess(state.autofillMappings)
        }
    }

    private fun onAutofillError() {
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
        val dataset = DatasetUtils.buildDataset(
            context = this,
            dsbOptions = datasetBuilderOptions,
            autofillMappings = autofillMappings.toOption(),
            assistFields = emptyList()
        )
        return Intent().apply {
            putExtra(AutofillManager.EXTRA_AUTHENTICATION_RESULT, dataset)
        }
    }

    companion object {
        const val ARG_AUTOFILL_IDS = "arg_autofill_ids"
        const val ARG_AUTOFILL_TYPES = "arg_autofill_types"
        const val ARG_PACKAGE_NAME = "arg_package_name"
        const val ARG_WEB_DOMAIN = "arg_web_domain"
        const val ARG_TITLE = "arg_title"
        const val ARG_INLINE_SUGGESTION_AUTOFILL_ITEM = "arg_inline_suggestion_autofill_item"

        fun newIntent(
            context: Context,
            data: AutofillData,
            item: Item
        ): Intent = Intent(context, InlineSuggestionsNoUiActivity::class.java).apply {
            if (data.assistInfo.url is Some) {
                putExtra(ARG_WEB_DOMAIN, data.assistInfo.url.value)
            }
            val fields = data.assistInfo.fields
            putExtras(
                bundleOf(
                    ARG_AUTOFILL_IDS to fields.map { it.id.asAndroid().autofillId },
                    ARG_AUTOFILL_TYPES to fields.map { it.type?.toString() },
                    ARG_PACKAGE_NAME to data.packageName.value(),
                    ARG_TITLE to Utils.getTitle(context, data.assistInfo.url, data.packageName)
                )
            )
            val autofillItem = item.toAutofillItem()
            if (autofillItem is Some) {
                putExtra(
                    ARG_INLINE_SUGGESTION_AUTOFILL_ITEM,
                    marshalParcelable(autofillItem.value)
                )
            }
        }
    }
}
