/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.features.sharing.manage.item.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.common.api.FlowUtils.oneShot
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.api.usecases.LeaveShare
import proton.android.pass.data.api.usecases.shares.ObserveShare
import proton.android.pass.data.api.usecases.shares.ObserveShareMembers
import proton.android.pass.data.api.usecases.shares.ObserveSharePendingInvites
import proton.android.pass.domain.ShareId
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import javax.inject.Inject

@HiltViewModel
class ManageItemViewModel @Inject constructor(
    savedStateHandleProvider: SavedStateHandleProvider,
    observeShare: ObserveShare,
    observeShareMembers: ObserveShareMembers,
    observeSharePendingInvites: ObserveSharePendingInvites,
    private val leaveShare: LeaveShare
) : ViewModel() {

    private val shareId: ShareId = savedStateHandleProvider.get()
        .require<String>(CommonNavArgId.ShareId.key)
        .let(::ShareId)

    private val eventFlow = MutableStateFlow<ManageItemEvent>(ManageItemEvent.Idle)

    private val shareFlow = oneShot { observeShare(shareId).first() }

    private val sharePendingInvitesFlow = shareFlow.flatMapLatest { share ->
        if (share.isAdmin) {
            observeSharePendingInvites(shareId)
        } else {
            flowOf(emptyList())
        }
    }

    private val isLoadingStateFlow = MutableStateFlow<IsLoadingState>(IsLoadingState.NotLoading)

    internal val stateFlow = combine(
        eventFlow,
        shareFlow,
        observeShareMembers(shareId),
        sharePendingInvitesFlow,
        isLoadingStateFlow,
        ManageItemState::Success
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = ManageItemState.Loading
    )

    internal fun onConsumeEvent(event: ManageItemEvent) {
        eventFlow.compareAndSet(event, ManageItemEvent.Idle)
    }

    internal fun onLeaveShare() {
        viewModelScope.launch {
            isLoadingStateFlow.update { IsLoadingState.Loading }

            runCatching { leaveShare(shareId) }
                .onFailure { error ->
                    PassLogger.w(TAG, "There was an error leaving item share")
                    PassLogger.w(TAG, error)
                }
                .onSuccess {
                    eventFlow.update { ManageItemEvent.OnShareLeaveSuccess }
                }

            isLoadingStateFlow.update { IsLoadingState.NotLoading }
        }
    }

    private companion object {

        private const val TAG = "ManageItemViewModel"

    }

}
