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

package proton.android.pass.features.sharing.accept

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.common.api.FlowUtils.oneShot
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.data.api.errors.CannotCreateMoreVaultsError
import proton.android.pass.data.api.usecases.AcceptInvite
import proton.android.pass.data.api.usecases.AcceptInviteStatus
import proton.android.pass.data.api.usecases.GetItemById
import proton.android.pass.data.api.usecases.RejectInvite
import proton.android.pass.data.api.usecases.invites.ObserveInvite
import proton.android.pass.domain.InviteId
import proton.android.pass.domain.InviteToken
import proton.android.pass.domain.PendingInvite
import proton.android.pass.domain.ShareType
import proton.android.pass.features.sharing.SharingSnackbarMessage
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import javax.inject.Inject

@HiltViewModel
class AcceptInviteViewModel @Inject constructor(
    savedStateHandleProvider: SavedStateHandleProvider,
    observeInvite: ObserveInvite,
    private val acceptInvite: AcceptInvite,
    private val rejectInvite: RejectInvite,
    private val getItemById: GetItemById,
    private val snackbarDispatcher: SnackbarDispatcher
) : ViewModel() {

    private val inviteToken: InviteToken? = savedStateHandleProvider.get()
        .get<String>(CommonOptionalNavArgId.InviteToken.key)
        ?.let(::InviteToken)
    private val inviteId: InviteId? = savedStateHandleProvider.get()
        .get<String>(CommonOptionalNavArgId.InviteId.key)
        ?.let(::InviteId)

    private val progressFlow: MutableStateFlow<AcceptInviteProgress> = MutableStateFlow(
        value = AcceptInviteProgress.Pending
    )

    private val eventFlow: MutableStateFlow<AcceptInviteEvent> = MutableStateFlow(
        value = AcceptInviteEvent.Idle
    )

    private val pendingUserInviteOptionFlow: Flow<Option<PendingInvite>> = oneShot {
        when {
            inviteToken != null -> observeInvite(inviteToken).first()
            inviteId != null -> observeInvite(inviteId).first()
            else -> None
        }
    }

    internal val stateFlow: StateFlow<AcceptInviteState> = combine(
        pendingUserInviteOptionFlow,
        progressFlow,
        eventFlow
    ) { pendingInviteOption, progress, event ->
        when (pendingInviteOption) {
            None -> AcceptInviteState.Initial
            is Some -> when (val pendingInvite = pendingInviteOption.value) {
                is PendingInvite.UserItem, is PendingInvite.GroupItem -> AcceptInviteState.ItemInvite(
                    progress = progress,
                    event = event,
                    pendingItemInvite = pendingInvite
                )

                is PendingInvite.UserVault, is PendingInvite.GroupVault -> AcceptInviteState.VaultInvite(
                    progress = progress,
                    event = event,
                    pendingVaultInvite = pendingInvite
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = AcceptInviteState.Initial
    )

    internal fun onConsumeEvent(event: AcceptInviteEvent) {
        eventFlow.compareAndSet(event, AcceptInviteEvent.Idle)
    }

    @Suppress("LongMethod")
    internal fun onAcceptInvite(shareType: ShareType) {
        viewModelScope.launch {
            when {
                inviteToken != null -> acceptInvite(inviteToken)
                inviteId != null -> acceptInvite(inviteId)
                else -> error("No id defined")
            }
                .catch { error ->
                    PassLogger.w(TAG, "There was an error accepting invite")
                    PassLogger.w(TAG, error)
                    eventFlow.update { AcceptInviteEvent.Close }

                    if (error is CannotCreateMoreVaultsError) {
                        SharingSnackbarMessage.InviteAcceptErrorCannotCreateMoreVaults
                    } else {
                        SharingSnackbarMessage.InviteAcceptError
                    }.also { errorMessage -> snackbarDispatcher(errorMessage) }
                }
                .distinctUntilChanged()
                .collect { acceptInviteStatus ->
                    when (acceptInviteStatus) {
                        AcceptInviteStatus.AcceptingInvite -> {
                            PassLogger.d(TAG, "Accepting invite")
                            progressFlow.update { AcceptInviteProgress.Accepting }
                        }

                        is AcceptInviteStatus.DownloadingItems -> {
                            PassLogger.d(TAG, "Downloading invite items")
                            progressFlow.update {
                                AcceptInviteProgress.Downloading(
                                    downloaded = acceptInviteStatus.downloaded,
                                    total = acceptInviteStatus.total
                                )
                            }
                        }

                        is AcceptInviteStatus.GroupInviteDone -> {
                            PassLogger.i(TAG, "Invite successfully accepted")
                            snackbarDispatcher(SharingSnackbarMessage.InviteAccepted)
                            eventFlow.update { AcceptInviteEvent.Close }
                        }
                        is AcceptInviteStatus.UserInviteDone -> {
                            PassLogger.i(TAG, "Invite successfully accepted")
                            when (shareType) {
                                ShareType.Item -> {
                                    getItemById(
                                        shareId = acceptInviteStatus.shareId,
                                        itemId = acceptInviteStatus.itemId
                                    ).let { item ->
                                        AcceptInviteEvent.OnItemInviteAcceptSuccess(
                                            shareId = item.shareId,
                                            itemId = item.id,
                                            itemCategory = item.itemType.category
                                        )
                                    }
                                }

                                ShareType.Vault -> AcceptInviteEvent.OnVaultInviteAcceptSuccess(
                                    shareId = acceptInviteStatus.shareId
                                )
                            }.also { acceptInviteEvent -> eventFlow.update { acceptInviteEvent } }

                            snackbarDispatcher(SharingSnackbarMessage.InviteAccepted)
                        }

                        AcceptInviteStatus.Error -> {
                            PassLogger.w(TAG, "There was an error accepting invite")
                            eventFlow.update { AcceptInviteEvent.Close }
                            snackbarDispatcher(SharingSnackbarMessage.InviteAcceptError)
                        }
                    }
                }
        }
    }

    internal fun onRejectInvite() {
        viewModelScope.launch {
            progressFlow.update { AcceptInviteProgress.Rejecting }

            runCatching {
                when {
                    inviteToken != null -> rejectInvite(inviteToken)
                    inviteId != null -> rejectInvite(inviteId)
                    else -> error("No id defined")
                }
            }
                .onFailure { error ->
                    PassLogger.w(TAG, "There was an error rejecting invite")
                    PassLogger.w(TAG, error)
                    snackbarDispatcher(SharingSnackbarMessage.InviteRejectError)
                }
                .onSuccess {
                    PassLogger.i(TAG, "Invite successfully rejected")
                    snackbarDispatcher(SharingSnackbarMessage.InviteRejected)
                }

            eventFlow.update { AcceptInviteEvent.Close }
        }
    }

    private companion object {

        private const val TAG = "AcceptInviteViewModel"

    }

}
