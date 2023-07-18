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

package proton.android.pass.autofill.ui.autosave

import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.domain.AccountManager
import proton.android.pass.account.api.AccountOrchestrators
import proton.android.pass.account.api.Orchestrator
import proton.android.pass.autofill.api.AutofillManager
import proton.android.pass.autofill.service.R
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.flatMap
import proton.android.pass.common.api.some
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.ToastManager
import proton.android.pass.preferences.HasAuthenticated
import proton.android.pass.preferences.InternalSettingsRepository
import proton.android.pass.preferences.UserPreferencesRepository
import javax.inject.Inject

@HiltViewModel
class AutosaveActivityViewModel @Inject constructor(
    private val accountOrchestrators: AccountOrchestrators,
    private val preferenceRepository: UserPreferencesRepository,
    private val internalSettingsRepository: InternalSettingsRepository,
    private val accountManager: AccountManager,
    private val toastManager: ToastManager,
    private val autofillManager: AutofillManager
) : ViewModel() {

    private val eventFlow: MutableStateFlow<Option<AutosaveEvent>> = MutableStateFlow(None)
    val state: StateFlow<Option<AutosaveEvent>> = eventFlow

    fun register(context: ComponentActivity) {
        accountOrchestrators.register(context, listOf(Orchestrator.PlansOrchestrator))
    }

    fun upgrade() = viewModelScope.launch {
        accountOrchestrators.start(Orchestrator.PlansOrchestrator)
    }

    fun onStop() = viewModelScope.launch {
        preferenceRepository.setHasAuthenticated(HasAuthenticated.NotAuthenticated)
    }

    fun signOut() = viewModelScope.launch {
        val primaryUserId = accountManager.getPrimaryUserId().firstOrNull()
        if (primaryUserId != null) {
            accountManager.removeAccount(primaryUserId)
            toastManager.showToast(R.string.autofill_user_logged_out)
        }
        preferenceRepository.clearPreferences()
            .flatMap { internalSettingsRepository.clearSettings() }
            .onSuccess { PassLogger.d(TAG, "Clearing preferences success") }
            .onFailure {
                PassLogger.w(TAG, it, "Error clearing preferences")
            }

        autofillManager.disableAutofill()

        eventFlow.update { AutosaveEvent.Close.some() }
    }

    companion object {
        private const val TAG = "AutosaveActivityViewModel"
    }
}

sealed interface AutosaveEvent {
    object Close : AutosaveEvent
}
