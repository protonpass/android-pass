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

package proton.android.pass.features.vault.bottomsheet.options

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.data.api.usecases.ObserveVaults
import proton.android.pass.data.api.usecases.capabilities.CanManageVaultAccess
import proton.android.pass.data.api.usecases.capabilities.CanMigrateVault
import proton.android.pass.data.api.usecases.capabilities.CanShareShare
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.Vault
import proton.android.pass.features.vault.VaultSnackbarMessage.CannotFindVaultError
import proton.android.pass.features.vault.VaultSnackbarMessage.CannotGetVaultListError
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import javax.inject.Inject

@HiltViewModel
class VaultOptionsViewModel @Inject constructor(
    snackbarDispatcher: SnackbarDispatcher,
    observeVaults: ObserveVaults,
    canShareShare: CanShareShare,
    canMigrateVault: CanMigrateVault,
    canManageVaultAccess: CanManageVaultAccess,
    savedStateHandle: SavedStateHandleProvider
) : ViewModel() {

    private val navShareId: ShareId =
        ShareId(savedStateHandle.get().require(CommonNavArgId.ShareId.key))

    private val canShare: Flow<Boolean> = flow { emit(canShareShare(navShareId)) }
        .map { it.value }
        .distinctUntilChanged()

    val state: StateFlow<VaultOptionsUiState> = combine(
        observeVaults().asLoadingResult(),
        canShare
    ) { vaultResult, canShare ->
        val (allVaults, selectedVault) = when (vaultResult) {
            is LoadingResult.Error -> {
                snackbarDispatcher(CannotGetVaultListError)
                PassLogger.w(TAG, "Cannot get vault")
                PassLogger.w(TAG, vaultResult.exception)
                return@combine VaultOptionsUiState.Error
            }

            LoadingResult.Loading -> return@combine VaultOptionsUiState.Loading
            is LoadingResult.Success -> {
                val selectedVault = vaultResult.data.find { it.shareId == navShareId }
                if (selectedVault == null) {
                    snackbarDispatcher(CannotFindVaultError)
                    PassLogger.w(TAG, "Cannot find vault with shareId $navShareId")
                    return@combine VaultOptionsUiState.Error
                }
                vaultResult.data to selectedVault
            }
        }

        val canDelete = canDeleteVault(allVaults, selectedVault)

        val canEdit = selectedVault.isOwned
        val canMigrate = canMigrateVault(navShareId)
        val canLeave = !selectedVault.isOwned

        val vaultAccessData = canManageVaultAccess(selectedVault)

        // Only show share if it is not already shared
        val showShare = canShare && !selectedVault.shared

        // Only show manageVault and viewMembers if vault has not already been shared
        val showManageAccess = selectedVault.shared && vaultAccessData.canManageAccess
        val showViewMembers = selectedVault.shared && vaultAccessData.canViewMembers
        VaultOptionsUiState.Success(
            shareId = navShareId,
            showEdit = canEdit,
            showMigrate = canMigrate,
            showDelete = canDelete,
            showShare = showShare,
            showLeave = canLeave,
            showManageAccess = showManageAccess,
            showViewMembers = showViewMembers
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = VaultOptionsUiState.Uninitialised
    )

    private fun canDeleteVault(allVaults: List<Vault>, selectedVault: Vault): Boolean {
        val ownedVaultsCount = allVaults.count { it.isOwned }
        return when {
            !selectedVault.isOwned -> false // Cannot remove vault if is not owned
            ownedVaultsCount == 1 -> false // Cannot remove vault if is the last owned one
            else -> true
        }
    }

    companion object {
        private const val TAG = "VaultOptionsViewModel"
    }
}
