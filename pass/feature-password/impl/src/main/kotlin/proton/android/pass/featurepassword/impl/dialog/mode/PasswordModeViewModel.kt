package proton.android.pass.featurepassword.impl.dialog.mode

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.some
import proton.android.pass.preferences.PasswordGenerationMode
import proton.android.pass.preferences.UserPreferencesRepository
import javax.inject.Inject

@HiltViewModel
class PasswordModeViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val selectedModeFlow = MutableStateFlow<Option<PasswordGenerationMode>>(None)
    private val eventFlow = MutableStateFlow<PasswordModeUiEvent>(PasswordModeUiEvent.Unknown)

    private val preferenceFlow = userPreferencesRepository.getPasswordGenerationPreference()
        .onEach { pref ->
            if (selectedModeFlow.value is None) {
                selectedModeFlow.update { pref.mode.some() }
            }
        }

    val state: StateFlow<PasswordModeUiState> = combine(
        selectedModeFlow,
        preferenceFlow,
        eventFlow
    ) { selectedMode, preference, event ->
        val selected = when (selectedMode) {
            None -> preference.mode.some()
            else -> selectedMode
        }

        PasswordModeUiState(
            options = PasswordModeUiState.Initial.options,
            selected = selected,
            event = event
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = PasswordModeUiState.Initial
        )

    fun onChange(value: PasswordGenerationMode) {
        selectedModeFlow.update { value.some() }
    }

    fun onConfirm() = viewModelScope.launch {
        val current = userPreferencesRepository.getPasswordGenerationPreference().first()
        val selectedMode = selectedModeFlow.value
        if (selectedMode is Some) {
            val updated = current.copy(mode = selectedMode.value)
            userPreferencesRepository.setPasswordGenerationPreference(updated)
        }

        eventFlow.update { PasswordModeUiEvent.Close }
    }

}
