package proton.android.pass.featuresettings.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarMessageRepository
import proton.android.pass.preferences.ClearClipboardPreference
import proton.android.pass.preferences.UserPreferencesRepository
import javax.inject.Inject

@HiltViewModel
class ClearClipboardOptionsViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
    private val snackbarMessageRepository: SnackbarMessageRepository
) : ViewModel() {

    private val isClearClipboardOptionSavedState: MutableStateFlow<IsClearClipboardOptionSaved> =
        MutableStateFlow(IsClearClipboardOptionSaved.Unknown)

    val state: StateFlow<ClearClipboardOptionsUIState> = combine(
        preferencesRepository.getClearClipboardPreference(),
        isClearClipboardOptionSavedState,
        ::ClearClipboardOptionsUIState
    )
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = runBlocking {
                ClearClipboardOptionsUIState(
                    preferencesRepository.getClearClipboardPreference().first(),
                    IsClearClipboardOptionSaved.Unknown
                )
            }
        )

    fun onClearClipboardSettingSelected(value: ClearClipboardPreference) = viewModelScope.launch {
        PassLogger.d(TAG, "Changing ClearClipboardPreference to $value")
        preferencesRepository.setClearClipboardPreference(value)
            .onSuccess { isClearClipboardOptionSavedState.update { IsClearClipboardOptionSaved.Success } }
            .onFailure {
                PassLogger.e(TAG, it, "Error setting ClearClipboardPreference")
                snackbarMessageRepository.emitSnackbarMessage(SettingsSnackbarMessage.ErrorPerformingOperation)
            }
    }

    companion object {
        private const val TAG = "ClearClipboardOptionsViewModel"
    }
}

data class ClearClipboardOptionsUIState(
    val clearClipboardPreference: ClearClipboardPreference,
    val isClearClipboardOptionSaved: IsClearClipboardOptionSaved
)
