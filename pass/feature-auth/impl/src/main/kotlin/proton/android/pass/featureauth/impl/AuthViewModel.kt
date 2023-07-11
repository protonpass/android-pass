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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.biometry.BiometryAuthError
import proton.android.pass.biometry.BiometryManager
import proton.android.pass.biometry.BiometryResult
import proton.android.pass.biometry.BiometryStatus
import proton.android.pass.biometry.ContextHolder
import proton.android.pass.log.api.PassLogger
import proton.android.pass.preferences.BiometricLockState
import proton.android.pass.preferences.HasAuthenticated
import proton.android.pass.preferences.UserPreferencesRepository
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val preferenceRepository: UserPreferencesRepository,
    private val biometryManager: BiometryManager
) : ViewModel() {

    private val _state: MutableStateFlow<AuthStatus> = MutableStateFlow(AuthStatus.Pending)
    val state: StateFlow<AuthStatus> = _state
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = AuthStatus.Pending
        )

    fun init(context: ContextHolder) = viewModelScope.launch {
        when (biometryManager.getBiometryStatus()) {
            BiometryStatus.CanAuthenticate -> {
                val biometricLockState = preferenceRepository.getBiometricLockState().first()
                if (biometricLockState == BiometricLockState.Disabled) {
                    // If there is biometry available, but the user does not have it enabled
                    // we should proceed
                    _state.update { AuthStatus.Success }
                } else {
                    // If there is biometry available, and the user has it enabled, perform auth
                    performAuth(context)
                }
            }

            else -> {
                // If there is no biometry available, emit success
                _state.update { AuthStatus.Success }
            }
        }
    }

    private suspend fun performAuth(context: ContextHolder) {
        PassLogger.i(TAG, "Launching Biometry")
        biometryManager.launch(context)
            .collect { result ->
                PassLogger.i(TAG, "Biometry result: $result")
                when (result) {
                    BiometryResult.Success -> {
                        preferenceRepository.setHasAuthenticated(HasAuthenticated.Authenticated)
                        _state.update { AuthStatus.Success }
                    }

                    is BiometryResult.Error -> {
                        PassLogger.w(TAG, "BiometryResult=Error: cause ${result.cause}")
                        when (result.cause) {
                            BiometryAuthError.Canceled -> _state.update { AuthStatus.Canceled }
                            BiometryAuthError.UserCanceled -> _state.update { AuthStatus.Canceled }
                            BiometryAuthError.NegativeButton -> _state.update { AuthStatus.Canceled }
                            else -> _state.update { AuthStatus.Failed }
                        }
                    }

                    // User can retry
                    BiometryResult.Failed -> {}
                    is BiometryResult.FailedToStart -> _state.update { AuthStatus.Failed }
                }
            }
    }

    companion object {
        private const val TAG = "AuthViewModel"
    }
}
