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

package proton.android.pass.autofill.ui.autofill

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.autofill.AutofillManager
import android.widget.RemoteViews
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import proton.android.pass.autofill.DatasetBuilderOptions
import proton.android.pass.autofill.DatasetUtils
import proton.android.pass.autofill.di.UserPreferenceEntryPoint
import proton.android.pass.autofill.entities.AutofillData
import proton.android.pass.autofill.entities.AutofillItem
import proton.android.pass.autofill.entities.AutofillMappings
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.some
import proton.android.pass.common.api.toOption
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.enableEdgeToEdgeProtonPass
import proton.android.pass.commonui.api.setSecureMode
import proton.android.pass.composecomponents.impl.theme.isDark
import proton.android.pass.log.api.PassLogger
import proton.android.pass.preferences.AllowScreenshotsPreference
import proton.android.pass.preferences.ThemePreference

@AndroidEntryPoint
class AutofillActivity : FragmentActivity() {

    private val viewModel: AutofillActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        setSecureMode()
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                window.setHideOverlayWindows(true)
            }
        }
        super.onCreate(savedInstanceState)

        viewModel.register(this)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.stateFlow.collectLatest(::onStateReceived)
            }
        }
    }

    private fun onStateReceived(autofillUiState: AutofillUiState) {
        when (autofillUiState) {
            AutofillUiState.CloseScreen -> {
                PassLogger.i(TAG, "Received AutofillUiState.CloseScreen")
                onAutofillCancel()
            }

            AutofillUiState.NotValidAutofillUiState -> {
                PassLogger.i(TAG, "Received AutofillUiState.NotValidAutofillUiState")
                onAutofillCancel()
            }

            is AutofillUiState.StartAutofillUiState -> {
                enableEdgeToEdgeProtonPass()
                setContent {
                    val isDark = isDark(ThemePreference.from(autofillUiState.themePreference))
                    PassTheme(isDark = isDark) {
                        AutofillApp(
                            autofillUiState = autofillUiState,
                            onNavigate = {
                                when (it) {
                                    AutofillNavigation.Cancel -> onAutofillCancel()
                                    is AutofillNavigation.SendResponse -> onAutofillSuccess(it.mappings)
                                    AutofillNavigation.Upgrade -> viewModel.upgrade()
                                    is AutofillNavigation.ForceSignOut -> viewModel.signOut(it.userId)
                                }
                            }
                        )
                    }
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
        PassLogger.i(TAG, "Mappings found: ${autofillMappings.mappings.size}")
        val intent = prepareAutofillSuccessIntent(autofillMappings)
        setResult(RESULT_OK, intent)
        finishApp()
    }

    private fun finishApp() {
        finish()
    }

    private fun prepareAutofillSuccessIntent(autofillMappings: AutofillMappings): Intent = Intent().apply {
        // We must send a remote view presentation, otherwise it will crash
        val notUsed = RemoteViews(packageName, android.R.layout.simple_list_item_1)
        val options = DatasetBuilderOptions(
            id = "AutofillActivity".some(),
            remoteViewPresentation = notUsed.some()
        )
        val dataset = DatasetUtils.buildDataset(
            options = options,
            autofillMappings = autofillMappings.toOption()
        )
        putExtra(AutofillManager.EXTRA_AUTHENTICATION_RESULT, dataset)
    }

    private fun setSecureMode() {
        val factory =
            EntryPointAccessors.fromApplication(this, UserPreferenceEntryPoint::class.java)
        val repository = factory.getRepository()
        val setting = runBlocking {
            repository.getAllowScreenshotsPreference()
                .firstOrNull()
                ?: AllowScreenshotsPreference.Disabled
        }
        setSecureMode(setting)
    }

    companion object {

        private const val TAG = "AutofillActivity"

        fun newIntent(
            context: Context,
            data: AutofillData,
            autofillItem: Option<AutofillItem> = None
        ): Intent = Intent(context, AutofillActivity::class.java).apply {
            val extras = AutofillIntentExtras.toExtras(data, autofillItem)

            putExtras(extras)
            setPackage(context.packageName)
        }
    }
}
