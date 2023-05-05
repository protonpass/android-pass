package proton.android.pass.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
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
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import proton.android.pass.biometry.BiometryAuthTimeHolder
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.asResultWithoutLoading
import proton.android.pass.log.api.PassLogger
import proton.android.pass.network.api.NetworkMonitor
import proton.android.pass.network.api.NetworkStatus
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.preferences.AppLockPreference
import proton.android.pass.preferences.BiometricLockState
import proton.android.pass.preferences.ThemePreference
import proton.android.pass.preferences.UserPreferencesRepository
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

@HiltViewModel
class AppViewModel @Inject constructor(
    private val preferenceRepository: UserPreferencesRepository,
    networkMonitor: NetworkMonitor,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val authTimeHolder: BiometryAuthTimeHolder,
    private val clock: Clock
) : ViewModel() {

    private val themePreference: Flow<ThemePreference> = preferenceRepository
        .getThemePreference()
        .asResultWithoutLoading()
        .map { getThemePreference(it) }

    private val networkStatus: Flow<NetworkStatus> = networkMonitor
        .connectivity
        .distinctUntilChanged()

    private val needsAuthFlow: MutableStateFlow<Boolean> = MutableStateFlow(
        runBlocking {
            shouldPerformAuth()
        }
    )

    val appUiState: StateFlow<AppUiState> = combine(
        snackbarDispatcher.snackbarMessage,
        themePreference,
        networkStatus,
        needsAuthFlow
    ) { snackbarMessage, theme, network, needsAuth ->
        AppUiState(
            snackbarMessage = snackbarMessage,
            theme = theme,
            networkStatus = network,
            needsAuth = needsAuth
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = run {
                val (theme, needsAuth) = runBlocking {
                    preferenceRepository.getThemePreference().first() to
                        shouldPerformAuth()
                }
                AppUiState.Initial(theme, needsAuth)
            }
        )

    fun onSnackbarMessageDelivered() {
        viewModelScope.launch {
            snackbarDispatcher.snackbarMessageDelivered()
        }
    }

    fun onAppResumed() = viewModelScope.launch {
        needsAuthFlow.update { shouldPerformAuth() }
    }

    fun onAuthPerformed() {
        needsAuthFlow.update { false }
    }

    private fun getThemePreference(state: LoadingResult<ThemePreference>): ThemePreference =
        when (state) {
            LoadingResult.Loading -> ThemePreference.System
            is LoadingResult.Success -> state.data
            is LoadingResult.Error -> {
                PassLogger.w(TAG, state.exception, "Error getting ThemePreference")
                ThemePreference.System
            }
        }

    private suspend fun shouldPerformAuth(): Boolean {
        val biometricLockState = preferenceRepository.getBiometricLockState().first()
        if (biometricLockState == BiometricLockState.Disabled) return false

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
        private const val TAG = "AppViewModel"
    }
}
