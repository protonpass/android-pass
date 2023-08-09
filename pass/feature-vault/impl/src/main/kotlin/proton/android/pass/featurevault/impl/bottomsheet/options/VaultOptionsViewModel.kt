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

package proton.android.pass.featurevault.impl.bottomsheet.options

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.data.api.usecases.GetVaultById
import proton.android.pass.data.api.usecases.capabilities.CanManageVaultAccess
import proton.android.pass.data.api.usecases.capabilities.CanMigrateVault
import proton.android.pass.data.api.usecases.capabilities.CanShareVault
import proton.android.pass.featurevault.impl.VaultSnackbarMessage.CannotGetVaultListError
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.pass.domain.ShareId
import javax.inject.Inject

@HiltViewModel
class VaultOptionsViewModel @Inject constructor(
    snackbarDispatcher: SnackbarDispatcher,
    getVaultById: GetVaultById,
    canShareVault: CanShareVault,
    canMigrateVault: CanMigrateVault,
    canManageVaultAccess: CanManageVaultAccess,
    savedStateHandle: SavedStateHandleProvider
) : ViewModel() {

    private val navShareId: ShareId =
        ShareId(savedStateHandle.get().require(CommonNavArgId.ShareId.key))

    private val canShare: Flow<Boolean> = flow { emit(canShareVault(navShareId)) }
        .distinctUntilChanged()

    val state: StateFlow<VaultOptionsUiState> = combine(
        getVaultById(shareId = navShareId).asLoadingResult(),
        canShare
    ) { vaultResult, canShare ->
        val selectedVault = when (vaultResult) {
            is LoadingResult.Error -> return@combine run {
                snackbarDispatcher(CannotGetVaultListError)
                PassLogger.w(TAG, vaultResult.exception, "Cannot get vault")
                VaultOptionsUiState.Error
            }

            LoadingResult.Loading -> return@combine VaultOptionsUiState.Loading
            is LoadingResult.Success -> vaultResult.data
        }

        val canEdit = selectedVault.isOwned
        val canMigrate = canMigrateVault(navShareId)
        val canDelete = !selectedVault.isPrimary && selectedVault.isOwned
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

    companion object {
        private const val TAG = "VaultOptionsViewModel"
    }
}
