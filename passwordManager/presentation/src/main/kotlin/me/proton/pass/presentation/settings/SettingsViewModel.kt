package me.proton.pass.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.proton.android.pass.preferences.BiometricLockState
import me.proton.android.pass.preferences.PreferenceRepository
import me.proton.pass.presentation.uievents.IsButtonEnabled
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: PreferenceRepository
) : ViewModel() {

    private val biometricLockState: Flow<BiometricLockState> =
        preferencesRepository.getBiometricLockState().distinctUntilChanged()

    val state: StateFlow<SettingsUiState> = combine(
        biometricLockState
    ) { biometricLock ->
        SettingsUiState(
            isFingerPrintEnabled = IsButtonEnabled.Enabled
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = SettingsUiState.Initial
    )

    fun onFingerPrintLockChange(state: IsButtonEnabled) = viewModelScope.launch {
        val lockState = when (state) {
            IsButtonEnabled.Enabled -> BiometricLockState.Enabled
            IsButtonEnabled.Disabled -> BiometricLockState.Disabled
        }
        preferencesRepository.setBiometricLockState(lockState)
    }

}
