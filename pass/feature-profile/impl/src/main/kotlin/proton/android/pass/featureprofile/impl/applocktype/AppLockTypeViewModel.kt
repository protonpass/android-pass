/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.featureprofile.impl.applocktype

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.biometry.BiometryAuthError
import proton.android.pass.biometry.BiometryManager
import proton.android.pass.biometry.BiometryResult
import proton.android.pass.biometry.BiometryStatus
import proton.android.pass.commonui.api.ClassHolder
import proton.android.pass.data.api.usecases.ClearPin
import proton.android.pass.featureprofile.impl.ProfileSnackbarMessage
import proton.android.pass.featureprofile.impl.ProfileSnackbarMessage.BiometryFailedToAuthenticateError
import proton.android.pass.featureprofile.impl.ProfileSnackbarMessage.BiometryFailedToStartError
import proton.android.pass.featureprofile.impl.ProfileSnackbarMessage.FingerprintLockEnabled
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.preferences.AppLockState
import proton.android.pass.preferences.AppLockTypePreference
import proton.android.pass.preferences.AppLockTypePreference.Biometrics
import proton.android.pass.preferences.AppLockTypePreference.None
import proton.android.pass.preferences.AppLockTypePreference.Pin
import proton.android.pass.preferences.HasAuthenticated
import proton.android.pass.preferences.UserPreferencesRepository
import javax.inject.Inject

@HiltViewModel
class AppLockTypeViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val biometryManager: BiometryManager,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val clearPin: ClearPin
) : ViewModel() {
    private val eventState: MutableStateFlow<AppLockTypeEvent> =
        MutableStateFlow(AppLockTypeEvent.Unknown)
    private val newPreferenceState: MutableStateFlow<AppLockTypePreference?> =
        MutableStateFlow(null)

    val state: StateFlow<AppLockTypeUiState> = combine(
        flow { emit(biometryManager.getBiometryStatus()) },
        userPreferencesRepository.getAppLockTypePreference(),
        eventState
    ) { biometryStatus, appLockTypePreference, event ->
        AppLockTypeUiState(
            items = appLockTypePreferences(biometryStatus),
            selected = appLockTypePreference,
            event = event
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = run {
            val biometryStatus = biometryManager.getBiometryStatus()
            AppLockTypeUiState.default(appLockTypePreferences(biometryStatus))
        }
    )

    private fun appLockTypePreferences(biometryStatus: BiometryStatus): MutableList<AppLockTypePreference> {
        val preferences = mutableListOf(None, Pin)
        if (biometryStatus is BiometryStatus.CanAuthenticate) {
            preferences.add(Biometrics)
        }
        return preferences
    }

    fun onChanged(newPreference: AppLockTypePreference, contextHolder: ClassHolder<Context>) =
        viewModelScope.launch {
            val oldPreference = state.value.selected
            if (oldPreference == newPreference) {
                eventState.update { AppLockTypeEvent.Dismiss }
            } else {
                newPreferenceState.update { newPreference }
                when (oldPreference) {
                    Biometrics -> when (newPreference) {
                        None -> openBiometrics(
                            contextHolder = contextHolder,
                            onSuccess = ::onBiometryAuthUnSet,
                            onError = ::onBiometryError
                        )

                        Pin -> openBiometrics(
                            contextHolder = contextHolder,
                            onSuccess = { eventState.update { AppLockTypeEvent.ConfigurePin } },
                            onError = ::onBiometryError
                        )

                        else -> {}
                    }

                    None -> when (newPreference) {
                        Biometrics -> openBiometrics(
                            contextHolder = contextHolder,
                            onSuccess = ::onBiometryAuthSet,
                            onError = ::onBiometryError
                        )

                        Pin -> eventState.update { AppLockTypeEvent.ConfigurePin }
                        else -> {}
                    }

                    Pin -> when (newPreference) {
                        Biometrics -> eventState.update { AppLockTypeEvent.EnterPin }
                        None -> eventState.update { AppLockTypeEvent.EnterPin }
                        else -> {}
                    }
                }
            }
        }

    fun onPinSuccessfullyEntered(contextHolder: ClassHolder<Context>) {
        val newPreference = newPreferenceState.value ?: return
        when (newPreference) {
            None -> onPinAuthUnSet()
            Biometrics -> openBiometrics(
                contextHolder = contextHolder,
                onSuccess = ::onBiometryAuthSet,
                onError = ::onBiometryError
            )

            Pin -> {
                // Cannot happen
            }
        }
    }

    fun clearEvents() = viewModelScope.launch {
        eventState.update { AppLockTypeEvent.Unknown }
    }

    private fun openBiometrics(
        contextHolder: ClassHolder<Context>,
        onSuccess: suspend () -> Unit,
        onError: suspend (BiometryResult.Error) -> Unit
    ) = viewModelScope.launch {
        biometryManager.launch(contextHolder)
            .collect { result ->
                when (result) {
                    BiometryResult.Success -> onSuccess()
                    is BiometryResult.Error -> onError(result)
                    // User can retry
                    BiometryResult.Failed -> {}
                    is BiometryResult.FailedToStart -> onBiometryFailedToStart()
                }
                PassLogger.i(TAG, "Biometry result: $result")
            }
    }

    private suspend fun onBiometryFailedToStart() {
        snackbarDispatcher(BiometryFailedToStartError)
    }

    private suspend fun onBiometryError(result: BiometryResult.Error) {
        when (result.cause) {
            // If the user has cancelled it, do nothing
            BiometryAuthError.Canceled -> {}
            BiometryAuthError.UserCanceled -> {}

            else -> snackbarDispatcher(BiometryFailedToAuthenticateError)
        }
    }

    private fun onBiometryAuthSet() {
        viewModelScope.launch {
            userPreferencesRepository.setAppLockState(AppLockState.Enabled)
            userPreferencesRepository.setHasAuthenticated(HasAuthenticated.Authenticated)
            userPreferencesRepository.setAppLockTypePreference(Biometrics)
            snackbarDispatcher(FingerprintLockEnabled)
            eventState.update { AppLockTypeEvent.Dismiss }
        }
    }

    private fun onBiometryAuthUnSet() {
        viewModelScope.launch {
            userPreferencesRepository.setAppLockState(AppLockState.Disabled)
            userPreferencesRepository.setAppLockTypePreference(None)
            snackbarDispatcher(ProfileSnackbarMessage.FingerprintLockDisabled)
            eventState.update { AppLockTypeEvent.Dismiss }
        }
    }

    private fun onPinAuthUnSet() {
        viewModelScope.launch {
            userPreferencesRepository.setAppLockState(AppLockState.Disabled)
            userPreferencesRepository.setAppLockTypePreference(None)
            snackbarDispatcher(ProfileSnackbarMessage.PinLockDisabled)
            clearPin()
            eventState.update { AppLockTypeEvent.Dismiss }
        }
    }

    companion object {
        private const val TAG = "AppLockTypeViewModel"
    }
}
