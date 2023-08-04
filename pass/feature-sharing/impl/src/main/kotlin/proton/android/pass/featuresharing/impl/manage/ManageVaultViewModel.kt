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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
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
import proton.android.pass.data.api.usecases.GetVaultMembers
import proton.android.pass.data.api.usecases.GetVaultWithItemCountById
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.android.pass.data.api.usecases.VaultMember
import proton.android.pass.data.api.usecases.capabilities.CanShareVault
import proton.android.pass.featuresharing.impl.SharingSnackbarMessage
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.pass.domain.ShareId
import proton.pass.domain.SharePermissionFlag
import proton.pass.domain.VaultWithItemCount
import proton.pass.domain.hasFlag
import proton.pass.domain.toPermissions
import javax.inject.Inject

@HiltViewModel
class ManageVaultViewModel @Inject constructor(
    getVaultMembers: GetVaultMembers,
    getVaultById: GetVaultWithItemCountById,
    savedStateHandleProvider: SavedStateHandleProvider,
    observeCurrentUser: ObserveCurrentUser,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val canShareVault: CanShareVault
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

    private val showShareButtonFlow: Flow<Boolean> = vaultFlow
        .map { canShareVault(it.vault) }
        .distinctUntilChanged()

    private val canEditFlow: Flow<Boolean> = vaultFlow
        .map { it.vault.role.toPermissions().hasFlag(SharePermissionFlag.Admin) }
        .distinctUntilChanged()

    val state: StateFlow<ManageVaultUiState> = combineN(
        membersFlow,
        vaultFlow,
        showShareButtonFlow,
        canEditFlow,
        eventFlow,
        observeCurrentUser()
    ) { vaultMembers, vault, showShareButton, canEdit, event, currentUser ->
        val content = when (vaultMembers) {
            is LoadingResult.Error -> ManageVaultUiContent.Loading
            LoadingResult.Loading -> ManageVaultUiContent.Loading
            is LoadingResult.Success -> ManageVaultUiContent.Content(
                vaultMembers = vaultMembers.data,
                canEdit = canEdit
            )
        }

        ManageVaultUiState(
            vault = vault,
            content = content,
            event = event,
            showShareButton = showShareButton
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

}
