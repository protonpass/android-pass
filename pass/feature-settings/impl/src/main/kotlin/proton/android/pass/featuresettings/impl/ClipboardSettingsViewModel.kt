package proton.android.pass.featuresettings.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarMessageRepository
import proton.android.pass.preferences.ClearClipboardPreference
import proton.android.pass.preferences.CopyTotpToClipboard
import proton.android.pass.preferences.UserPreferencesRepository
import javax.inject.Inject

@HiltViewModel
class ClipboardSettingsViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
    private val snackbarMessageRepository: SnackbarMessageRepository
) : ViewModel() {

    val state: StateFlow<ClipboardSettingsUIState> = combine(
        preferencesRepository.getCopyTotpToClipboardEnabled(),
        preferencesRepository.getClearClipboardPreference(),
        ::ClipboardSettingsUIState
    )
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = runBlocking {
                ClipboardSettingsUIState(
                    preferencesRepository.getCopyTotpToClipboardEnabled().first(),
                    preferencesRepository.getClearClipboardPreference().first()
                )
            }
        )

    fun onCopyToClipboardChange(value: Boolean) = viewModelScope.launch {
        PassLogger.d(TAG, "Changing CopyTotpToClipboard to $value")
        preferencesRepository.setCopyTotpToClipboardEnabled(CopyTotpToClipboard.from(value))
            .onFailure {
                PassLogger.e(TAG, it, "Error setting CopyTotpToClipboard")
                snackbarMessageRepository.emitSnackbarMessage(SettingsSnackbarMessage.ErrorPerformingOperation)
            }
    }

    companion object {
        private const val TAG = "ClipboardSettingsViewModel"
    }
}

data class ClipboardSettingsUIState(
    val isCopyTotpToClipboardEnabled: CopyTotpToClipboard,
    val clearClipboardPreference: ClearClipboardPreference
)
