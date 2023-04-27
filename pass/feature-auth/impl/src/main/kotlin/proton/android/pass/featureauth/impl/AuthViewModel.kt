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
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import proton.android.pass.biometry.BiometryAuthError
import proton.android.pass.biometry.BiometryAuthTimeHolder
import proton.android.pass.biometry.BiometryManager
import proton.android.pass.biometry.BiometryResult
import proton.android.pass.biometry.BiometryStatus
import proton.android.pass.biometry.ContextHolder
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Some
import proton.android.pass.log.api.PassLogger
import proton.android.pass.preferences.AppLockPreference
import proton.android.pass.preferences.BiometricLockState
import proton.android.pass.preferences.HasAuthenticated
import proton.android.pass.preferences.UserPreferencesRepository
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val preferenceRepository: UserPreferencesRepository,
    private val biometryManager: BiometryManager,
    private val authTimeHolder: BiometryAuthTimeHolder,
    private val clock: Clock
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
                    // If there is biometry available and the user has it enabled, check if we need
                    // to perform auth
                    if (shouldPerformAuth()) {
                        performAuth(context)
                    } else {
                        _state.update { AuthStatus.Success }
                    }
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
                            else -> _state.update { AuthStatus.Failed }
                        }
                    }

                    // User can retry
                    BiometryResult.Failed -> {}
                    is BiometryResult.FailedToStart -> _state.update { AuthStatus.Failed }
                }
            }
    }

    private suspend fun shouldPerformAuth(): Boolean {
        val lastAuthTime = when (val time = authTimeHolder.getBiometryAuthTime().first()) {
            is Some -> time.value
            None -> {
                PassLogger.d(TAG, "Requesting auth because no last auth time was found")
                return true
            }
        }

        return shouldPerformAuthWithLastAuthTime(lastAuthTime)
    }

    private suspend fun shouldPerformAuthWithLastAuthTime(lastAuthTime: Instant): Boolean {
        val appLockTimePreference =
            when (val pref = preferenceRepository.getAppLockPreference().first()) {
                AppLockPreference.Immediately -> {
                    PassLogger.d(TAG, "Requesting auth because AppLockPreference.Immediately")
                    return true
                }

                AppLockPreference.Never -> return false
                else -> pref
            }

        val appLockDuration = appLockTimePreference.toDuration()
        val timeSinceLastAuth = clock.now() - lastAuthTime
        val shouldPerform = appLockDuration < timeSinceLastAuth
        PassLogger.d(
            TAG,
            "timeSinceLastAuth: $timeSinceLastAuth |" +
                " appLockDuration: $appLockDuration | shouldPerformAuth: $shouldPerform"
        )
        return shouldPerform
    }

    private fun AppLockPreference.toDuration(): Duration = when (this) {
        AppLockPreference.InOneMinute -> 1.minutes
        AppLockPreference.InTwoMinutes -> 2.minutes
        AppLockPreference.InFiveMinutes -> 5.minutes
        AppLockPreference.InTenMinutes -> 10.minutes
        AppLockPreference.InOneHour -> 1.hours
        AppLockPreference.InFourHours -> 4.hours
        else -> Duration.ZERO
    }

    companion object {
        private const val TAG = "AuthViewModel"
    }
}
