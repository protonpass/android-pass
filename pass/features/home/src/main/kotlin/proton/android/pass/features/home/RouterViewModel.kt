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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.data.api.repositories.ItemSyncStatusRepository
import proton.android.pass.data.api.repositories.SyncMode
import proton.android.pass.data.api.usecases.ObserveConfirmedInviteToken
import proton.android.pass.domain.InviteToken
import proton.android.pass.preferences.HasCompletedOnBoarding
import proton.android.pass.preferences.UserPreferencesRepository
import javax.inject.Inject

@HiltViewModel
class RouterViewModel @Inject constructor(
    userPreferencesRepository: UserPreferencesRepository,
    observeConfirmedInviteToken: ObserveConfirmedInviteToken,
    itemSyncStatusRepository: ItemSyncStatusRepository
) : ViewModel() {

    internal val routerEventState = MutableSharedFlow<RouterEvent>(replay = 1)

    init {
        combine(
            userPreferencesRepository.getHasCompletedOnBoarding(),
            observeConfirmedInviteToken()
                .onEach { inviteTokenOption ->
                    if (inviteTokenOption is Some) {
                        observeConfirmedInviteToken.clear()
                    }
                }
                .distinctUntilChanged(),
            itemSyncStatusRepository.observeMode().distinctUntilChanged(),
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

    private fun routerEvent(
        hasCompletedOnBoarding: HasCompletedOnBoarding,
        inviteTokenOption: Option<InviteToken>,
        syncMode: SyncMode
    ): RouterEvent = when {
        inviteTokenOption is Some -> RouterEvent.ConfirmedInvite(inviteTokenOption.value)
        hasCompletedOnBoarding == HasCompletedOnBoarding.NotCompleted -> RouterEvent.OnBoarding
        syncMode == SyncMode.ShownToUser -> RouterEvent.SyncDialog
        else -> RouterEvent.None
    }
}

sealed interface RouterEvent {

    data object OnBoarding : RouterEvent

    data object SyncDialog : RouterEvent

    @JvmInline
    value class ConfirmedInvite(val inviteToken: InviteToken) : RouterEvent

    data object None : RouterEvent

}
