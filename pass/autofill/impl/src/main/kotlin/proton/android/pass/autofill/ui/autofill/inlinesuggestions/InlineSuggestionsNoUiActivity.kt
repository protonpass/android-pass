/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.autofill.ui.autofill.inlinesuggestions

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.autofill.AutofillManager
import android.widget.RemoteViews
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import proton.android.pass.autofill.DatasetBuilderOptions
import proton.android.pass.autofill.DatasetUtils
import proton.android.pass.autofill.entities.AutofillData
import proton.android.pass.autofill.entities.AutofillItem
import proton.android.pass.autofill.entities.AutofillMappings
import proton.android.pass.autofill.ui.autofill.AutofillIntentExtras
import proton.android.pass.common.api.some
import proton.android.pass.common.api.toOption

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

    private fun prepareAutofillSuccessIntent(autofillMappings: AutofillMappings): Intent =
        Intent().apply {
            // We must send a remote view presentation, otherwise it will crash
            val notUsed = RemoteViews(packageName, android.R.layout.simple_list_item_1)
            val options = DatasetBuilderOptions(
                id = "InlineSuggestionsNoUiActivity".some(),
                remoteViewPresentation = notUsed.some()
            )
            val dataset = DatasetUtils.buildDataset(
                options = options,
                autofillMappings = autofillMappings.toOption()
            )
            putExtra(AutofillManager.EXTRA_AUTHENTICATION_RESULT, dataset)
        }

    companion object {

        fun newIntent(
            context: Context,
            data: AutofillData,
            autofillItem: AutofillItem
        ): Intent {
            val extras = AutofillIntentExtras.toExtras(data, autofillItem.some())
            val intent = Intent(context, InlineSuggestionsNoUiActivity::class.java)
            intent.putExtras(extras)
            return intent
        }
    }
}
