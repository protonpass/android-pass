package proton.android.pass.featureprofile.impl.applock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.preferences.AppLockPreference
import proton.android.pass.preferences.UserPreferencesRepository
import javax.inject.Inject

@HiltViewModel
class AppLockViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val eventState: MutableStateFlow<AppLockEvent> = MutableStateFlow(AppLockEvent.Unknown)

    val state: StateFlow<AppLockUiState> = combine(
        userPreferencesRepository.getAppLockPreference(),
        eventState
    ) { preference, event ->
        AppLockUiState(
            items = allPreferences,
            selected = preference,
            event = event
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AppLockUiState.Initial
    )

    fun onChanged(appLockPreference: AppLockPreference) = viewModelScope.launch {
        userPreferencesRepository.setAppLockPreference(appLockPreference)
        eventState.update { AppLockEvent.OnChanged }
    }
}
