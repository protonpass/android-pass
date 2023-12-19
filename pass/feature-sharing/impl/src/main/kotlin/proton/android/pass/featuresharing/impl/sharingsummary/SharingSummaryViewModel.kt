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

package proton.android.pass.featuresharing.impl.sharingsummary

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
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.api.usecases.GetVaultWithItemCountById
import proton.android.pass.data.api.usecases.InviteToVault
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareRole
import proton.android.pass.featuresharing.impl.EmailNavArgId
import proton.android.pass.featuresharing.impl.PermissionNavArgId
import proton.android.pass.featuresharing.impl.SharingSnackbarMessage.InviteSentError
import proton.android.pass.featuresharing.impl.SharingSnackbarMessage.InviteSentSuccess
import proton.android.pass.featuresharing.impl.SharingSnackbarMessage.VaultNotFound
import proton.android.pass.featuresharing.impl.SharingWithUserModeArgId
import proton.android.pass.featuresharing.impl.SharingWithUserModeType
import proton.android.pass.featuresharing.impl.sharingpermissions.SharingType
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import javax.inject.Inject

@HiltViewModel
class SharingSummaryViewModel @Inject constructor(
    private val inviteToVault: InviteToVault,
    private val snackbarDispatcher: SnackbarDispatcher,
    getVaultWithItemCountById: GetVaultWithItemCountById,
    savedStateHandleProvider: SavedStateHandleProvider,
) : ViewModel() {

    private val shareId: ShareId = ShareId(
        savedStateHandleProvider.get().require(CommonNavArgId.ShareId.key)
    )
    private val email: String = savedStateHandleProvider.get().require(EmailNavArgId.key)
    private val sharingType: SharingType = SharingType
        .values()[savedStateHandleProvider.get().require(PermissionNavArgId.key)]
    private val userMode: SharingWithUserModeType = SharingWithUserModeType
        .values()
        .first {
            it.name == savedStateHandleProvider.get().require(SharingWithUserModeArgId.key)
        }

    private val isLoadingStateFlow: MutableStateFlow<IsLoadingState> =
        MutableStateFlow(IsLoadingState.NotLoading)

    private val eventFlow: MutableStateFlow<SharingSummaryEvent> =
        MutableStateFlow(SharingSummaryEvent.Unknown)

    val state: StateFlow<SharingSummaryUIState> = combine(
        flowOf(email),
        flowOf(sharingType),
        getVaultWithItemCountById(shareId = shareId).asLoadingResult(),
        isLoadingStateFlow,
        eventFlow
    ) { email, sharingType, vaultResult, isLoadingState, event ->
        val vaultWithItemCount = when (vaultResult) {
            is LoadingResult.Success -> vaultResult.data
            is LoadingResult.Error -> {
                snackbarDispatcher(VaultNotFound)
                null
            }

            is LoadingResult.Loading -> null
        }
        val isLoading = vaultResult is LoadingResult.Loading ||
            isLoadingState is IsLoadingState.Loading
        SharingSummaryUIState(
            email = email,
            vaultWithItemCount = vaultWithItemCount,
            sharingType = sharingType,
            isLoading = isLoading,
            event = event
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SharingSummaryUIState()
    )

    fun onSubmit(email: String, shareId: ShareId?, sharingType: SharingType) =
        viewModelScope.launch {
            if (shareId != null) {
                isLoadingStateFlow.update { IsLoadingState.Loading }
                inviteToVault(
                    targetEmail = email,
                    shareId = shareId,
                    shareRole = sharingType.toShareRole(),
                    userMode = userMode.toUserMode()
                ).onSuccess {
                    isLoadingStateFlow.update { IsLoadingState.NotLoading }
                    snackbarDispatcher(InviteSentSuccess)
                    PassLogger.i(TAG, "Invite sent successfully")
                    eventFlow.update { SharingSummaryEvent.Shared }
                }.onFailure {
                    isLoadingStateFlow.update { IsLoadingState.NotLoading }
                    snackbarDispatcher(InviteSentError)
                    PassLogger.w(TAG, "Error sending invite")
                    PassLogger.w(TAG, it)
                }
            } else {
                snackbarDispatcher(VaultNotFound)
            }
        }

    companion object {
        private const val TAG = "SharingSummaryViewModel"
    }
}

private fun SharingWithUserModeType.toUserMode(): InviteToVault.UserMode = when (this) {
    SharingWithUserModeType.ExistingUser -> InviteToVault.UserMode.ExistingUser
    SharingWithUserModeType.NewUser -> InviteToVault.UserMode.NewUser
}

fun SharingType.toShareRole(): ShareRole = when (this) {
    SharingType.Read -> ShareRole.Read
    SharingType.Write -> ShareRole.Write
    SharingType.Admin -> ShareRole.Admin
}
