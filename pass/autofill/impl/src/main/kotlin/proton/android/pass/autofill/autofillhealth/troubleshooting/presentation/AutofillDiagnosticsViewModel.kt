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

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import proton.android.pass.autofill.api.AutofillManager
import proton.android.pass.autofill.api.AutofillStatus
import proton.android.pass.autofill.api.AutofillSupportedStatus
import proton.android.pass.autofill.autofillhealth.service.AutofillHealthMonitor
import proton.android.pass.preferences.FeatureFlag
import proton.android.pass.preferences.FeatureFlagsPreferencesRepository
import javax.inject.Inject

@HiltViewModel
class AutofillDiagnosticsViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val autofillManager: AutofillManager,
    private val healthMonitor: AutofillHealthMonitor,
    private val featureFlagsRepository: FeatureFlagsPreferencesRepository
) : ViewModel() {

    val state: StateFlow<AutofillDiagnosticsState> = combine(
        featureFlagsRepository.get<Boolean>(FeatureFlag.AUTOFILL_DEBUG_MODE),
        autofillManager.getAutofillStatus(),
        healthMonitor.events
    ) { isEnabled, autofillStatus, events ->
        val attempts = AutofillAttemptResolver.resolve(events, ::resolveAppLabel)
        AutofillDiagnosticsState(
            isDiagnosticsEnabled = isEnabled,
            serviceStatus = autofillStatus.toServiceStatus(),
            lastAttempt = attempts.firstOrNull(),
            recentAttempts = attempts
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = AutofillDiagnosticsState.Initial
    )

    fun setDiagnosticsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            featureFlagsRepository.set(FeatureFlag.AUTOFILL_DEBUG_MODE, enabled)
        }
    }

    fun clearAttempts() {
        healthMonitor.clearLog()
    }

    private fun resolveAppLabel(packageName: String?): String {
        if (packageName.isNullOrBlank()) return ""
        return runCatching {
            val info = context.packageManager.getApplicationInfo(packageName, 0)
            context.packageManager.getApplicationLabel(info).toString()
        }.getOrDefault(packageName)
    }

    private fun AutofillSupportedStatus.toServiceStatus(): AutofillServiceStatus = when (this) {
        AutofillSupportedStatus.Unsupported -> AutofillServiceStatus.Unsupported
        is AutofillSupportedStatus.Supported -> when (status) {
            AutofillStatus.EnabledByOurService -> AutofillServiceStatus.EnabledByOurService
            AutofillStatus.Disabled,
            AutofillStatus.EnabledByOtherService -> AutofillServiceStatus.NotDefault
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
