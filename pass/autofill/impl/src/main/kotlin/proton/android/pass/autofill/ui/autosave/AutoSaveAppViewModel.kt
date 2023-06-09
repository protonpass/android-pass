package proton.android.pass.autofill.ui.autosave

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.runBlocking
import proton.android.pass.autofill.AutosaveDisplay
import proton.android.pass.autofill.AutosaveDone
import proton.android.pass.biometry.NeedsBiometricAuth
import proton.android.pass.preferences.ThemePreference
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.telemetry.api.TelemetryManager
import javax.inject.Inject

@HiltViewModel
class AutoSaveAppViewModel @Inject constructor(
    private val preferenceRepository: UserPreferencesRepository,
    private val needsBiometricAuth: NeedsBiometricAuth,
    private val telemetryManager: TelemetryManager
) : ViewModel() {

    init {
        telemetryManager.sendEvent(AutosaveDisplay)
    }

    val state: StateFlow<AutoSaveAppViewState> = combine(
        preferenceRepository.getThemePreference(),
        needsBiometricAuth()
    ) { theme, needsAuth -> AutoSaveAppViewState(theme, needsAuth) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = runBlocking {
                AutoSaveAppViewState(
                    theme = preferenceRepository.getThemePreference().first(),
                    needsAuth = needsBiometricAuth().first()
                )
            }
        )

    fun onItemAutoSaved() {
        telemetryManager.sendEvent(AutosaveDone)
    }
}

data class AutoSaveAppViewState(
    val theme: ThemePreference,
    val needsAuth: Boolean
)
