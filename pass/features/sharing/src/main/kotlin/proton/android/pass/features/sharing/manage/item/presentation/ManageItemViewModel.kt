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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import proton.android.pass.common.api.FlowUtils.oneShot
import proton.android.pass.common.api.combineN
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.api.usecases.shares.ObserveShare
import proton.android.pass.data.api.usecases.shares.ObserveShareItemMembers
import proton.android.pass.data.api.usecases.shares.ObserveShareItemsCount
import proton.android.pass.data.api.usecases.shares.ObserveSharePendingInvites
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.features.sharing.SharingSnackbarMessage
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import javax.inject.Inject

@HiltViewModel
class ManageItemViewModel @Inject constructor(
    savedStateHandleProvider: SavedStateHandleProvider,
    observeShare: ObserveShare,
    observeShareItemMembers: ObserveShareItemMembers,
    observeSharePendingInvites: ObserveSharePendingInvites,
    observeShareItemsCount: ObserveShareItemsCount,
    private val snackbarDispatcher: SnackbarDispatcher
) : ViewModel() {

    private val shareId: ShareId = savedStateHandleProvider.get()
        .require<String>(CommonNavArgId.ShareId.key)
        .let(::ShareId)

    private val itemId: ItemId = savedStateHandleProvider.get()
        .require<String>(CommonNavArgId.ItemId.key)
        .let(::ItemId)

    private val eventFlow = MutableStateFlow<ManageItemEvent>(ManageItemEvent.Idle)

    private val shareFlow = oneShot { observeShare(shareId).first() }

    private val shareItemMembersFlow = observeShareItemMembers(shareId, itemId)
        .catch { error ->
            PassLogger.w(TAG, "There was an error observing share members")
            PassLogger.w(TAG, error)

            snackbarDispatcher(SharingSnackbarMessage.FetchMembersError)
            eventFlow.update { ManageItemEvent.OnShareManagementError }
            emit(emptyList())
        }

    private val sharePendingInvitesFlow = observeSharePendingInvites(shareId, itemId)
        .catch { error ->
            PassLogger.w(TAG, "There was an error observing share pending invites")
            PassLogger.w(TAG, error)

            snackbarDispatcher(SharingSnackbarMessage.FetchPendingInvitesError)
            eventFlow.update { ManageItemEvent.OnShareManagementError }
            emit(emptyList())
        }

    private val isLoadingStateFlow = MutableStateFlow<IsLoadingState>(IsLoadingState.NotLoading)

    internal val stateFlow: StateFlow<ManageItemState> = combineN(
        eventFlow,
        flowOf(itemId),
        shareFlow,
        sharePendingInvitesFlow,
        observeShareItemsCount(shareId),
        shareItemMembersFlow,
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

    private companion object {

        private const val TAG = "ManageItemViewModel"

    }

}
