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

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import proton.android.pass.biometry.BiometryAuthError
import proton.android.pass.biometry.BiometryManager
import proton.android.pass.biometry.BiometryResult
import proton.android.pass.biometry.BiometryStatus
import proton.android.pass.biometry.ContextHolder
import proton.android.pass.biometry.StoreAuthSuccessful
import proton.android.pass.common.api.AppDispatchers
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.some
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.api.usecases.CheckMasterPassword
import proton.android.pass.data.api.usecases.ObservePrimaryUserEmail
import proton.android.pass.log.api.PassLogger
import proton.android.pass.preferences.AppLockTypePreference
import proton.android.pass.preferences.AppLockState
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

    private val eventFlow: MutableStateFlow<AuthEvent> = MutableStateFlow(AuthEvent.Unknown)
    private val formContentFlow: MutableStateFlow<FormContents> = MutableStateFlow(FormContents())

    val state: StateFlow<AuthState> = combine(
        eventFlow,
        formContentFlow,
        observePrimaryUserEmail()
    ) { event, formContent, userEmail ->
        AuthState(
            event = event,
            content = AuthContent(
                password = formContent.password,
                isLoadingState = formContent.isLoadingState,
                isPasswordVisible = formContent.isPasswordVisible,
                error = formContent.error,
                passwordError = formContent.passwordError,
                address = userEmail
            )
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = AuthState.Initial
        )

    fun init(context: ContextHolder) = viewModelScope.launch {
        when (preferenceRepository.getAppLockTypePreference().first()) {
            AppLockTypePreference.Biometrics -> when (biometryManager.getBiometryStatus()) {
                BiometryStatus.CanAuthenticate -> {
                    val biometricLockState = preferenceRepository.getAppLockState().first()
                    if (biometricLockState == AppLockState.Disabled) {
                        // If there is biometry available, but the user does not have it enabled
                        // we should proceed
                        eventFlow.update { AuthEvent.Success }
                    } else {
                        // If there is biometry available, and the user has it enabled, perform auth
                        openBiometrics(context)
                    }
                }

                else -> {
                    // If there is no biometry available, emit success
                    eventFlow.update { AuthEvent.Success }
                }
            }

            AppLockTypePreference.Pin -> eventFlow.update { AuthEvent.EnterPin }
            AppLockTypePreference.None -> {}
        }
    }

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
                    eventFlow.update { AuthEvent.Success }
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
                        eventFlow.update { AuthEvent.ForceSignOut }
                    } else {
                        PassLogger.i(TAG, "Wrong password. Remaining attempts: $remainingAttempts")
                        formContentFlow.update {
                            it.copy(error = AuthError.WrongPassword(remainingAttempts).some())
                        }
                    }
                }
            }
            .onFailure { err ->
                PassLogger.w(TAG, err, "Error checking master password")
                formContentFlow.update { it.copy(error = AuthError.UnknownError.some()) }
            }

        formContentFlow.update { it.copy(isLoadingState = IsLoadingState.NotLoading) }
    }

    fun onSignOut() = viewModelScope.launch {
        eventFlow.update { AuthEvent.SignOut }
    }

    fun onTogglePasswordVisibility(value: Boolean) = viewModelScope.launch {
        formContentFlow.update { it.copy(isPasswordVisible = value) }
    }

    fun clearEvent() = viewModelScope.launch {
        eventFlow.update { AuthEvent.Unknown }
    }

    private suspend fun openBiometrics(context: ContextHolder) {
        PassLogger.i(TAG, "Launching Biometry")
        biometryManager.launch(context)
            .collect { result ->
                PassLogger.i(TAG, "Biometry result: $result")
                when (result) {
                    BiometryResult.Success -> {
                        formContentFlow.update { it.copy(password = "", isPasswordVisible = false) }
                        eventFlow.update { AuthEvent.Success }
                    }

                    is BiometryResult.Error -> {
                        PassLogger.w(TAG, "BiometryResult=Error: cause ${result.cause}")
                        when (result.cause) {
                            BiometryAuthError.Canceled,
                            BiometryAuthError.UserCanceled,
                            BiometryAuthError.NegativeButton -> {
                            }

                            else -> eventFlow.update { AuthEvent.Failed }
                        }
                    }

                    // User can retry
                    BiometryResult.Failed -> {}

                    is BiometryResult.FailedToStart -> {
                        eventFlow.update { AuthEvent.Failed }
                    }
                }
            }
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
