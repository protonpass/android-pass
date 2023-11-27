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

import androidx.activity.ComponentActivity
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.domain.AccountManager
import proton.android.pass.account.api.AccountOrchestrators
import proton.android.pass.account.api.Orchestrator
import proton.android.pass.autofill.entities.AutofillAppState
import proton.android.pass.autofill.entities.AutofillItem
import proton.android.pass.autofill.entities.isValid
import proton.android.pass.autofill.service.R
import proton.android.pass.autofill.ui.autofill.AutofillIntentExtras.ARG_EXTRAS_BUNDLE
import proton.android.pass.autofill.ui.autofill.AutofillUiState.NotValidAutofillUiState
import proton.android.pass.autofill.ui.autofill.AutofillUiState.UninitialisedAutofillUiState
import proton.android.pass.autofill.ui.autofill.AutofillUiState.UpgradeUiState
import proton.android.pass.biometry.NeedsBiometricAuth
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.flatMap
import proton.android.pass.commonui.api.require
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.ToastManager
import proton.android.pass.preferences.HasAuthenticated
import proton.android.pass.preferences.InternalSettingsRepository
import proton.android.pass.preferences.ThemePreference
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.preferences.value
import javax.inject.Inject

@HiltViewModel
class AutofillActivityViewModel @Inject constructor(
    private val accountOrchestrators: AccountOrchestrators,
    private val preferenceRepository: UserPreferencesRepository,
    private val internalSettingsRepository: InternalSettingsRepository,
    private val accountManager: AccountManager,
    private val toastManager: ToastManager,
    private val savedStateHandle: SavedStateHandle,
    needsBiometricAuth: NeedsBiometricAuth
) : ViewModel() {

    private val closeScreenFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)

    private val appInitialState = getAutofillAppState()

    private val copyTotpToClipboardPreferenceState = preferenceRepository
        .getCopyTotpToClipboardEnabled()
        .distinctUntilChanged()

    private val themePreferenceState: Flow<ThemePreference> = preferenceRepository
        .getThemePreference()
        .distinctUntilChanged()

    val state: StateFlow<AutofillUiState> = combine(
        themePreferenceState,
        needsBiometricAuth(),
        copyTotpToClipboardPreferenceState,
        closeScreenFlow
    ) { themePreference, needsAuth, copyTotpToClipboard, closeScreen ->
        if (closeScreen) {
            return@combine AutofillUiState.CloseScreen
        }

        when (appInitialState) {
            State.Unknown -> NotValidAutofillUiState
            State.Upgrade -> UpgradeUiState
            is State.AppState -> {
                val (appState, selectedAutofillItem) = appInitialState
                when {
                    !appInitialState.appState.isValid() -> NotValidAutofillUiState
                    else -> AutofillUiState.StartAutofillUiState(
                        themePreference = themePreference.value(),
                        needsAuth = needsAuth,
                        autofillAppState = appState,
                        copyTotpToClipboardPreference = copyTotpToClipboard.value(),
                        selectedAutofillItem = selectedAutofillItem
                    )
                }
            }
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UninitialisedAutofillUiState
        )

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

        closeScreenFlow.update { true }
    }

    private fun getAutofillAppState(): State =
        when (val mode = savedStateHandle.require<Int>(MODE_AUTOFILL_KEY)) {
            MODE_AUTOFILL -> {
                val extras = AutofillIntentExtras.fromExtras(
                    bundle = savedStateHandle.require(ARG_EXTRAS_BUNDLE)
                )
                State.AppState(
                    appState = AutofillAppState(extras.first),
                    selectedAutofillItem = extras.second
                )
            }
            MODE_UPGRADE -> State.Upgrade
            else -> {
                PassLogger.w(TAG, "Unknown autofill mode [$mode]")
                State.Unknown
            }
        }

    sealed interface State {
        data class AppState(
            val appState: AutofillAppState,
            val selectedAutofillItem: Option<AutofillItem>
        ) : State
        object Upgrade : State
        object Unknown : State
    }

    companion object {
        const val MODE_AUTOFILL = 1
        const val MODE_UPGRADE = 2
        const val MODE_AUTOFILL_KEY = "autofill_mode"

        private const val TAG = "AutofillActivityViewModel"
    }
}
