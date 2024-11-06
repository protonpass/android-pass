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

package proton.android.pass.ui

import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.proton.core.domain.entity.UserId
import proton.android.pass.biometry.NeedsBiometricAuth
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.asResultWithoutLoading
import proton.android.pass.common.api.getOrNull
import proton.android.pass.common.api.onError
import proton.android.pass.common.api.onSuccess
import proton.android.pass.common.api.runCatching
import proton.android.pass.common.api.toOption
import proton.android.pass.data.api.usecases.inappmessages.ChangeInAppMessageStatus
import proton.android.pass.data.api.usecases.inappmessages.ObserveDeliverableInAppMessages
import proton.android.pass.domain.inappmessages.InAppMessageId
import proton.android.pass.domain.inappmessages.InAppMessageStatus
import proton.android.pass.inappupdates.api.InAppUpdatesManager
import proton.android.pass.log.api.PassLogger
import proton.android.pass.network.api.NetworkMonitor
import proton.android.pass.network.api.NetworkStatus
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.preferences.ThemePreference
import proton.android.pass.preferences.UserPreferencesRepository
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    private val preferenceRepository: UserPreferencesRepository,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val needsBiometricAuth: NeedsBiometricAuth,
    private val inAppUpdatesManager: InAppUpdatesManager,
    private val changeInAppMessageStatus: ChangeInAppMessageStatus,
    networkMonitor: NetworkMonitor,
    observeDeliverableInAppMessages: ObserveDeliverableInAppMessages
) : ViewModel() {

    private val themePreference: Flow<ThemePreference> = preferenceRepository
        .getThemePreference()
        .asResultWithoutLoading()
        .map(::getThemePreference)
        .onEach {
            when (it) {
                ThemePreference.Light -> AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO)
                ThemePreference.Dark -> AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)
                ThemePreference.System ->
                    AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }

    private val networkStatus: Flow<NetworkStatus> = networkMonitor
        .connectivity
        .distinctUntilChanged()

    val needsAuthState = needsBiometricAuth()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = runBlocking { needsBiometricAuth().first() }
        )

    private val inAppMessageFlow = observeDeliverableInAppMessages()
        .asResultWithoutLoading()
        .map { it.getOrNull()?.firstOrNull().toOption() }

    val appUiState: StateFlow<AppUiState> = combine(
        snackbarDispatcher.snackbarMessage,
        themePreference,
        networkStatus,
        inAppUpdatesManager.observeInAppUpdateState(),
        inAppMessageFlow
    ) { snackbarMessage, theme, networkStatus, inAppUpdateState, inAppMessage ->
        AppUiState(
            snackbarMessage = snackbarMessage,
            theme = theme,
            networkStatus = networkStatus,
            inAppUpdateState = inAppUpdateState,
            inAppMessage = inAppMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = run {
            val theme = runBlocking { preferenceRepository.getThemePreference().first() }
            AppUiState.default(theme)
        }
    )

    fun onStop() = viewModelScope.launch {
        inAppUpdatesManager.completeUpdate()
    }

    fun onResume() = viewModelScope.launch {
        inAppUpdatesManager.checkUpdateStalled()
    }

    fun onCompleteUpdate() {
        inAppUpdatesManager.completeUpdate()
    }

    fun onSnackbarMessageDelivered() {
        viewModelScope.launch {
            snackbarDispatcher.snackbarMessageDelivered()
        }
    }

    private fun getThemePreference(state: LoadingResult<ThemePreference>): ThemePreference = when (state) {
        LoadingResult.Loading -> ThemePreference.System
        is LoadingResult.Success -> state.data
        is LoadingResult.Error -> {
            PassLogger.w(TAG, "Error getting ThemePreference")
            PassLogger.w(TAG, state.exception)
            ThemePreference.System
        }
    }

    fun onInAppMessageBannerRead(userId: UserId, inAppMessageId: InAppMessageId) {
        viewModelScope.launch {
            runCatching {
                changeInAppMessageStatus(userId, inAppMessageId, InAppMessageStatus.Read)
            }
                .onSuccess {
                    PassLogger.i(TAG, "In-app message read")
                }
                .onError {
                    PassLogger.w(TAG, "Error reading in-app message")
                    PassLogger.w(TAG, it)
                }
        }
    }

    companion object {
        private const val TAG = "AppViewModel"
    }
}
