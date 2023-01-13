package proton.android.pass.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import proton.android.pass.appconfig.api.AppConfig
import proton.android.pass.common.api.onError
import proton.android.pass.common.api.onSuccess
import proton.android.pass.data.api.usecases.DeleteVault
import proton.android.pass.data.api.usecases.ObserveActiveShare
import proton.android.pass.log.api.LogSharing
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarMessageRepository
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.ui.InternalDrawerSnackbarMessage.DeleteVaultError
import proton.android.pass.ui.InternalDrawerSnackbarMessage.DeleteVaultSuccess
import proton.android.pass.ui.InternalDrawerSnackbarMessage.EmptyShareError
import javax.inject.Inject

@HiltViewModel
class InternalDrawerViewModel @Inject constructor(
    private val appConfig: AppConfig,
    private val preferenceRepository: UserPreferencesRepository,
    private val snackbarMessageRepository: SnackbarMessageRepository,
    private val logSharing: LogSharing,
    private val observeActiveShare: ObserveActiveShare,
    private val deleteVault: DeleteVault
) : ViewModel() {

    fun deleteVault() = viewModelScope.launch {
        observeActiveShare()
            .first()
            .onSuccess { shareId ->
                if (shareId != null) {
                    deleteVault(shareId)
                        .onSuccess {
                            snackbarMessageRepository.emitSnackbarMessage(DeleteVaultSuccess)
                        }
                        .onError {
                            snackbarMessageRepository.emitSnackbarMessage(DeleteVaultError)
                        }
                } else {
                    snackbarMessageRepository.emitSnackbarMessage(EmptyShareError)
                }
            }
            .onError {
                snackbarMessageRepository.emitSnackbarMessage(EmptyShareError)
            }
    }

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
        logSharing.shareLogs(appConfig.applicationId, context)
    }

    companion object {
        private const val TAG = "InternalDrawerViewModel"
    }
}
