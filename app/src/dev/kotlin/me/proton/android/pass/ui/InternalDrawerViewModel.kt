package me.proton.android.pass.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.proton.android.pass.BuildConfig
import me.proton.android.pass.log.InternalLogSharing
import me.proton.android.pass.log.PassLogger
import me.proton.android.pass.notifications.api.SnackbarMessageRepository
import me.proton.android.pass.preferences.UserPreferencesRepository
import javax.inject.Inject

@HiltViewModel
class InternalDrawerViewModel @Inject constructor(
    private val preferenceRepository: UserPreferencesRepository,
    private val snackbarMessageRepository: SnackbarMessageRepository
) : ViewModel() {

    fun clearPreferences() = viewModelScope.launch {
        preferenceRepository.clearPreferences()
            .onSuccess {
                snackbarMessageRepository
                    .emitSnackbarMessage(InternalDrawerSnackbarMessage.PreferencesCleared)
            }
            .onFailure {
                PassLogger.e(TAG, it, "Error clearing preferences")
                snackbarMessageRepository
                    .emitSnackbarMessage(InternalDrawerSnackbarMessage.PreferencesClearError)
            }
    }

    fun shareLogCatOutput(context: Context) = viewModelScope.launch(Dispatchers.IO) {
        InternalLogSharing.shareLogs(BuildConfig.APPLICATION_ID, context)
    }

    companion object {
        private const val TAG = "InternalDrawerViewModel"
    }
}
