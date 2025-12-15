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

package proton.android.pass.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import me.proton.core.domain.entity.UserId
import proton.android.pass.appconfig.api.AppConfig
import proton.android.pass.appconfig.api.BuildFlavor.Companion.supportPayment
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.common.api.combineN
import proton.android.pass.common.api.getOrNull
import proton.android.pass.common.api.map
import proton.android.pass.data.api.repositories.ItemSyncStatusRepository
import proton.android.pass.data.api.repositories.SyncMode
import proton.android.pass.data.api.usecases.GetUserPlan
import proton.android.pass.data.api.usecases.ObserveConfirmedInviteToken
import proton.android.pass.data.api.usecases.inappmessages.ObserveDeliverableModalInAppMessages
import proton.android.pass.data.api.usecases.inappmessages.ObserveDeliverablePromoInAppMessages
import proton.android.pass.domain.InviteToken
import proton.android.pass.domain.inappmessages.InAppMessage
import proton.android.pass.domain.inappmessages.InAppMessageId
import proton.android.pass.preferences.FeatureFlag
import proton.android.pass.preferences.FeatureFlagsPreferencesRepository
import proton.android.pass.preferences.HasCompletedOnBoarding
import proton.android.pass.preferences.UserPreferencesRepository
import javax.inject.Inject

@HiltViewModel
class RouterViewModel @Inject constructor(
    userPreferencesRepository: UserPreferencesRepository,
    observeConfirmedInviteToken: ObserveConfirmedInviteToken,
    itemSyncStatusRepository: ItemSyncStatusRepository,
    observeDeliverableModalInAppMessages: ObserveDeliverableModalInAppMessages,
    observeDeliverablePromoInAppMessages: ObserveDeliverablePromoInAppMessages,
    appConfig: AppConfig,
    getUserPlan: GetUserPlan,
    featureFlagsPreferencesRepository: FeatureFlagsPreferencesRepository
) : ViewModel() {


    internal val routerEventState = MutableSharedFlow<RouterEvent>(replay = 1)

    init {
        combineN(
            userPreferencesRepository.getHasCompletedOnBoarding(),
            observeConfirmedInviteToken()
                .onEach { inviteTokenOption ->
                    if (inviteTokenOption is Some) {
                        observeConfirmedInviteToken.clear()
                    }
                }
                .distinctUntilChanged(),
            itemSyncStatusRepository.observeMode().distinctUntilChanged(),
            observeDeliverableModalInAppMessages(),
            observeDeliverablePromoInAppMessages(),
            flowOf(appConfig.flavor.supportPayment()),
            getUserPlan()
                .asLoadingResult()
                .flatMapLatest {
                    flowOf(it.map { plan -> plan.isFreePlan }.getOrNull() != false)
                },
            featureFlagsPreferencesRepository.get<Boolean>(FeatureFlag.PASS_MOBILE_ON_BOARDING_V2),
            ::routerEvent
        )
            .distinctUntilChanged()
            .onEach { routerEventState.emit(it) }
            .launchIn(viewModelScope)
    }

    internal fun clearEvent() {
        viewModelScope.launch {
            routerEventState.emit(RouterEvent.None)
        }
    }

    @SuppressWarnings("LongParameterList")
    private fun routerEvent(
        hasCompletedOnBoarding: HasCompletedOnBoarding,
        inviteTokenOption: Option<InviteToken>,
        syncMode: SyncMode,
        modalOption: InAppMessage.Modal?,
        promoOption: InAppMessage.Promo?,
        supportPayment: Boolean,
        isFreePlan: Boolean,
        isOnboardingV2Enable: Boolean
    ): RouterEvent = when {
        inviteTokenOption is Some -> RouterEvent.ConfirmedInvite(inviteTokenOption.value)
        hasCompletedOnBoarding == HasCompletedOnBoarding.NotCompleted -> {
            RouterEvent.OnBoarding(
                supportPayment = supportPayment,
                isFreePlan = isFreePlan,
                isOnboardingV2Enable = isOnboardingV2Enable
            )
        }

        syncMode == SyncMode.ShownToUser -> RouterEvent.SyncDialog
        promoOption != null ->
            RouterEvent.InAppPromo(promoOption.userId, promoOption.id)

        modalOption != null ->
            RouterEvent.InAppModal(modalOption.userId, modalOption.id)

        else -> RouterEvent.None
    }
}

sealed interface RouterEvent {

    data class OnBoarding(
        val supportPayment: Boolean,
        val isFreePlan: Boolean,
        val isOnboardingV2Enable: Boolean
    ) :
        RouterEvent

    data object SyncDialog : RouterEvent

    @JvmInline
    value class ConfirmedInvite(val inviteToken: InviteToken) : RouterEvent

    data class InAppPromo(val userId: UserId, val inAppMessageId: InAppMessageId) : RouterEvent

    data class InAppModal(val userId: UserId, val inAppMessageId: InAppMessageId) : RouterEvent

    data object None : RouterEvent

}
