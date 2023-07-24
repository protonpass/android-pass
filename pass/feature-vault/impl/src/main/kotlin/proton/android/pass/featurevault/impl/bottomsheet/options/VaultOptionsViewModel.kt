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

import androidx.lifecycle.SavedStateHandle
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
import proton.android.pass.data.api.usecases.CanPerformPaidAction
import proton.android.pass.data.api.usecases.ObserveVaults
import proton.android.pass.featurevault.impl.VaultSnackbarMessage.CannotGetVaultListError
import proton.android.pass.featurevault.impl.VaultSnackbarMessage.CannotGetVaultUpgradeInfoError
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.useraccess.api.UserAccess
import proton.pass.domain.ShareId
import javax.inject.Inject

@HiltViewModel
class VaultOptionsViewModel @Inject constructor(
    snackbarDispatcher: SnackbarDispatcher,
    canPerformPaidAction: CanPerformPaidAction,
    observeVaults: ObserveVaults,
    userAccess: UserAccess,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val navShareId: ShareId =
        ShareId(requireNotNull(savedStateHandle.get<String>(CommonNavArgId.ShareId.key)))

    private val canShare: Flow<Boolean> = flow { emit(userAccess.canShare(navShareId)) }
        .distinctUntilChanged()

    val state: StateFlow<VaultOptionsUiState> = combine(
        observeVaults().asLoadingResult(),
        canPerformPaidAction().asLoadingResult(),
        canShare
    ) { vaultResult, canPerformPaidActionResult, canShare ->
        val vaultList = when (vaultResult) {
            is LoadingResult.Error -> return@combine run {
                snackbarDispatcher(CannotGetVaultListError)
                PassLogger.w(TAG, vaultResult.exception, "Cannot get vault list")
                VaultOptionsUiState.Error
            }

            LoadingResult.Loading -> return@combine VaultOptionsUiState.Loading
            is LoadingResult.Success -> vaultResult.data
        }
        val canPerformPaidActionValue = when (canPerformPaidActionResult) {
            is LoadingResult.Error -> return@combine run {
                snackbarDispatcher(CannotGetVaultUpgradeInfoError)
                PassLogger.w(
                    TAG,
                    canPerformPaidActionResult.exception,
                    "Cannot get CanPerformPaidAction"
                )
                VaultOptionsUiState.Error
            }

            LoadingResult.Loading -> return@combine VaultOptionsUiState.Loading
            is LoadingResult.Success -> canPerformPaidActionResult.data
        }
        val selectedVault = vaultList.firstOrNull { it.shareId == navShareId }
            ?: return@combine VaultOptionsUiState.Error
        val canEdit = canPerformPaidActionValue || selectedVault.isPrimary
        val canMigrate = if (canPerformPaidActionValue) {
            vaultList.size > 1
        } else {
            vaultList.size > 1 && !selectedVault.isPrimary
        }
        val canDelete = !selectedVault.isPrimary
        VaultOptionsUiState.Success(
            shareId = navShareId,
            showEdit = canEdit,
            showMigrate = canMigrate,
            showDelete = canDelete,
            showShare = canShare
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
