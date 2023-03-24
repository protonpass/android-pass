package proton.android.pass.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import proton.android.pass.appconfig.api.AppConfig
import proton.android.pass.image.impl.ClearIconCache
import proton.android.pass.log.api.LogSharing
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.ui.InternalDrawerSnackbarMessage.PreferencesClearError
import proton.android.pass.ui.InternalDrawerSnackbarMessage.PreferencesCleared
import javax.inject.Inject

@HiltViewModel
class InternalDrawerViewModel @Inject constructor(
    private val appConfig: AppConfig,
    private val preferenceRepository: UserPreferencesRepository,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val logSharing: LogSharing,
    private val clearCache: ClearIconCache
) : ViewModel() {

    fun clearPreferences() = viewModelScope.launch {
        preferenceRepository.clearPreferences()
            .onSuccess {
                snackbarDispatcher(PreferencesCleared)
            }
            .onFailure {
                PassLogger.e(TAG, it, "Error clearing preferences")
                snackbarDispatcher(PreferencesClearError)
            }
    }

    fun shareLogCatOutput(context: Context) = viewModelScope.launch(Dispatchers.IO) {
        logSharing.shareLogs(appConfig.applicationId, context)
    }

    fun clearIconCache() = viewModelScope.launch {
        clearCache()
    }

    companion object {
        private const val TAG = "InternalDrawerViewModel"
    }
}
