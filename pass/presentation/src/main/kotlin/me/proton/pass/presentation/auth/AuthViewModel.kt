package me.proton.pass.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.android.pass.biometry.BiometryAuthError
import me.proton.android.pass.biometry.BiometryManager
import me.proton.android.pass.biometry.BiometryResult
import me.proton.android.pass.biometry.ContextHolder
import me.proton.android.pass.log.PassLogger
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
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
        PassLogger.i(TAG, "Launching Biometry")
        biometryManager.launch(context)
            .map { result ->
                when (val res = result) {
                    BiometryResult.Success -> _state.update { AuthStatus.Success }
                    is BiometryResult.Error -> {
                        when (res.cause) {
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
