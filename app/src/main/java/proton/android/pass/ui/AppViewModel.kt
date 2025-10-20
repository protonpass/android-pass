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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import me.proton.core.domain.entity.UserId
import proton.android.pass.biometry.NeedsBiometricAuth
import proton.android.pass.common.api.onError
import proton.android.pass.common.api.onSuccess
import proton.android.pass.common.api.runCatching
import proton.android.pass.data.api.usecases.inappmessages.ChangeInAppMessageStatus
import proton.android.pass.data.api.usecases.inappmessages.ObserveDeliverableBannerInAppMessages
import proton.android.pass.domain.inappmessages.InAppMessageId
import proton.android.pass.domain.inappmessages.InAppMessageKey
import proton.android.pass.domain.inappmessages.InAppMessageStatus
import proton.android.pass.features.inappmessages.InAppMessagesChange
import proton.android.pass.features.inappmessages.InAppMessagesClick
import proton.android.pass.features.inappmessages.InAppMessagesDisplay
import proton.android.pass.inappupdates.api.InAppUpdatesManager
import proton.android.pass.log.api.PassLogger
import proton.android.pass.network.api.NetworkMonitor
import proton.android.pass.network.api.NetworkStatus
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.preferences.InternalSettingsRepository
import proton.android.pass.preferences.LastTimeUserHasSeenIAMPreference
import proton.android.pass.telemetry.api.TelemetryManager
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    private val snackbarDispatcher: SnackbarDispatcher,
    private val needsBiometricAuth: NeedsBiometricAuth,
    private val inAppUpdatesManager: InAppUpdatesManager,
    private val changeInAppMessageStatus: ChangeInAppMessageStatus,
    private val telemetryManager: TelemetryManager,
    private val internalSettingsRepository: InternalSettingsRepository,
    private val clock: Clock,
    networkMonitor: NetworkMonitor,
    observeDeliverableBannerInAppMessages: ObserveDeliverableBannerInAppMessages
) : ViewModel() {

    private val networkStatus: Flow<NetworkStatus> = networkMonitor
        .connectivity
        .distinctUntilChanged()

    val needsAuthState = needsBiometricAuth()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = runBlocking { needsBiometricAuth().first() }
        )

    val appUiState: StateFlow<AppUiState> = combine(
        snackbarDispatcher.snackbarMessage,
        networkStatus,
        inAppUpdatesManager.observeInAppUpdateState(),
        observeDeliverableBannerInAppMessages(refresh = true)
    ) { snackbarMessage, networkStatus, inAppUpdateState, inAppMessage ->
        AppUiState(
            snackbarMessage = snackbarMessage,
            networkStatus = networkStatus,
            inAppUpdateState = inAppUpdateState,
            inAppMessage = inAppMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AppUiState.Initial
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

    fun onInAppMessageBannerRead(
        userId: UserId,
        inAppMessageId: InAppMessageId,
        inAppMessageKey: InAppMessageKey
    ) {
        viewModelScope.launch {
            runCatching {
                changeInAppMessageStatus(userId, inAppMessageId, InAppMessageStatus.Read)
            }
                .onSuccess {
                    internalSettingsRepository.setLastTimeUserHasSeenIAM(
                        LastTimeUserHasSeenIAMPreference(userId, clock.now().epochSeconds)
                    )
                    telemetryManager.sendEvent(InAppMessagesChange(inAppMessageKey, InAppMessageStatus.Read))
                    PassLogger.i(TAG, "In-app message read")
                }
                .onError {
                    PassLogger.w(TAG, "Error reading in-app message")
                    PassLogger.w(TAG, it)
                }
        }
    }

    fun onInAppMessageBannerDisplayed(inAppMessageKey: InAppMessageKey) {
        telemetryManager.sendEvent(InAppMessagesDisplay(inAppMessageKey))
    }

    fun onInAppMessageBannerCTAClicked(inAppMessageKey: InAppMessageKey) {
        telemetryManager.sendEvent(InAppMessagesClick(inAppMessageKey))
    }

    companion object {
        private const val TAG = "AppViewModel"
    }
}
