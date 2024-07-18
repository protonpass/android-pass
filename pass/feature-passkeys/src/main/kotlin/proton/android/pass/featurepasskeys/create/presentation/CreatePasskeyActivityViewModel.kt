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

package proton.android.pass.featurepasskeys.create.presentation

import androidx.activity.ComponentActivity
import androidx.credentials.CreatePublicKeyCredentialRequest
import androidx.credentials.provider.CallingAppInfo
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
import proton.android.pass.data.api.url.UrlSanitizer
import proton.android.pass.featurepasskeys.R
import proton.android.pass.featurepasskeys.select.SelectPasskeyUtils
import proton.android.pass.featurepasskeys.telemetry.CreateDone
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.ToastManager
import proton.android.pass.passkeys.api.ParseCreatePasskeyRequest
import proton.android.pass.preferences.ThemePreference
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.telemetry.api.TelemetryManager
import javax.inject.Inject

data class CreatePasskeyRequest(
    val callingAppInfo: CallingAppInfo,
    val callingRequest: CreatePublicKeyCredentialRequest
)

@HiltViewModel
class CreatePasskeyActivityViewModel @Inject constructor(
    private val accountOrchestrators: AccountOrchestrators,
    private val accountManager: AccountManager,
    private val parseCreatePasskeyRequest: ParseCreatePasskeyRequest,
    private val toastManager: ToastManager,
    private val telemetryManager: TelemetryManager,
    private val extendAuthTime: ExtendAuthTime,
    preferenceRepository: UserPreferencesRepository,
    needsBiometricAuth: NeedsBiometricAuth
) : ViewModel() {

    private val closeScreenFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)

    private val themePreferenceState: Flow<ThemePreference> = preferenceRepository
        .getThemePreference()
        .distinctUntilChanged()

    private val requestFlow: MutableStateFlow<Option<CreatePasskeyRequest>> = MutableStateFlow(None)

    private val requestDataFlow: Flow<Option<CreatePasskeyRequestData>> = requestFlow
        .map { requestOption ->
            requestOption.flatMap { request ->
                logRequest(request)

                runCatching {
                    val requestJson = request.callingRequest.requestJson
                    val parsed = parseCreatePasskeyRequest(requestJson)
                    val requestOrigin = request.callingRequest.origin ?: parsed.rpId ?: ""
                    val domain =
                        UrlSanitizer.getDomain(requestOrigin).getOrElse { parsed.rpId } ?: ""

                    CreatePasskeyRequestData(
                        domain = domain,
                        origin = requestOrigin,
                        username = parsed.userName,
                        request = requestJson,
                        rpName = parsed.rpName
                    )
                }.fold(
                    onSuccess = { it.some() },
                    onFailure = {
                        PassLogger.w(TAG, "Error parsing create request")
                        PassLogger.w(TAG, it)

                        closeScreenFlow.update { true }

                        None
                    }
                )
            }
        }


    val state: StateFlow<CreatePasskeyAppState> = combine(
        closeScreenFlow,
        themePreferenceState,
        needsBiometricAuth(),
        requestDataFlow
    ) { closeScreen, theme, needsAuth, request ->
        when (request) {
            None -> return@combine CreatePasskeyAppState.NotReady
            is Some -> when {
                closeScreen -> CreatePasskeyAppState.Close
                else -> CreatePasskeyAppState.Ready(
                    theme = theme,
                    needsAuth = needsAuth,
                    data = request.value
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CreatePasskeyAppState.NotReady
    )

    fun setRequest(request: CreatePasskeyRequest) {
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
        val primaryUserId = accountManager.getPrimaryUserId().firstOrNull()
        if (primaryUserId != null) {
            accountManager.disableAccount(primaryUserId)
            toastManager.showToast(R.string.passkeys_user_logged_out)
        }

        closeScreenFlow.update { true }
    }

    fun onResponseSent() = viewModelScope.launch {
        telemetryManager.sendEvent(CreateDone)
    }

    private fun logRequest(request: CreatePasskeyRequest) {
        val appInfoOrigin = request.callingAppInfo.origin
        val appPackageName = request.callingAppInfo.packageName
        val requestOrigin = request.callingRequest.origin
        val rpInfo = SelectPasskeyUtils.getRpInfoFromCreateRequest(request.callingRequest)

        PassLogger.i(
            TAG,
            "Create passkey request [appInfoOrigin=$appInfoOrigin] " +
                "[appPackageName=$appPackageName] [requestOrigin=$requestOrigin] [rpInfo=$rpInfo]"
        )
    }

    companion object {
        private const val TAG = "CreatePasskeyActivityViewModel"
    }
}
