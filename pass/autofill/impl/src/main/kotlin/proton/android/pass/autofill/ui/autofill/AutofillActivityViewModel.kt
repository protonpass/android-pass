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

import android.os.Bundle
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
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.domain.getAccounts
import me.proton.core.domain.entity.UserId
import proton.android.pass.account.api.AccountOrchestrators
import proton.android.pass.account.api.Orchestrator
import proton.android.pass.autofill.api.suggestions.PackageNameUrlSuggestionAdapter
import proton.android.pass.autofill.entities.AutofillAppState
import proton.android.pass.autofill.service.R
import proton.android.pass.autofill.ui.autofill.AutofillIntentExtras.ARG_EXTRAS_BUNDLE
import proton.android.pass.autofill.ui.autofill.AutofillUiState.NotValidAutofillUiState
import proton.android.pass.autofill.ui.autofill.AutofillUiState.UninitialisedAutofillUiState
import proton.android.pass.biometry.NeedsBiometricAuth
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.ToastManager
import proton.android.pass.preferences.InternalSettingsRepository
import proton.android.pass.preferences.ThemePreference
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.preferences.value
import javax.inject.Inject

@HiltViewModel
class AutofillActivityViewModel @Inject constructor(
    private val accountOrchestrators: AccountOrchestrators,
    private val accountManager: AccountManager,
    private val toastManager: ToastManager,
    private val internalSettingsRepository: InternalSettingsRepository,
    private val packageNameUrlSuggestionAdapter: PackageNameUrlSuggestionAdapter,
    savedStateHandle: SavedStateHandle,
    userPreferencesRepository: UserPreferencesRepository,
    needsBiometricAuth: NeedsBiometricAuth
) : ViewModel() {

    private val closeScreenFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)

    private val copyTotpToClipboardPreferenceState = userPreferencesRepository
        .getCopyTotpToClipboardEnabled()
        .distinctUntilChanged()

    private val themePreferenceState: Flow<ThemePreference> = userPreferencesRepository
        .getThemePreference()
        .distinctUntilChanged()

    internal val stateFlow: StateFlow<AutofillUiState> = combine(
        themePreferenceState,
        needsBiometricAuth(),
        copyTotpToClipboardPreferenceState,
        closeScreenFlow,
        savedStateHandle.getStateFlow<Bundle?>(ARG_EXTRAS_BUNDLE, null)
    ) { themePreference, needsAuth, copyTotpToClipboard, closeScreen, extrasBundle ->
        when {
            closeScreen -> AutofillUiState.CloseScreen
            extrasBundle == null -> UninitialisedAutofillUiState
            else -> {
                runCatching {
                    val extras = AutofillIntentExtras.fromExtras(extrasBundle)
                    val appState = AutofillAppState(
                        autofillData = extras.first,
                        packageNameUrlSuggestionAdapter = packageNameUrlSuggestionAdapter
                    )

                    if (!appState.isValid()) {
                        NotValidAutofillUiState
                    } else {
                        AutofillUiState.StartAutofillUiState(
                            themePreference = themePreference.value(),
                            needsAuth = needsAuth,
                            autofillAppState = appState,
                            copyTotpToClipboardPreference = copyTotpToClipboard.value(),
                            selectedAutofillItem = extras.second
                        )
                    }
                }.fold(
                    onFailure = {
                        PassLogger.w(TAG, it)
                        PassLogger.w(TAG, "Failed to parse extras bundle")
                        NotValidAutofillUiState
                    },
                    onSuccess = { it }
                )
            }
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UninitialisedAutofillUiState
        )

    internal fun register(context: ComponentActivity) {
        accountOrchestrators.register(context, listOf(Orchestrator.PlansOrchestrator))
    }

    internal fun upgrade() {
        viewModelScope.launch {
            accountOrchestrators.start(Orchestrator.PlansOrchestrator)
        }
    }

    internal fun signOut(userId: UserId) {
        viewModelScope.launch {
            val accounts = accountManager.getAccounts(AccountState.Ready).firstOrNull().orEmpty()
            val hasAccountsLeft = accounts.filterNot { it.userId == userId }.isNotEmpty()
            internalSettingsRepository.setMasterPasswordAttemptsCount(userId, 0)

            accountManager.disableAccount(userId)
            toastManager.showToast(R.string.autofill_user_logged_out)

            if (hasAccountsLeft.not()) {
                closeScreenFlow.update { true }
            }
        }
    }

    companion object {
        private const val TAG = "AutofillActivityViewModel"
    }
}
