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

package proton.android.pass.features.home.onboardingtips

import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.appconfig.api.AppConfig
import proton.android.pass.appconfig.api.BuildFlavor.Companion.isQuest
import proton.android.pass.autofill.api.AutofillManager
import proton.android.pass.autofill.api.AutofillStatus
import proton.android.pass.autofill.api.AutofillSupportedStatus
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.common.api.getOrNull
import proton.android.pass.common.api.toOption
import proton.android.pass.data.api.usecases.ObserveInvites
import proton.android.pass.data.api.usecases.simplelogin.ObserveSimpleLoginSyncStatus
import proton.android.pass.domain.PendingInvite
import proton.android.pass.features.home.onboardingtips.OnBoardingTipPage.Autofill
import proton.android.pass.features.home.onboardingtips.OnBoardingTipPage.Invite
import proton.android.pass.features.home.onboardingtips.OnBoardingTipPage.NotificationPermission
import proton.android.pass.features.home.onboardingtips.OnBoardingTipPage.SLSync
import proton.android.pass.notifications.api.NotificationManager
import proton.android.pass.preferences.HasDismissedAutofillBanner
import proton.android.pass.preferences.HasDismissedNotificationBanner
import proton.android.pass.preferences.HasDismissedSLSyncBanner
import proton.android.pass.preferences.UserPreferencesRepository
import javax.inject.Inject

@HiltViewModel
class OnBoardingTipsViewModel @Inject constructor(
    private val autofillManager: AutofillManager,
    private val preferencesRepository: UserPreferencesRepository,
    private val appConfig: AppConfig,
    observeInvites: ObserveInvites,
    notificationManager: NotificationManager,
    observeSimpleLoginSyncStatus: ObserveSimpleLoginSyncStatus
) : ViewModel() {

    private val eventFlow: MutableStateFlow<OnBoardingTipsEvent> =
        MutableStateFlow(OnBoardingTipsEvent.Unknown)

    private val pendingUserInviteFlow: Flow<PendingInvite?> = observeInvites()
        .map { pendingInvites -> pendingInvites.firstOrNull() }
        .distinctUntilChanged()

    private val notificationPermissionFlow: MutableStateFlow<Boolean> = MutableStateFlow(
        notificationManager.hasNotificationPermission()
    )

    private val shouldShowAutofillFlow: Flow<Boolean> = combine(
        autofillManager.getAutofillStatus(),
        preferencesRepository.getHasDismissedAutofillBanner(),
        ::shouldShowAutofillBanner
    )

    private val shouldShowNotificationPermissionFlow: Flow<Boolean> = combine(
        notificationPermissionFlow,
        preferencesRepository.getHasDismissedNotificationBanner(),
        flowOf(appConfig.flavor.isQuest())
    ) { notificationPermissionEnabled, hasDismissedNotificationBanner, isQuest ->
        when {
            isQuest -> false
            notificationPermissionEnabled -> false
            hasDismissedNotificationBanner is HasDismissedNotificationBanner.Dismissed -> false
            else -> needsNotificationPermissions()
        }
    }.distinctUntilChanged()

    private val simpleLoginSyncStatusResultFlow = observeSimpleLoginSyncStatus()
        .asLoadingResult()

    private val shouldShowSLSyncFlow = combine(
        preferencesRepository.getHasDismissedSLSyncBanner(),
        simpleLoginSyncStatusResultFlow
    ) { hasDismissedSLSyncBanner, syncStatusResult ->
        when {
            hasDismissedSLSyncBanner is HasDismissedSLSyncBanner.Dismissed -> false
            else -> syncStatusResult.getOrNull()?.let { syncStatus ->
                syncStatus.isPreferenceEnabled && syncStatus.hasPendingAliases && !syncStatus.isSyncEnabled
            } ?: false
        }
    }.distinctUntilChanged()

    private val onboardingTipPageOptionFlow = combine(
        pendingUserInviteFlow,
        shouldShowNotificationPermissionFlow,
        shouldShowAutofillFlow,
        shouldShowSLSyncFlow,
        simpleLoginSyncStatusResultFlow
    ) { pendingInvite, showNotificationPermission, showAutofill, showSLSync, slSyncStatusResult ->
        when {
            pendingInvite != null -> Invite(pendingInvite)
            showNotificationPermission -> NotificationPermission
            showAutofill -> Autofill
            showSLSync -> slSyncStatusResult.getOrNull()?.let { syncStatus ->
                SLSync(
                    aliasCount = syncStatus.pendingAliasCount,
                    shareId = syncStatus.defaultVault.shareId
                )
            }

            else -> null
        }.toOption()
    }

    internal val stateFlow: StateFlow<OnBoardingTipsUiState> = combine(
        onboardingTipPageOptionFlow,
        eventFlow,
        ::OnBoardingTipsUiState
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = OnBoardingTipsUiState()
    )

    private fun shouldShowAutofillBanner(
        autofillSupportedStatus: AutofillSupportedStatus,
        hasDismissedAutofillBanner: HasDismissedAutofillBanner
    ): Boolean = autofillSupportedStatus is AutofillSupportedStatus.Supported &&
        autofillSupportedStatus.status !is AutofillStatus.EnabledByOurService &&
        hasDismissedAutofillBanner is HasDismissedAutofillBanner.NotDismissed

    internal fun onClick(onBoardingTipPage: OnBoardingTipPage) {
        when (onBoardingTipPage) {
            Autofill -> autofillManager.openAutofillSelector()
            is Invite -> eventFlow.update {
                when (onBoardingTipPage.pendingInvite) {
                    is PendingInvite.GroupItem,
                    is PendingInvite.GroupVault ->
                        OnBoardingTipsEvent.OpenGroupInviteScreen(onBoardingTipPage.pendingInvite.inviteId)

                    is PendingInvite.UserItem,
                    is PendingInvite.UserVault ->
                        OnBoardingTipsEvent.OpenUserInviteScreen(onBoardingTipPage.pendingInvite.inviteToken)
                }
            }

            NotificationPermission -> eventFlow.update { OnBoardingTipsEvent.RequestNotificationPermission }
            is SLSync -> eventFlow.update {
                OnBoardingTipsEvent.OpenSLSyncSettingsScreen(
                    onBoardingTipPage.shareId
                )
            }
        }
    }

    internal fun onDismiss(onBoardingTipPage: OnBoardingTipPage) {
        viewModelScope.launch {
            when (onBoardingTipPage) {
                Autofill ->
                    preferencesRepository.setHasDismissedAutofillBanner(HasDismissedAutofillBanner.Dismissed)

                is Invite -> Unit // Invites cannot be dismissed
                NotificationPermission ->
                    preferencesRepository.setHasDismissedNotificationBanner(
                        HasDismissedNotificationBanner.Dismissed
                    )

                is SLSync ->
                    preferencesRepository.setHasDismissedSLSyncBanner(HasDismissedSLSyncBanner.Dismissed)
            }
        }
    }

    internal fun onNotificationPermissionChanged(permission: Boolean) {
        notificationPermissionFlow.update { permission }
    }

    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.TIRAMISU)
    private fun needsNotificationPermissions(): Boolean = appConfig.androidVersion >= Build.VERSION_CODES.TIRAMISU

    internal fun clearEvent() {
        eventFlow.update { OnBoardingTipsEvent.Unknown }
    }
}
