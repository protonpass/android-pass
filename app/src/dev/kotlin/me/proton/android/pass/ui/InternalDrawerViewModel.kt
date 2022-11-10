package me.proton.android.pass.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import me.proton.android.pass.log.PassLogger
import me.proton.android.pass.notifications.api.SnackbarMessageRepository
import me.proton.android.pass.preferences.PreferenceRepository
import me.proton.pass.common.api.asResultWithoutLoading
import me.proton.pass.common.api.onError
import me.proton.pass.common.api.onSuccess
import javax.inject.Inject

@HiltViewModel
class InternalDrawerViewModel @Inject constructor(
    private val preferenceRepository: PreferenceRepository,
    private val snackbarMessageRepository: SnackbarMessageRepository
) : ViewModel() {

    fun clearPreferences() = viewModelScope.launch {
        preferenceRepository.clearPreferences()
            .asResultWithoutLoading()
            .collect { result ->
                result
                    .onSuccess {
                        snackbarMessageRepository
                            .emitSnackbarMessage(InternalDrawerSnackbarMessage.PreferencesCleared)
                    }
                    .onError {
                        val message = "Error clearing preferences"
                        PassLogger.e(TAG, it ?: Exception(message), message)
                        snackbarMessageRepository
                            .emitSnackbarMessage(InternalDrawerSnackbarMessage.PreferencesClearError)
                    }
            }
    }

    companion object {
        private const val TAG = "InternalDrawerViewModel"
    }

}
