package proton.android.pass.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import proton.android.pass.common.api.flatMap
import proton.android.pass.image.api.ClearIconCache
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.preferences.InternalSettingsRepository
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.ui.InternalDrawerSnackbarMessage.PreferencesClearError
import proton.android.pass.ui.InternalDrawerSnackbarMessage.PreferencesCleared
import javax.inject.Inject

@HiltViewModel
class InternalDrawerViewModel @Inject constructor(
    private val preferenceRepository: UserPreferencesRepository,
    private val internalSettingsRepository: InternalSettingsRepository,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val clearCache: ClearIconCache
) : ViewModel() {

    fun clearPreferences() = viewModelScope.launch {
        preferenceRepository.clearPreferences()
            .flatMap { internalSettingsRepository.clearSettings() }
            .onSuccess {
                snackbarDispatcher(PreferencesCleared)
            }
            .onFailure {
                PassLogger.e(TAG, it, "Error clearing preferences")
                snackbarDispatcher(PreferencesClearError)
            }
    }

    fun clearIconCache() = viewModelScope.launch {
        clearCache()
    }

    companion object {
        private const val TAG = "InternalDrawerViewModel"
    }
}
