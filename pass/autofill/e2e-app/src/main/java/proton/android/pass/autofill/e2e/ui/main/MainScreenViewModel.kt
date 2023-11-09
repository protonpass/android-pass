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

package proton.android.pass.autofill.e2e.ui.main

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import proton.android.pass.autofill.api.AutofillManager
import proton.android.pass.autofill.api.AutofillStatus
import proton.android.pass.autofill.api.AutofillSupportedStatus
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.common.api.getOrNull
import proton.android.pass.common.api.map
import proton.android.pass.preferences.FeatureFlag
import proton.android.pass.preferences.FeatureFlagsPreferencesRepository
import javax.inject.Inject

@HiltViewModel
class MainScreenViewModel @Inject constructor(
    private val autofillManager: AutofillManager,
    private val preferencesRepository: FeatureFlagsPreferencesRepository
) : ViewModel() {

    val state: StateFlow<MainScreenState> = combine(
        autofillManager.getAutofillStatus().asLoadingResult(),
        preferencesRepository.get<Boolean>(FeatureFlag.AUTOFILL_DEBUG_MODE)
    ) { autofill, debugMode ->
        val isAutofillEnabled = autofill.map {
            when (it) {
                is AutofillSupportedStatus.Supported -> when (it.status) {
                    AutofillStatus.Disabled -> false
                    AutofillStatus.EnabledByOtherService -> false
                    AutofillStatus.EnabledByOurService -> true
                }
                AutofillSupportedStatus.Unsupported -> false
            }
        }.getOrNull() ?: false
        MainScreenState(
            isAutofillEnabled = isAutofillEnabled,
            isDebugModeEnabled = debugMode
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = MainScreenState()
        )

    fun onAutofillChange(newValue: Boolean) = viewModelScope.launch {
        if (newValue) {
            autofillManager.openAutofillSelector()
        } else {
            autofillManager.disableAutofill()
        }
    }

    fun onDebugModeChange(newValue: Boolean) = viewModelScope.launch {
        preferencesRepository.set(FeatureFlag.AUTOFILL_DEBUG_MODE, newValue)
    }
}

@Stable
data class MainScreenState(
    val isAutofillEnabled: Boolean = false,
    val isDebugModeEnabled: Boolean = false
)
