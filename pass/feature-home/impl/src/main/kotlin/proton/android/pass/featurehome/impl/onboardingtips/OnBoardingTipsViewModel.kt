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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import proton.android.pass.autofill.api.AutofillManager
import proton.android.pass.autofill.api.AutofillStatus
import proton.android.pass.autofill.api.AutofillSupportedStatus
import proton.android.pass.data.api.usecases.GetUserPlan
import proton.android.pass.data.api.usecases.ObserveInvites
import proton.android.pass.featurehome.impl.onboardingtips.OnBoardingTipPage.AUTOFILL
import proton.android.pass.featurehome.impl.onboardingtips.OnBoardingTipPage.INVITE
import proton.android.pass.featurehome.impl.onboardingtips.OnBoardingTipPage.TRIAL
import proton.android.pass.preferences.HasDismissedAutofillBanner
import proton.android.pass.preferences.HasDismissedTrialBanner
import proton.android.pass.preferences.UserPreferencesRepository
import proton.pass.domain.PlanType
import javax.inject.Inject

@HiltViewModel
class OnBoardingTipsViewModel @Inject constructor(
    private val autofillManager: AutofillManager,
    private val preferencesRepository: UserPreferencesRepository,
    observeInvites: ObserveInvites,
    getUserPlan: GetUserPlan
) : ViewModel() {

    private val eventFlow: MutableStateFlow<OnBoardingTipsEvent> =
        MutableStateFlow(OnBoardingTipsEvent.Unknown)

    private val hasInvitesFlow: Flow<Boolean> = observeInvites()
        .map { invites -> invites.isNotEmpty() }
        .distinctUntilChanged()

    private val shouldShowAutofillFlow: Flow<Boolean> = combine(
        autofillManager.getAutofillStatus(),
        preferencesRepository.getHasDismissedAutofillBanner(),
        hasInvitesFlow
    ) { autofillStatus, hasDismissedAutofillBanner, hasInvites ->
        if (hasInvites) {
            true
        } else {
            shouldShowBanner(
                autofillSupportedStatus = autofillStatus,
                hasDismissedAutofillBanner = hasDismissedAutofillBanner
            )
        }
    }

    private val shouldShowTrialFlow: Flow<Boolean> = preferencesRepository
        .getHasDismissedTrialBanner()
        .map { pref ->
            when (pref) {
                HasDismissedTrialBanner.Dismissed -> false
                HasDismissedTrialBanner.NotDismissed -> true
            }
        }
        .distinctUntilChanged()

    val state: StateFlow<OnBoardingTipsUiState> = combine(
        shouldShowAutofillFlow,
        shouldShowTrialFlow,
        getUserPlan(),
        eventFlow,
        hasInvitesFlow
    ) { shouldShowAutofill, shouldShowTrial, userPlan, event, hasInvites ->

        val tips = getTips(
            planType = userPlan.planType,
            shouldShowTrial = shouldShowTrial,
            shouldShowAutofill = shouldShowAutofill,
            hasInvites
        )

        OnBoardingTipsUiState(
            tipsToShow = tips,
            event = event
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = runBlocking {
                val userPlan = getUserPlan().first()

                val tips = getTips(
                    planType = userPlan.planType,
                    shouldShowTrial = shouldShowTrialFlow.first(),
                    shouldShowAutofill = shouldShowAutofillFlow.first(),
                    hasInvites = hasInvitesFlow.first()
                )
                OnBoardingTipsUiState(tips)
            }
        )

    private fun getTips(
        planType: PlanType,
        shouldShowTrial: Boolean,
        shouldShowAutofill: Boolean,
        hasInvites: Boolean
    ): ImmutableSet<OnBoardingTipPage> {
        val isTrial = planType is PlanType.Trial
        return when {
            hasInvites -> persistentSetOf(INVITE)
            isTrial && shouldShowTrial -> persistentSetOf(TRIAL)
            shouldShowAutofill -> persistentSetOf(AUTOFILL)
            else -> persistentSetOf()
        }
    }

    private fun shouldShowBanner(
        autofillSupportedStatus: AutofillSupportedStatus,
        hasDismissedAutofillBanner: HasDismissedAutofillBanner
    ): Boolean =
        autofillSupportedStatus is AutofillSupportedStatus.Supported &&
            autofillSupportedStatus.status !is AutofillStatus.EnabledByOurService &&
            hasDismissedAutofillBanner is HasDismissedAutofillBanner.NotDismissed

    fun onClick(onBoardingTipPage: OnBoardingTipPage) {
        when (onBoardingTipPage) {
            AUTOFILL -> autofillManager.openAutofillSelector()
            TRIAL -> eventFlow.update { OnBoardingTipsEvent.OpenTrialScreen }
            INVITE -> eventFlow.update { OnBoardingTipsEvent.OpenInviteScreen }
        }
    }

    fun onDismiss(onBoardingTipPage: OnBoardingTipPage) = viewModelScope.launch {
        when (onBoardingTipPage) {
            AUTOFILL ->
                preferencesRepository.setHasDismissedAutofillBanner(HasDismissedAutofillBanner.Dismissed)

            TRIAL ->
                preferencesRepository.setHasDismissedTrialBanner(HasDismissedTrialBanner.Dismissed)

            INVITE -> {} // Invites cannot be dismissed
        }
    }

    fun clearEvent() {
        eventFlow.update { OnBoardingTipsEvent.Unknown }
    }
}
