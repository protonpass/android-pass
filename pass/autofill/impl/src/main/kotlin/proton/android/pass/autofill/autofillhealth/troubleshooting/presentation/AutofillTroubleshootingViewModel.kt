/*
 * Copyright (c) 2026 Proton AG
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

package proton.android.pass.autofill.autofillhealth.troubleshooting.presentation

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.autofill.api.AutofillManager
import proton.android.pass.autofill.api.AutofillStatus
import proton.android.pass.autofill.api.AutofillSupportedStatus
import proton.android.pass.autofill.autofillhealth.troubleshooting.data.CurrentAutofillServiceProvider
import proton.android.pass.autofill.autofillhealth.troubleshooting.data.InstalledBrowsersProvider
import proton.android.pass.appconfig.api.AppConfig
import javax.inject.Inject

@HiltViewModel
class AutofillTroubleshootingViewModel @Inject constructor(
    private val autofillManager: AutofillManager,
    private val installedBrowsersProvider: InstalledBrowsersProvider,
    private val currentAutofillServiceProvider: CurrentAutofillServiceProvider,
    private val appConfig: AppConfig
) : ViewModel() {

    private val stateFlow = MutableStateFlow(AutofillTroubleshootingState.Initial)
    val state: StateFlow<AutofillTroubleshootingState> = stateFlow.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val serviceStatus = autofillManager.getAutofillStatus().first().toServiceStatus()
            val browsers = installedBrowsersProvider.getInstalledBrowsers()
            val conflictingServiceLabel = currentAutofillServiceProvider.getActiveOtherServiceLabel()
            stateFlow.update { current ->
                current.copy(
                    serviceStatus = serviceStatus,
                    installedBrowsers = browsers,
                    supportsInlineSuggestions = appConfig.androidVersion >= Build.VERSION_CODES.R,
                    conflictingServiceLabel = conflictingServiceLabel
                )
            }
        }
    }

    fun openAutofillSelector() {
        autofillManager.openAutofillSelector()
    }

    private fun AutofillSupportedStatus.toServiceStatus(): AutofillServiceStatus = when (this) {
        AutofillSupportedStatus.Unsupported -> AutofillServiceStatus.Unsupported
        is AutofillSupportedStatus.Supported -> when (status) {
            AutofillStatus.EnabledByOurService -> AutofillServiceStatus.EnabledByOurService
            AutofillStatus.Disabled,
            AutofillStatus.EnabledByOtherService -> AutofillServiceStatus.NotDefault
        }
    }
}
