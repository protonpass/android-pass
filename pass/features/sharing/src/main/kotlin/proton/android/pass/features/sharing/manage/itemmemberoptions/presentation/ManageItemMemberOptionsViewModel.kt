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

package proton.android.pass.features.sharing.manage.itemmemberoptions.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.data.api.usecases.RemoveShareMember
import proton.android.pass.data.api.usecases.shares.UpdateShareMemberRole
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareRole
import proton.android.pass.features.sharing.SharingSnackbarMessage
import proton.android.pass.features.sharing.manage.bottomsheet.MemberShareIdArg
import proton.android.pass.features.sharing.manage.bottomsheet.ShareRoleArg
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import javax.inject.Inject

@HiltViewModel
class ManageItemMemberOptionsViewModel @Inject constructor(
    savedStateHandleProvider: SavedStateHandleProvider,
    private val updateShareMemberRole: UpdateShareMemberRole,
    private val removeShareMember: RemoveShareMember,
    private val snackbarDispatcher: SnackbarDispatcher
) : ViewModel() {

    private val shareId: ShareId = savedStateHandleProvider.get()
        .require<String>(CommonNavArgId.ShareId.key)
        .let(::ShareId)

    private val memberShareRole: ShareRole = savedStateHandleProvider.get()
        .require<String>(ShareRoleArg.key)
        .let(ShareRole::fromValue)

    private val memberShareId: ShareId = savedStateHandleProvider.get()
        .require<String>(MemberShareIdArg.key)
        .let(::ShareId)

    private val actionFlow = MutableStateFlow<ManageItemMemberOptionsAction>(
        value = ManageItemMemberOptionsAction.None
    )

    private val eventFlow = MutableStateFlow<ManageItemMemberOptionsEvent>(
        value = ManageItemMemberOptionsEvent.Idle
    )

    internal val stateFlow: StateFlow<ManageItemMemberOptionsState> = combine(
        flowOf(memberShareRole),
        actionFlow,
        eventFlow,
        ::ManageItemMemberOptionsState
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
        initialValue = ManageItemMemberOptionsState.initial(memberShareRole)
    )

    internal fun onConsumeEvent(event: ManageItemMemberOptionsEvent) {
        eventFlow.compareAndSet(event, ManageItemMemberOptionsEvent.Idle)
    }

    internal fun onUpdateMemberRole(newMemberRole: ShareRole) {
        if (memberShareRole == newMemberRole) {
            eventFlow.update { ManageItemMemberOptionsEvent.OnUpdateMemberRoleSuccess }
            return
        }

        viewModelScope.launch {
            actionFlow.update {
                when (newMemberRole) {
                    ShareRole.Admin -> ManageItemMemberOptionsAction.SetAdmin
                    ShareRole.Read -> ManageItemMemberOptionsAction.SetViewer
                    ShareRole.Write -> ManageItemMemberOptionsAction.SetEditor
                    is ShareRole.Custom -> it
                }
            }

            runCatching { updateShareMemberRole(shareId, memberShareId, newMemberRole) }
                .onFailure { error ->
                    PassLogger.w(TAG, "There was an error updating member role")
                    PassLogger.w(TAG, error)

                    eventFlow.update { ManageItemMemberOptionsEvent.OnUpdateMemberRoleFailure }
                    snackbarDispatcher(SharingSnackbarMessage.ChangeMemberPermissionError)
                }
                .onSuccess {
                    eventFlow.update { ManageItemMemberOptionsEvent.OnUpdateMemberRoleSuccess }
                }

            actionFlow.update { ManageItemMemberOptionsAction.None }
        }
    }

    internal fun onRevokeMemberAccess() {
        viewModelScope.launch {
            actionFlow.update { ManageItemMemberOptionsAction.RevokeAccess }

            runCatching { removeShareMember(shareId, memberShareId) }
                .onFailure { error ->
                    PassLogger.w(TAG, "There was an error revoking member access")
                    PassLogger.w(TAG, error)

                    eventFlow.update { ManageItemMemberOptionsEvent.OnRevokeMemberAccessFailure }
                    snackbarDispatcher(SharingSnackbarMessage.RemoveMemberError)
                }
                .onSuccess {
                    eventFlow.update { ManageItemMemberOptionsEvent.OnRevokeMemberAccessSuccess }
                }

            actionFlow.update { ManageItemMemberOptionsAction.None }
        }
    }

    private companion object {

        private const val TAG = "ManageItemMemberOptionsViewModel"

    }

}
