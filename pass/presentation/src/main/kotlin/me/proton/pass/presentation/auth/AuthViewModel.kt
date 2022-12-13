package me.proton.pass.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.android.pass.biometry.BiometryAuthError
import me.proton.android.pass.biometry.BiometryManager
import me.proton.android.pass.biometry.BiometryResult
import me.proton.android.pass.biometry.BiometryStatus
import me.proton.android.pass.biometry.ContextHolder
import me.proton.android.pass.log.PassLogger
import me.proton.android.pass.preferences.BiometricLockState
import me.proton.android.pass.preferences.HasAuthenticated
import me.proton.android.pass.preferences.PreferenceRepository
import me.proton.pass.common.api.asResultWithoutLoading
import me.proton.pass.common.api.onError
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val preferenceRepository: PreferenceRepository,
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
                    // If there is biometry available and the user has it enabled, perform auth
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
            .map { result ->
                when (result) {
                    BiometryResult.Success -> {
                        preferenceRepository.setHasAuthenticated(HasAuthenticated.Authenticated)
                            .asResultWithoutLoading()
                            .collect { prefResult ->
                                prefResult.onError {
                                    val message = "Could not save HasAuthenticated preference"
                                    PassLogger.e(TAG, it ?: RuntimeException(message))
                                }
                            }
                        _state.update { AuthStatus.Success }
                    }
                    is BiometryResult.Error -> {
                        when (result.cause) {
                            BiometryAuthError.Canceled -> _state.update { AuthStatus.Canceled }
                            BiometryAuthError.UserCanceled -> _state.update { AuthStatus.Canceled }
                            else -> _state.update { AuthStatus.Failed }
                        }
                    }

                    // User can retry
                    BiometryResult.Failed -> { }
                    is BiometryResult.FailedToStart -> _state.update { AuthStatus.Failed }
                }
                PassLogger.i(TAG, "Biometry result: $result")
            }
            .collect {}
    }

    companion object {
        private const val TAG = "AuthViewModel"
    }
}
