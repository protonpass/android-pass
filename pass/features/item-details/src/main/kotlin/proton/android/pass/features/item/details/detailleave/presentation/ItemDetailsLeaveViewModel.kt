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

package proton.android.pass.features.item.details.detailleave.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.api.usecases.LeaveShare
import proton.android.pass.domain.ShareId
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import javax.inject.Inject

@HiltViewModel
class ItemDetailsLeaveViewModel @Inject constructor(
    savedStateHandleProvider: SavedStateHandleProvider,
    private val leaveShare: LeaveShare,
    private val snackbarDispatcher: SnackbarDispatcher
) : ViewModel() {

    private val shareId: ShareId = savedStateHandleProvider.get()
        .require<String>(CommonNavArgId.ShareId.key)
        .let(::ShareId)

    private val eventFlow = MutableStateFlow<ItemDetailsLeaveEvent>(ItemDetailsLeaveEvent.Idle)

    private val isLoadingStateFlow = MutableStateFlow<IsLoadingState>(IsLoadingState.NotLoading)

    internal val stateFlow: StateFlow<ItemDetailsLeaveState> = combine(
        eventFlow,
        isLoadingStateFlow,
        ::ItemDetailsLeaveState
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
        initialValue = ItemDetailsLeaveState.Initial
    )

    internal fun onConsumeEvent(event: ItemDetailsLeaveEvent) {
        eventFlow.compareAndSet(event, ItemDetailsLeaveEvent.Idle)
    }

    internal fun onLeaveShare() {
        viewModelScope.launch {
            isLoadingStateFlow.update { IsLoadingState.Loading }

            runCatching { leaveShare(shareId) }
                .onFailure { error ->
                    PassLogger.w(TAG, "There was an error leaving the share")
                    PassLogger.w(TAG, error)

                    eventFlow.update { ItemDetailsLeaveEvent.OnLeaveShareError }
                    snackbarDispatcher(ItemDetailsLeaveMessage.LeaveItemError)
                }
                .onSuccess {
                    eventFlow.update { ItemDetailsLeaveEvent.OnLeaveShareError }
                    snackbarDispatcher(ItemDetailsLeaveMessage.LeaveItemSuccess)
                }

            isLoadingStateFlow.update { IsLoadingState.NotLoading }
        }
    }

    private companion object {

        private const val TAG = "ItemDetailsLeaveViewModel"

    }

}
