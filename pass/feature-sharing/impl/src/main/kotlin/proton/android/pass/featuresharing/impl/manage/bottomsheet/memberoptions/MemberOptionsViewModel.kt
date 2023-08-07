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

package proton.android.pass.featuresharing.impl.manage.bottomsheet.memberoptions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.api.usecases.GetVaultById
import proton.android.pass.data.api.usecases.RemoveMemberFromVault
import proton.android.pass.data.api.usecases.SetVaultMemberPermission
import proton.android.pass.featuresharing.impl.SharingSnackbarMessage
import proton.android.pass.featuresharing.impl.manage.bottomsheet.MemberEmailArg
import proton.android.pass.featuresharing.impl.manage.bottomsheet.MemberShareIdArg
import proton.android.pass.featuresharing.impl.manage.bottomsheet.ShareRoleArg
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.NavParamEncoder
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.pass.domain.ShareId
import proton.pass.domain.SharePermissionFlag
import proton.pass.domain.ShareRole
import proton.pass.domain.Vault
import proton.pass.domain.hasFlag
import proton.pass.domain.toPermissions
import javax.inject.Inject

@HiltViewModel
class MemberOptionsViewModel @Inject constructor(
    private val snackbarDispatcher: SnackbarDispatcher,
    private val removeMemberFromVault: RemoveMemberFromVault,
    private val setVaultMemberPermission: SetVaultMemberPermission,
    savedState: SavedStateHandleProvider,
    getVaultById: GetVaultById
) : ViewModel() {

    private val vaultShareId = ShareId(savedState.get().require(CommonNavArgId.ShareId.key))
    private val memberShareId = ShareId(savedState.get().require(MemberShareIdArg.key))
    private val shareRole = ShareRole.fromValue(savedState.get().require(ShareRoleArg.key))
    private val memberEmail = NavParamEncoder.decode(savedState.get().require(MemberEmailArg.key))

    private val eventFlow: MutableStateFlow<MemberOptionsEvent> =
        MutableStateFlow(MemberOptionsEvent.Unknown)
    private val isLoadingFlow: MutableStateFlow<IsLoadingState> =
        MutableStateFlow(IsLoadingState.NotLoading)
    private val loadingOptionFlow: MutableStateFlow<LoadingOption?> =
        MutableStateFlow(null)

    private val getVaultFlow: Flow<Vault> = getVaultById(shareId = vaultShareId)
        .distinctUntilChanged()

    val state: StateFlow<MemberOptionsUiState> = combine(
        getVaultFlow,
        eventFlow,
        isLoadingFlow,
        loadingOptionFlow
    ) { vault, event, isLoading, loadingOption ->
        val memberPermissions = shareRole.toPermissions()
        val showTransferOwnership =
            vault.isOwned && memberPermissions.hasFlag(SharePermissionFlag.Admin)
        MemberOptionsUiState(
            memberRole = shareRole,
            showTransferOwnership = showTransferOwnership,
            event = event,
            isLoading = isLoading,
            loadingOption = loadingOption
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = MemberOptionsUiState.Initial
        )

    fun setPermissions(permissionLevel: MemberPermissionLevel) = viewModelScope.launch {
        isLoadingFlow.update { IsLoadingState.Loading }
        loadingOptionFlow.update { permissionLevel.toLoadingOption() }
        runCatching {
            setVaultMemberPermission(
                shareId = vaultShareId,
                memberShareId = memberShareId,
                role = permissionLevel.toShareRole()
            )
        }.onSuccess {
            PassLogger.i(TAG, "Member permissions changed")
            eventFlow.update { MemberOptionsEvent.Close(refresh = true) }
            snackbarDispatcher(SharingSnackbarMessage.ChangeMemberPermissionSuccess)
        }.onFailure {
            PassLogger.w(TAG, it, "Error changing member permissions")
            snackbarDispatcher(SharingSnackbarMessage.ChangeMemberPermissionError)
        }
        isLoadingFlow.update { IsLoadingState.NotLoading }
        loadingOptionFlow.update { null }
    }

    fun removeFromVault() = viewModelScope.launch {
        isLoadingFlow.update { IsLoadingState.Loading }
        loadingOptionFlow.update { LoadingOption.RemoveMember }
        runCatching {
            removeMemberFromVault(
                shareId = vaultShareId,
                memberShareId = memberShareId
            )
        }.onSuccess {
            PassLogger.i(TAG, "Member removed from vault")
            eventFlow.update { MemberOptionsEvent.Close(refresh = true) }
            snackbarDispatcher(SharingSnackbarMessage.RemoveMemberSuccess)
        }.onFailure {
            PassLogger.w(TAG, it, "Error removing member")
            snackbarDispatcher(SharingSnackbarMessage.RemoveMemberError)
        }
        isLoadingFlow.update { IsLoadingState.NotLoading }
        loadingOptionFlow.update { null }
    }

    fun transferOwnership() = viewModelScope.launch {
        eventFlow.update {
            MemberOptionsEvent.TransferOwnership(
                shareId = vaultShareId,
                destShareId = memberShareId,
                destEmail = memberEmail
            )
        }
    }

    fun clearEvent() = viewModelScope.launch {
        eventFlow.update { MemberOptionsEvent.Unknown }
    }

    companion object {
        private const val TAG = "MemberOptionsViewModel"
    }
}
