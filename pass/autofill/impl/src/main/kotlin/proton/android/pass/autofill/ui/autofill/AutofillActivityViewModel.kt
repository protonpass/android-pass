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

import android.view.autofill.AutofillId
import androidx.activity.ComponentActivity
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.domain.AccountManager
import proton.android.pass.account.api.AccountOrchestrators
import proton.android.pass.account.api.Orchestrator
import proton.android.pass.autofill.api.AutofillManager
import proton.android.pass.autofill.entities.AndroidAutofillFieldId
import proton.android.pass.autofill.entities.AutofillAppState
import proton.android.pass.autofill.entities.AutofillItem
import proton.android.pass.autofill.entities.FieldType
import proton.android.pass.autofill.entities.isValid
import proton.android.pass.autofill.extensions.deserializeParcelable
import proton.android.pass.autofill.service.R
import proton.android.pass.autofill.ui.autofill.AutofillActivity.Companion.ARG_APP_NAME
import proton.android.pass.autofill.ui.autofill.AutofillActivity.Companion.ARG_AUTOFILL_IDS
import proton.android.pass.autofill.ui.autofill.AutofillActivity.Companion.ARG_AUTOFILL_IS_FOCUSED
import proton.android.pass.autofill.ui.autofill.AutofillActivity.Companion.ARG_AUTOFILL_PARENT_ID
import proton.android.pass.autofill.ui.autofill.AutofillActivity.Companion.ARG_AUTOFILL_TYPES
import proton.android.pass.autofill.ui.autofill.AutofillActivity.Companion.ARG_INLINE_SUGGESTION_AUTOFILL_ITEM
import proton.android.pass.autofill.ui.autofill.AutofillActivity.Companion.ARG_PACKAGE_NAME
import proton.android.pass.autofill.ui.autofill.AutofillActivity.Companion.ARG_TITLE
import proton.android.pass.autofill.ui.autofill.AutofillActivity.Companion.ARG_WEB_DOMAIN
import proton.android.pass.autofill.ui.autofill.AutofillUiState.NotValidAutofillUiState
import proton.android.pass.autofill.ui.autofill.AutofillUiState.StartAutofillUiState
import proton.android.pass.autofill.ui.autofill.AutofillUiState.UninitialisedAutofillUiState
import proton.android.pass.biometry.NeedsBiometricAuth
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.combineN
import proton.android.pass.common.api.flatMap
import proton.android.pass.common.api.toOption
import proton.android.pass.commonuimodels.api.PackageInfoUi
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
    private val autofillManager: AutofillManager,
    needsBiometricAuth: NeedsBiometricAuth,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val packageInfo: Option<PackageInfoUi> = savedStateHandle.get<String>(ARG_PACKAGE_NAME)
        .toOption()
        .map { packageName ->
            PackageInfoUi(
                packageName = packageName,
                appName = savedStateHandle.get<String>(ARG_APP_NAME) ?: packageName
            )
        }

    private val closeScreenFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)

    private val webDomain: Option<String> = savedStateHandle.get<String>(ARG_WEB_DOMAIN)
        .toOption()
    private val title: Option<String> = savedStateHandle.get<String>(ARG_TITLE)
        .toOption()
    private val types: Option<List<FieldType>> =
        savedStateHandle.get<List<String>>(ARG_AUTOFILL_TYPES)
            .toOption()
            .map { list -> list.map(FieldType.Companion::from) }
    private val ids: Option<List<AndroidAutofillFieldId?>> =
        savedStateHandle.get<List<AutofillId?>>(ARG_AUTOFILL_IDS)
            .toOption()
            .map { list -> list.map { item -> item?.let { AndroidAutofillFieldId(it) } } }
    private val fieldIsFocusedList: Option<List<Boolean>> =
        savedStateHandle.get<List<Boolean>>(ARG_AUTOFILL_IS_FOCUSED)
            .toOption()
    private val parentIdList: Option<List<AndroidAutofillFieldId?>> =
        savedStateHandle.get<List<AutofillId?>>(ARG_AUTOFILL_PARENT_ID)
            .toOption()
            .map { list -> list.map { item -> item?.let { AndroidAutofillFieldId(it) } } }

    private val autofillAppState: MutableStateFlow<AutofillAppState> =
        MutableStateFlow(
            AutofillAppState(
                packageInfoUi = packageInfo.value(),
                androidAutofillIds = ids.value() ?: emptyList(),
                fieldTypes = types.value() ?: emptyList(),
                fieldIsFocusedList = fieldIsFocusedList.value() ?: emptyList(),
                parentIdList = parentIdList.value() ?: emptyList(),
                webDomain = webDomain,
                title = title.value() ?: ""
            )
        )

    private val selectedAutofillItemState: MutableStateFlow<Option<AutofillItem>> =
        MutableStateFlow(
            savedStateHandle.get<ByteArray>(ARG_INLINE_SUGGESTION_AUTOFILL_ITEM)
                ?.deserializeParcelable<AutofillItem>()
                .toOption()
        )

    private val copyTotpToClipboardPreferenceState = preferenceRepository
        .getCopyTotpToClipboardEnabled()
        .distinctUntilChanged()

    private val themePreferenceState: Flow<ThemePreference> = preferenceRepository
        .getThemePreference()
        .distinctUntilChanged()

    val state: StateFlow<AutofillUiState> = combineN(
        themePreferenceState,
        needsBiometricAuth(),
        autofillAppState,
        selectedAutofillItemState,
        copyTotpToClipboardPreferenceState,
        closeScreenFlow
    ) { themePreference, needsAuth, autofillAppState, selectedAutofillItem, copyTotpToClipboard, closeScreen ->
        when {
            closeScreen -> AutofillUiState.CloseScreen
            autofillAppState.isValid() -> NotValidAutofillUiState
            else -> StartAutofillUiState(
                themePreference = themePreference.value(),
                needsAuth = needsAuth,
                autofillAppState = autofillAppState,
                copyTotpToClipboardPreference = copyTotpToClipboard.value(),
                selectedAutofillItem = selectedAutofillItem
            )
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

        autofillManager.disableAutofill()

        closeScreenFlow.update { true }
    }

    companion object {
        private const val TAG = "AutofillActivityViewModel"
    }
}
