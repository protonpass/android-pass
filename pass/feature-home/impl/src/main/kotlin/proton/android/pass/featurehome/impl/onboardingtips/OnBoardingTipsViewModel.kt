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

package proton.android.pass.featurehome.impl.onboardingtips

import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.appconfig.api.AppConfig
import proton.android.pass.autofill.api.AutofillManager
import proton.android.pass.autofill.api.AutofillStatus
import proton.android.pass.autofill.api.AutofillSupportedStatus
import proton.android.pass.data.api.usecases.GetUserPlan
import proton.android.pass.data.api.usecases.ObserveInvites
import proton.android.pass.domain.PlanType
import proton.android.pass.featurehome.impl.onboardingtips.OnBoardingTipPage.AUTOFILL
import proton.android.pass.featurehome.impl.onboardingtips.OnBoardingTipPage.INVITE
import proton.android.pass.featurehome.impl.onboardingtips.OnBoardingTipPage.NOTIFICATION_PERMISSION
import proton.android.pass.featurehome.impl.onboardingtips.OnBoardingTipPage.SL_SYNC
import proton.android.pass.featurehome.impl.onboardingtips.OnBoardingTipPage.TRIAL
import proton.android.pass.notifications.api.NotificationManager
import proton.android.pass.preferences.FeatureFlag
import proton.android.pass.preferences.FeatureFlagsPreferencesRepository
import proton.android.pass.preferences.HasDismissedAutofillBanner
import proton.android.pass.preferences.HasDismissedNotificationBanner
import proton.android.pass.preferences.HasDismissedSLSyncBanner
import proton.android.pass.preferences.HasDismissedTrialBanner
import proton.android.pass.preferences.UserPreferencesRepository
import javax.inject.Inject

@HiltViewModel
class OnBoardingTipsViewModel @Inject constructor(
    private val autofillManager: AutofillManager,
    private val preferencesRepository: UserPreferencesRepository,
    private val featureFlagsPreferencesRepository: FeatureFlagsPreferencesRepository,
    private val appConfig: AppConfig,
    observeInvites: ObserveInvites,
    getUserPlan: GetUserPlan,
    notificationManager: NotificationManager
) : ViewModel() {

    private val eventFlow: MutableStateFlow<OnBoardingTipsEvent> =
        MutableStateFlow(OnBoardingTipsEvent.Unknown)

    private val hasInvitesFlow: Flow<Boolean> = observeInvites()
        .map { invites -> invites.isNotEmpty() }
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
        preferencesRepository.getHasDismissedNotificationBanner()
    ) { notificationPermissionEnabled, hasDismissedNotificationBanner ->
        when {
            notificationPermissionEnabled -> false
            hasDismissedNotificationBanner is HasDismissedNotificationBanner.Dismissed -> false
            else -> needsNotificationPermissions()
        }
    }.distinctUntilChanged()

    private val shouldShowTrialFlow: Flow<Boolean> = preferencesRepository
        .getHasDismissedTrialBanner()
        .map { pref ->
            when (pref) {
                HasDismissedTrialBanner.Dismissed -> false
                HasDismissedTrialBanner.NotDismissed -> true
            }
        }
        .distinctUntilChanged()

    private val shouldShowSLSyncFlow: Flow<Boolean> = combine(
        preferencesRepository.getHasDismissedSLSyncBanner(),
        featureFlagsPreferencesRepository.get<Boolean>(FeatureFlag.SL_ALIASES_SYNC)
    ) { hasDismissedSLSyncBanner, isSimpleLoginAliasesSyncEnabled ->
        when {
            !isSimpleLoginAliasesSyncEnabled -> false
            hasDismissedSLSyncBanner == HasDismissedSLSyncBanner.Dismissed -> false
            else -> true
        }
    }.distinctUntilChanged()

    data class TipVisibility(
        val shouldShowAutofill: Boolean,
        val shouldShowTrial: Boolean,
        val shouldShowInvites: Boolean,
        val shouldShowNotificationPermission: Boolean,
        val shouldShowSLSync: Boolean
    )

    private val tipVisibilityFlow = combine(
        shouldShowAutofillFlow,
        shouldShowTrialFlow,
        hasInvitesFlow,
        shouldShowNotificationPermissionFlow,
        shouldShowSLSyncFlow,
        ::TipVisibility
    )

    val state: StateFlow<OnBoardingTipsUiState> = combine(
        tipVisibilityFlow,
        getUserPlan(),
        eventFlow
    ) { tipVisibility, userPlan, event ->
        val isTrial = userPlan.planType is PlanType.Trial
        val tips = when {
            tipVisibility.shouldShowInvites -> persistentSetOf(INVITE)
            tipVisibility.shouldShowNotificationPermission ->
                persistentSetOf(NOTIFICATION_PERMISSION)

            isTrial && tipVisibility.shouldShowTrial -> persistentSetOf(TRIAL)
            tipVisibility.shouldShowAutofill -> persistentSetOf(AUTOFILL)
            tipVisibility.shouldShowSLSync -> persistentSetOf(SL_SYNC)
            else -> persistentSetOf()
        }

        OnBoardingTipsUiState(
            tipsToShow = tips,
            event = event
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = OnBoardingTipsUiState(
                tipsToShow = persistentSetOf(),
                event = OnBoardingTipsEvent.Unknown
            )
        )


    private fun shouldShowAutofillBanner(
        autofillSupportedStatus: AutofillSupportedStatus,
        hasDismissedAutofillBanner: HasDismissedAutofillBanner
    ): Boolean = autofillSupportedStatus is AutofillSupportedStatus.Supported &&
        autofillSupportedStatus.status !is AutofillStatus.EnabledByOurService &&
        hasDismissedAutofillBanner is HasDismissedAutofillBanner.NotDismissed

    fun onClick(onBoardingTipPage: OnBoardingTipPage) {
        when (onBoardingTipPage) {
            AUTOFILL -> autofillManager.openAutofillSelector()
            TRIAL -> eventFlow.update { OnBoardingTipsEvent.OpenTrialScreen }
            INVITE -> eventFlow.update { OnBoardingTipsEvent.OpenInviteScreen }
            NOTIFICATION_PERMISSION -> eventFlow.update { OnBoardingTipsEvent.RequestNotificationPermission }
            SL_SYNC -> eventFlow.update { OnBoardingTipsEvent.OpenSLSyncScreen }
        }
    }

    fun onDismiss(onBoardingTipPage: OnBoardingTipPage) = viewModelScope.launch {
        when (onBoardingTipPage) {
            AUTOFILL ->
                preferencesRepository.setHasDismissedAutofillBanner(HasDismissedAutofillBanner.Dismissed)

            TRIAL ->
                preferencesRepository.setHasDismissedTrialBanner(HasDismissedTrialBanner.Dismissed)

            INVITE -> {} // Invites cannot be dismissed
            NOTIFICATION_PERMISSION ->
                preferencesRepository.setHasDismissedNotificationBanner(
                    HasDismissedNotificationBanner.Dismissed
                )

            SL_SYNC ->
                preferencesRepository.setHasDismissedSLSyncBanner(HasDismissedSLSyncBanner.Dismissed)
        }
    }

    fun onNotificationPermissionChanged(permission: Boolean) = viewModelScope.launch {
        notificationPermissionFlow.update { permission }
    }

    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.TIRAMISU)
    private fun needsNotificationPermissions(): Boolean = appConfig.androidVersion >= Build.VERSION_CODES.TIRAMISU


    fun clearEvent() {
        eventFlow.update { OnBoardingTipsEvent.Unknown }
    }
}
