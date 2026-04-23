/*
 * Copyright (c) 2023-2026 Proton AG
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

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.proton.core.domain.entity.UserId
import proton.android.pass.appconfig.api.AppConfig
import proton.android.pass.appconfig.api.BuildFlavor.Companion.isQuest
import proton.android.pass.autofill.api.AutofillManager
import proton.android.pass.autofill.api.AutofillStatus
import proton.android.pass.autofill.api.AutofillSupportedStatus
import proton.android.pass.biometry.NeedsBiometricAuth
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.common.api.getOrNull
import proton.android.pass.common.api.onError
import proton.android.pass.common.api.onSuccess
import proton.android.pass.common.api.runCatching
import proton.android.pass.data.api.usecases.ObserveShouldShowExploreTab
import proton.android.pass.data.api.usecases.inappmessages.ChangeInAppMessageStatus
import proton.android.pass.data.api.usecases.inappmessages.ObserveDeliverableBannerInAppMessages
import proton.android.pass.data.api.usecases.simplelogin.ObserveSimpleLoginSyncStatus
import proton.android.pass.domain.inappmessages.InAppMessage
import proton.android.pass.domain.inappmessages.InAppMessageId
import proton.android.pass.domain.inappmessages.InAppMessageKey
import proton.android.pass.domain.inappmessages.InAppMessageStatus
import proton.android.pass.features.home.localinappmessages.LocalInAppMessagesEvent
import proton.android.pass.features.inappmessages.InAppMessagesChange
import proton.android.pass.features.inappmessages.InAppMessagesClick
import proton.android.pass.features.inappmessages.InAppMessagesDisplay
import proton.android.pass.inappupdates.api.InAppUpdatesManager
import proton.android.pass.log.api.PassLogger
import proton.android.pass.network.api.NetworkMonitor
import proton.android.pass.network.api.NetworkStatus
import proton.android.pass.notifications.api.NotificationManager
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.preferences.HasCompletedOnBoarding
import proton.android.pass.preferences.HasDismissedAutofillBanner
import proton.android.pass.preferences.HasDismissedNotificationBanner
import proton.android.pass.preferences.HasDismissedSLSyncBanner
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.telemetry.api.TelemetryManager
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    private val snackbarDispatcher: SnackbarDispatcher,
    private val needsBiometricAuth: NeedsBiometricAuth,
    private val inAppUpdatesManager: InAppUpdatesManager,
    private val changeInAppMessageStatus: ChangeInAppMessageStatus,
    private val telemetryManager: TelemetryManager,
    private val autofillManager: AutofillManager,
    private val preferencesRepository: UserPreferencesRepository,
    private val appConfig: AppConfig,
    networkMonitor: NetworkMonitor,
    notificationManager: NotificationManager,
    observeDeliverableBannerInAppMessages: ObserveDeliverableBannerInAppMessages,
    observeSimpleLoginSyncStatus: ObserveSimpleLoginSyncStatus,
    observeShouldShowExploreTab: ObserveShouldShowExploreTab
) : ViewModel() {

    private val networkStatus: Flow<NetworkStatus> = networkMonitor
        .connectivity
        .distinctUntilChanged()

    private val notificationPermissionFlow: MutableStateFlow<Boolean> =
        MutableStateFlow(notificationManager.hasNotificationPermission())

    private val localInAppMessageEventFlow: MutableStateFlow<LocalInAppMessagesEvent> =
        MutableStateFlow(LocalInAppMessagesEvent.Unknown)

    val needsAuthState = needsBiometricAuth()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = runBlocking { needsBiometricAuth().first() }
        )

    private val localMessagesFlow: Flow<List<InAppMessage.Local>> = combine(
        combine(
            autofillManager.getAutofillStatus(),
            preferencesRepository.getHasDismissedAutofillBanner()
        ) { status, dismissed ->
            if (status is AutofillSupportedStatus.Supported &&
                status.status !is AutofillStatus.EnabledByOurService &&
                dismissed is HasDismissedAutofillBanner.NotDismissed
            ) InAppMessage.Local.Autofill else null
        },
        combine(
            notificationPermissionFlow,
            preferencesRepository.getHasDismissedNotificationBanner(),
            flowOf(appConfig.flavor.isQuest())
        ) { granted, dismissed, isQuest ->
            val shouldShow = !isQuest &&
                !granted &&
                dismissed is HasDismissedNotificationBanner.NotDismissed
            if (shouldShow && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                InAppMessage.Local.NotificationPermission
            } else null
        }.distinctUntilChanged(),
        combine(
            preferencesRepository.getHasDismissedSLSyncBanner(),
            observeSimpleLoginSyncStatus().asLoadingResult()
        ) { dismissed, syncStatus ->
            if (dismissed is HasDismissedSLSyncBanner.NotDismissed) {
                syncStatus.getOrNull()?.let { status ->
                    if (status.isPreferenceEnabled && status.hasPendingAliases && !status.isSyncEnabled) {
                        InAppMessage.Local.SLSync(status.pendingAliasCount, status.defaultVault.shareId)
                    } else null
                }
            } else null
        }.distinctUntilChanged()
    ) { autofill, notification, slSync ->
        listOfNotNull(notification, autofill, slSync)
    }.distinctUntilChanged()

    private val systemStatusFlow = combine(
        snackbarDispatcher.snackbarMessage,
        networkStatus,
        inAppUpdatesManager.observeInAppUpdateState()
    ) { snackbar, net, update -> Triple(snackbar, net, update) }

    private val bannersFlow = combine(
        observeDeliverableBannerInAppMessages(),
        localMessagesFlow,
        localInAppMessageEventFlow,
        preferencesRepository.getHasCompletedOnBoarding()
    ) { banners, local, event, onboarding ->
        val visibleBanners = if (onboarding == HasCompletedOnBoarding.Completed) {
            (local + banners).take(MAX_VISIBLE_BANNERS)
        } else {
            emptyList()
        }
        Pair(visibleBanners, event)
    }

    val appUiState: StateFlow<AppUiState> = combine(
        systemStatusFlow,
        bannersFlow,
        observeShouldShowExploreTab()
    ) { (snackbar, net, update), (messages, event), showExplore ->
        AppUiState(
            snackbarMessage = snackbar,
            networkStatus = net,
            inAppUpdateState = update,
            inAppMessages = messages,
            showExplore = showExplore,
            localInAppMessageEvent = event
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AppUiState.Initial.copy(
            showExplore = runBlocking { observeShouldShowExploreTab().first() }
        )
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

    fun onLocalInAppMessageClick(message: InAppMessage.Local) {
        when (message) {
            InAppMessage.Local.Autofill -> autofillManager.openAutofillSelector()
            InAppMessage.Local.NotificationPermission ->
                localInAppMessageEventFlow.update { LocalInAppMessagesEvent.RequestNotificationPermission }
            is InAppMessage.Local.SLSync ->
                localInAppMessageEventFlow.update { LocalInAppMessagesEvent.OpenSLSyncSettings(message.shareId) }
        }
    }

    fun onLocalInAppMessageDismiss(message: InAppMessage.Local) {
        viewModelScope.launch {
            when (message) {
                InAppMessage.Local.Autofill ->
                    preferencesRepository.setHasDismissedAutofillBanner(HasDismissedAutofillBanner.Dismissed)
                InAppMessage.Local.NotificationPermission ->
                    preferencesRepository.setHasDismissedNotificationBanner(HasDismissedNotificationBanner.Dismissed)
                is InAppMessage.Local.SLSync ->
                    preferencesRepository.setHasDismissedSLSyncBanner(HasDismissedSLSyncBanner.Dismissed)
            }
        }
    }

    fun onNotificationPermissionChanged(granted: Boolean) {
        if (notificationPermissionFlow.value != granted) {
            notificationPermissionFlow.value = granted
        }
    }

    fun clearLocalInAppMessageEvent() {
        localInAppMessageEventFlow.update { LocalInAppMessagesEvent.Unknown }
    }

    companion object {
        private const val TAG = "AppViewModel"
        private const val MAX_VISIBLE_BANNERS = 3
    }
}
