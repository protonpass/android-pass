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

package proton.android.pass.featureauth.impl

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import proton.android.pass.biometry.BiometryAuthError
import proton.android.pass.biometry.BiometryManager
import proton.android.pass.biometry.BiometryResult
import proton.android.pass.biometry.BiometryStatus
import proton.android.pass.biometry.BiometryType
import proton.android.pass.biometry.StoreAuthSuccessful
import proton.android.pass.common.api.AppDispatchers
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.common.api.some
import proton.android.pass.commonui.api.ClassHolder
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.api.usecases.CheckMasterPassword
import proton.android.pass.data.api.usecases.ObservePrimaryUserEmail
import proton.android.pass.log.api.PassLogger
import proton.android.pass.preferences.AppLockState
import proton.android.pass.preferences.AppLockTypePreference
import proton.android.pass.preferences.InternalSettingsRepository
import proton.android.pass.preferences.UserPreferencesRepository
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val preferenceRepository: UserPreferencesRepository,
    private val biometryManager: BiometryManager,
    private val checkMasterPassword: CheckMasterPassword,
    private val storeAuthSuccessful: StoreAuthSuccessful,
    private val internalSettingsRepository: InternalSettingsRepository,
    private val appDispatchers: AppDispatchers,
    observePrimaryUserEmail: ObservePrimaryUserEmail
) : ViewModel() {

    private val eventFlow: MutableStateFlow<Option<AuthEvent>> = MutableStateFlow(None)
    private val formContentFlow: MutableStateFlow<FormContents> = MutableStateFlow(FormContents())
    private val authMethodFlow: Flow<Option<AuthMethod>> = preferenceRepository
        .getAppLockTypePreference()
        .map {
            when (it) {
                AppLockTypePreference.None -> None
                AppLockTypePreference.Biometrics -> AuthMethod.Fingerprint.some()
                AppLockTypePreference.Pin -> AuthMethod.Pin.some()
            }
        }
        .distinctUntilChanged()

    val state: StateFlow<AuthState> = combine(
        eventFlow,
        formContentFlow,
        observePrimaryUserEmail().asLoadingResult(),
        authMethodFlow
    ) { event, formContent, userEmail, authMethod ->

        val address = when (userEmail) {
            LoadingResult.Loading -> None
            is LoadingResult.Error -> {
                PassLogger.w(TAG, "Error loading userEmail")
                PassLogger.w(TAG, userEmail.exception)
                None
            }

            is LoadingResult.Success -> userEmail.data.some()
        }
        AuthState(
            event = event,
            content = AuthContent(
                password = formContent.password,
                isLoadingState = formContent.isLoadingState,
                isPasswordVisible = formContent.isPasswordVisible,
                error = formContent.error,
                passwordError = formContent.passwordError,
                address = address,
                authMethod = authMethod
            )
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = AuthState.Initial
        )

    fun onPasswordChanged(value: String) = viewModelScope.launch {
        formContentFlow.update {
            it.copy(
                password = value,

                // Hide errors on password change
                passwordError = None,
                error = None
            )
        }
    }

    fun onSubmit() = viewModelScope.launch(appDispatchers.main) {
        val password = formContentFlow.value.password
        if (password.isEmpty()) { // Do not use isBlank, as spaces are valid
            formContentFlow.update {
                it.copy(passwordError = PasswordError.EmptyPassword.some())
            }
            return@launch
        }

        formContentFlow.update {
            it.copy(
                isPasswordVisible = false, // Hide password on submit by default
                isLoadingState = IsLoadingState.Loading,

                // Hide errors by default
                error = None,
                passwordError = None
            )
        }

        runCatching { checkMasterPassword(password = password.encodeToByteArray()) }
            .onSuccess { isPasswordRight ->
                if (isPasswordRight) {
                    storeAuthSuccessful()
                    formContentFlow.update { it.copy(password = "", isPasswordVisible = false) }
                    updateAuthEvent(AuthEvent.Success)
                } else {
                    withContext(appDispatchers.default) {
                        delay(WRONG_PASSWORD_DELAY_SECONDS)
                    }

                    val currentFailedAttempts = internalSettingsRepository
                        .getMasterPasswordAttemptsCount()
                        .first()

                    internalSettingsRepository.setMasterPasswordAttemptsCount(currentFailedAttempts + 1)

                    val remainingAttempts = MAX_WRONG_PASSWORD_ATTEMPTS - currentFailedAttempts - 1

                    if (remainingAttempts <= 0) {
                        PassLogger.w(TAG, "Too many wrong attempts, logging user out")
                        updateAuthEvent(AuthEvent.ForceSignOut)
                    } else {
                        PassLogger.i(TAG, "Wrong password. Remaining attempts: $remainingAttempts")
                        formContentFlow.update {
                            it.copy(error = AuthError.WrongPassword(remainingAttempts).some())
                        }
                    }
                }
            }
            .onFailure { err ->
                PassLogger.w(TAG, "Error checking master password")
                PassLogger.w(TAG, err)
                formContentFlow.update { it.copy(error = AuthError.UnknownError.some()) }
            }

        formContentFlow.update { it.copy(isLoadingState = IsLoadingState.NotLoading) }
    }

    fun onSignOut() = viewModelScope.launch {
        updateAuthEvent(AuthEvent.SignOut)
    }

    fun onTogglePasswordVisibility(value: Boolean) = viewModelScope.launch {
        formContentFlow.update { it.copy(isPasswordVisible = value) }
    }

    internal fun onAuthMethodRequested() = viewModelScope.launch {
        val newAuthEvent = when (preferenceRepository.getAppLockTypePreference().first()) {
            AppLockTypePreference.None -> AuthEvent.Unknown
            AppLockTypePreference.Biometrics -> AuthEvent.EnterBiometrics
            AppLockTypePreference.Pin -> AuthEvent.EnterPin
        }

        updateAuthEvent(newAuthEvent)
    }

    internal fun clearEvent() = viewModelScope.launch {
        updateAuthEvent(AuthEvent.Unknown)
    }

    internal suspend fun onBiometricsRequired(contextHolder: ClassHolder<Context>) {
        val newAuthEvent = when (biometryManager.getBiometryStatus()) {
            BiometryStatus.NotAvailable,
            BiometryStatus.NotEnrolled -> AuthEvent.Success

            BiometryStatus.CanAuthenticate -> {
                val biometricLockState = preferenceRepository.getAppLockState().first()
                if (biometricLockState == AppLockState.Enabled) {
                    // If there is biometry available, and the user has it enabled, perform auth
                    openBiometrics(contextHolder)
                    return
                }
                // If there is biometry available, but the user does not have it enabled
                // we should proceed
                AuthEvent.Success
            }
        }

        updateAuthEvent(newAuthEvent)
    }

    private suspend fun openBiometrics(contextHolder: ClassHolder<Context>) {
        PassLogger.i(TAG, "Launching Biometry")
        biometryManager.launch(contextHolder, BiometryType.AUTHENTICATE)
            .collect { result ->
                PassLogger.i(TAG, "Biometry result: $result")
                when (result) {
                    BiometryResult.Success -> {
                        formContentFlow.update { it.copy(password = "", isPasswordVisible = false) }
                        updateAuthEvent(AuthEvent.Success)
                    }

                    is BiometryResult.Error -> {
                        PassLogger.w(TAG, "BiometryResult=Error: cause ${result.cause}")
                        when (result.cause) {
                            BiometryAuthError.Canceled,
                            BiometryAuthError.UserCanceled,
                            BiometryAuthError.NegativeButton -> {
                            }

                            else -> updateAuthEvent(AuthEvent.Failed)
                        }
                    }

                    // User can retry
                    BiometryResult.Failed -> {}

                    is BiometryResult.FailedToStart -> {
                        updateAuthEvent(AuthEvent.Failed)
                    }
                }
            }
    }

    private fun updateAuthEvent(newAuthEvent: AuthEvent) {
        eventFlow.update { newAuthEvent.some() }
    }

    private data class FormContents(
        val password: String = "",
        val isPasswordVisible: Boolean = false,
        val isLoadingState: IsLoadingState = IsLoadingState.NotLoading,
        val error: Option<AuthError> = None,
        val passwordError: Option<PasswordError> = None
    )

    companion object {
        private const val TAG = "AuthViewModel"

        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        const val WRONG_PASSWORD_DELAY_SECONDS = 2000L

        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        const val MAX_WRONG_PASSWORD_ATTEMPTS = 5
    }
}
