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

package proton.android.pass.featuresharing.impl.manage

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.common.api.combineN
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.data.api.usecases.ConfirmNewUserInvite
import proton.android.pass.data.api.usecases.GetVaultMembers
import proton.android.pass.data.api.usecases.GetVaultWithItemCountById
import proton.android.pass.data.api.usecases.VaultMember
import proton.android.pass.data.api.usecases.capabilities.CanShareVault
import proton.android.pass.data.api.usecases.capabilities.CanShareVaultStatus
import proton.android.pass.domain.NewUserInviteId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.SharePermissionFlag
import proton.android.pass.domain.VaultWithItemCount
import proton.android.pass.domain.hasFlag
import proton.android.pass.domain.toPermissions
import proton.android.pass.featuresharing.impl.SharingSnackbarMessage
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import javax.inject.Inject

@HiltViewModel
class ManageVaultViewModel @Inject constructor(
    getVaultMembers: GetVaultMembers,
    getVaultById: GetVaultWithItemCountById,
    savedStateHandleProvider: SavedStateHandleProvider,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val canShareVault: CanShareVault,
    private val confirmNewUserInvite: ConfirmNewUserInvite
) : ViewModel() {

    private val navShareId: ShareId =
        ShareId(savedStateHandleProvider.get().require(CommonNavArgId.ShareId.key))

    private val refreshFlow: MutableStateFlow<Boolean> = MutableStateFlow(true)

    private val membersFlow: Flow<LoadingResult<List<VaultMember>>> = refreshFlow
        .filter { it }
        .flatMapLatest {
            getVaultMembers(navShareId).asLoadingResult()
        }
        .onEach { refreshFlow.update { false } }
        .distinctUntilChanged()

    private val eventFlow: MutableStateFlow<ManageVaultEvent> =
        MutableStateFlow(ManageVaultEvent.Unknown)

    private val vaultFlow: Flow<VaultWithItemCount> = getVaultById(shareId = navShareId)
        .catch {
            snackbarDispatcher(SharingSnackbarMessage.GetMembersInfoError)
            eventFlow.update { ManageVaultEvent.Close }
        }
        .distinctUntilChanged()

    private val showShareButtonFlow: Flow<CanShareVaultStatus> = vaultFlow
        .map { canShareVault(it.vault) }
        .distinctUntilChanged()

    private val canEditFlow: Flow<Boolean> = vaultFlow
        .map { it.vault.role.toPermissions().hasFlag(SharePermissionFlag.Admin) }
        .distinctUntilChanged()

    private val invitesBeingConfirmedMutableFlow: MutableStateFlow<Set<NewUserInviteId>> =
        MutableStateFlow(emptySet())

    private val invitesBeingConfirmedFlow: Flow<Set<NewUserInviteId>> =
        invitesBeingConfirmedMutableFlow

    val state: StateFlow<ManageVaultUiState> = combineN(
        membersFlow,
        vaultFlow,
        showShareButtonFlow,
        canEditFlow,
        eventFlow,
        invitesBeingConfirmedFlow.distinctUntilChanged()
    ) { vaultMembers, vault, showShareButton, canEdit, event, invitesBeingConfirmed ->
        val content = when (vaultMembers) {
            is LoadingResult.Error -> ManageVaultUiContent.Loading
            LoadingResult.Loading -> ManageVaultUiContent.Loading
            is LoadingResult.Success -> {
                val partitioned = partitionMembers(vaultMembers.data)

                ManageVaultUiContent.Content(
                    vaultMembers = partitioned.members,
                    invites = partitioned.invites,
                    loadingInvites = invitesBeingConfirmed.toImmutableSet(),
                    canEdit = canEdit
                )
            }
        }

        val sharingOptions = when (showShareButton) {
            is CanShareVaultStatus.CanShare -> {
                ShareOptions.Show(
                    enableButton = true,
                    subtitle = ShareOptions.ShareOptionsSubtitle.RemainingInvites(
                        remainingInvites = showShareButton.invitesRemaining
                    )
                )
            }
            is CanShareVaultStatus.CannotShare -> {
                when (showShareButton.reason) {
                    CanShareVaultStatus.CannotShareReason.NotEnoughInvites -> {
                        ShareOptions.Show(
                            enableButton = false,
                            subtitle = ShareOptions.ShareOptionsSubtitle.LimitReached
                        )
                    }
                    CanShareVaultStatus.CannotShareReason.NotEnoughPermissions -> ShareOptions.Hide
                    CanShareVaultStatus.CannotShareReason.SharingDisabled -> ShareOptions.Hide
                    CanShareVaultStatus.CannotShareReason.Unknown -> ShareOptions.Hide
                }
            }
        }

        ManageVaultUiState(
            vault = vault,
            content = content,
            event = event,
            shareOptions = sharingOptions
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = ManageVaultUiState.Initial
    )

    fun refresh() = viewModelScope.launch {
        refreshFlow.update { true }
    }

    fun clearEvent() {
        eventFlow.update { ManageVaultEvent.Unknown }
    }

    fun onConfirmInvite(invite: VaultMember.NewUserInvitePending) = viewModelScope.launch {
        val inviteId = invite.newUserInviteId
        val isAlreadyRunning = invitesBeingConfirmedMutableFlow.value.contains(inviteId)
        if (isAlreadyRunning) return@launch

        invitesBeingConfirmedMutableFlow.update { it + inviteId }

        PassLogger.i(TAG, "Confirming invite ${inviteId.value}")
        confirmNewUserInvite(
            shareId = navShareId,
            invite = invite
        ).onSuccess {
            PassLogger.i(TAG, "Confirmed invite ${inviteId.value}")
            snackbarDispatcher(SharingSnackbarMessage.ConfirmInviteSuccess)
            refreshFlow.update { true }
        }.onFailure {
            PassLogger.w(TAG, "Error confirming invite ${inviteId.value}")
            PassLogger.w(TAG, it)
            snackbarDispatcher(SharingSnackbarMessage.ConfirmInviteError)
        }

        invitesBeingConfirmedMutableFlow.update {
            val asMutable = it.toMutableSet()
            asMutable.remove(inviteId)
            asMutable
        }
    }

    fun onPendingInvitesClick() = viewModelScope.launch {
        eventFlow.update { ManageVaultEvent.ShowInvitesInfo(navShareId) }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    fun partitionMembers(members: List<VaultMember>): PartitionedMembers {
        val invites = mutableListOf<VaultMember>()
        val membersList = mutableListOf<VaultMember.Member>()
        members.forEach {
            when (it) {
                is VaultMember.Member -> membersList.add(it)
                else -> invites.add(it)
            }
        }

        return PartitionedMembers(
            members = membersList.toPersistentList(),
            invites = sortInvites(invites).toPersistentList()
        )
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    fun sortInvites(invites: List<VaultMember>): List<VaultMember> {
        val newUserInvitesWaitingActivation = mutableListOf<VaultMember.NewUserInvitePending>()
        val newUserInvitesWaitingAccountCreation = mutableListOf<VaultMember.NewUserInvitePending>()
        val regularInvites = mutableListOf<VaultMember.InvitePending>()

        invites.forEach {
            when (it) {
                is VaultMember.NewUserInvitePending -> when (it.inviteState) {
                    VaultMember.NewUserInvitePending.InviteState.PendingAcceptance -> {
                        newUserInvitesWaitingActivation.add(it)
                    }

                    VaultMember.NewUserInvitePending.InviteState.PendingAccountCreation -> {
                        newUserInvitesWaitingAccountCreation.add(it)
                    }
                }

                is VaultMember.InvitePending -> regularInvites.add(it)
                else -> {}
            }
        }

        return newUserInvitesWaitingActivation
            .plus(newUserInvitesWaitingAccountCreation)
            .plus(regularInvites)
    }

    data class PartitionedMembers(
        val members: ImmutableList<VaultMember.Member>,
        val invites: ImmutableList<VaultMember>
    )

    companion object {
        private const val TAG = "ManageVaultViewModel"
    }

}
