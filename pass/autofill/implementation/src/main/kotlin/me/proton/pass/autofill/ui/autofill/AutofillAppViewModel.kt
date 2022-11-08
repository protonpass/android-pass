package me.proton.pass.autofill.ui.autofill

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import me.proton.android.pass.biometry.BiometryManager
import me.proton.android.pass.biometry.BiometryStatus
import me.proton.android.pass.log.PassLogger
import me.proton.android.pass.preferences.BiometricLockState
import me.proton.android.pass.preferences.PreferenceRepository
import me.proton.android.pass.preferences.ThemePreference
import me.proton.pass.common.api.Result
import me.proton.pass.common.api.asResultWithoutLoading
import javax.inject.Inject

@HiltViewModel
class AutofillAppViewModel @Inject constructor(
    preferenceRepository: PreferenceRepository,
    private val biometryManager: BiometryManager
) : ViewModel() {

    private val themeState: Flow<ThemePreference> = preferenceRepository
        .getThemePreference()
        .asResultWithoutLoading()
        .map { getThemePreference(it) }
        .distinctUntilChanged()

    private val biometricLockState: Flow<BiometricLockState> = preferenceRepository
        .getBiometricLockState()
        .asResultWithoutLoading()
        .map { getBiometricLockState(it) }
        .distinctUntilChanged()

    val state: StateFlow<AutofillAppUiState> = combine(
        themeState, biometricLockState
    ) { theme, fingerprint ->
        val fingerprintRequired = when (biometryManager.getBiometryStatus()) {
            BiometryStatus.CanAuthenticate -> fingerprint is BiometricLockState.Enabled
            else -> false
        }

        AutofillAppUiState(
            theme = theme,
            isFingerprintRequired = fingerprintRequired
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = AutofillAppUiState.Initial
        )

    private fun getBiometricLockState(state: Result<BiometricLockState>): BiometricLockState =
        when (state) {
            Result.Loading -> BiometricLockState.Disabled
            is Result.Success -> state.data
            is Result.Error -> {
                val message = "Error getting BiometricLockState"
                PassLogger.e(TAG, state.exception ?: Exception(message))
                BiometricLockState.Disabled
            }
        }

    private fun getThemePreference(state: Result<ThemePreference>): ThemePreference =
        when (state) {
            Result.Loading -> ThemePreference.System
            is Result.Success -> state.data
            is Result.Error -> {
                val message = "Error getting ThemePreference"
                PassLogger.e(TAG, state.exception ?: Exception(message))
                ThemePreference.System
            }
        }

    companion object {
        private const val TAG = "AutofillAppViewModel"
    }
}
