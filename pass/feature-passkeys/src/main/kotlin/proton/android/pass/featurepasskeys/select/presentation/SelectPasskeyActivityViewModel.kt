/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.featurepasskeys.select.presentation

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Immutable
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.domain.AccountManager
import proton.android.pass.account.api.AccountOrchestrators
import proton.android.pass.account.api.Orchestrator
import proton.android.pass.biometry.NeedsBiometricAuth
import proton.android.pass.biometry.ExtendAuthTime
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.some
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.PasskeyId
import proton.android.pass.domain.ShareId
import proton.android.pass.featurepasskeys.R
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.ToastManager
import proton.android.pass.preferences.ThemePreference
import proton.android.pass.preferences.UserPreferencesRepository
import javax.inject.Inject

sealed class SelectPasskeyRequest(
    val requestJson: String,
    val requestOrigin: String,
    val clientDataHash: ByteArray?
) {
    data class SelectPasskey(
        private val request: String,
        private val origin: String,
        private val dataHash: ByteArray?
    ) : SelectPasskeyRequest(request, origin, dataHash)

    data class UsePasskey(
        private val request: String,
        private val origin: String,
        private val dataHash: ByteArray?,
        val shareId: ShareId,
        val itemId: ItemId,
        val passkeyId: PasskeyId
    ) : SelectPasskeyRequest(request, origin, dataHash)
}

@Immutable
sealed class SelectPasskeyRequestData(
    val domain: String,
    val request: String,
    val clientDataHash: ByteArray?
) {
    data class SelectPasskey(
        private val requestDomain: String,
        private val requestJson: String,
        private val requestClientDataHash: ByteArray?
    ) : SelectPasskeyRequestData(requestDomain, requestJson, requestClientDataHash)

    data class UsePasskey(
        private val requestDomain: String,
        private val requestJson: String,
        private val requestClientDataHash: ByteArray?,
        val shareId: ShareId,
        val itemId: ItemId,
        val passkeyId: PasskeyId
    ) : SelectPasskeyRequestData(requestDomain, requestJson, requestClientDataHash)
}

@Immutable
enum class SelectPasskeyActionAfterAuth {
    EmitEvent,
    SelectItem
}

@Immutable
sealed interface SelectPasskeyAppState {

    @Immutable
    data object NotReady : SelectPasskeyAppState

    @Immutable
    data object Close : SelectPasskeyAppState

    @Immutable
    data class Ready(
        val theme: ThemePreference,
        val needsAuth: Boolean,
        val data: SelectPasskeyRequestData,
        val actionAfterAuth: SelectPasskeyActionAfterAuth
    ) : SelectPasskeyAppState

    @Immutable
    data object ErrorAuthenticating : SelectPasskeyAppState
}

@HiltViewModel
class SelectPasskeyActivityViewModel @Inject constructor(
    private val accountOrchestrators: AccountOrchestrators,
    private val accountManager: AccountManager,
    private val toastManager: ToastManager,
    private val extendAuthTime: ExtendAuthTime,
    preferenceRepository: UserPreferencesRepository,
    needsBiometricAuth: NeedsBiometricAuth
) : ViewModel() {

    private val closeScreenFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)

    private val themePreferenceState: Flow<ThemePreference> = preferenceRepository
        .getThemePreference()
        .distinctUntilChanged()

    private val requestFlow: MutableStateFlow<Option<SelectPasskeyRequest>> = MutableStateFlow(None)

    private val requestDataFlow: Flow<Option<SelectPasskeyRequestData>> = requestFlow.map {
        it.map { request ->
            when (request) {
                is SelectPasskeyRequest.SelectPasskey -> SelectPasskeyRequestData.SelectPasskey(
                    requestDomain = request.requestOrigin,
                    requestJson = request.requestJson,
                    requestClientDataHash = request.clientDataHash
                )

                is SelectPasskeyRequest.UsePasskey -> SelectPasskeyRequestData.UsePasskey(
                    requestDomain = request.requestOrigin,
                    requestJson = request.requestJson,
                    requestClientDataHash = request.clientDataHash,
                    shareId = request.shareId,
                    itemId = request.itemId,
                    passkeyId = request.passkeyId
                )
            }
        }
    }

    val state: StateFlow<SelectPasskeyAppState> = combine(
        closeScreenFlow,
        themePreferenceState,
        needsBiometricAuth(),
        requestDataFlow
    ) { closeScreen, theme, needsAuth, request ->
        when (request) {
            None -> return@combine SelectPasskeyAppState.NotReady
            is Some -> when {
                closeScreen -> SelectPasskeyAppState.Close
                else -> SelectPasskeyAppState.Ready(
                    theme = theme,
                    needsAuth = needsAuth,
                    data = request.value,
                    actionAfterAuth = when (request.value) {
                        is SelectPasskeyRequestData.SelectPasskey -> {
                            SelectPasskeyActionAfterAuth.SelectItem
                        }

                        is SelectPasskeyRequestData.UsePasskey -> {
                            SelectPasskeyActionAfterAuth.EmitEvent
                        }
                    }
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SelectPasskeyAppState.NotReady
    )

    fun setRequest(request: SelectPasskeyRequest) = viewModelScope.launch {
        requestFlow.update { request.some() }
    }

    fun register(context: ComponentActivity) {
        accountOrchestrators.register(context, listOf(Orchestrator.PlansOrchestrator))
    }

    fun upgrade() = viewModelScope.launch {
        accountOrchestrators.start(Orchestrator.PlansOrchestrator)
    }

    fun onStop() = viewModelScope.launch {
        extendAuthTime()
    }

    fun signOut() = viewModelScope.launch {
        PassLogger.i(TAG, "Signing user out")
        val primaryUserId = accountManager.getPrimaryUserId().firstOrNull()
        if (primaryUserId != null) {
            accountManager.disableAccount(primaryUserId)
            toastManager.showToast(R.string.passkeys_user_logged_out)
        }

        closeScreenFlow.update { true }
    }

    companion object {
        private const val TAG = "SelectPasskeyActivityViewModel"
    }
}
