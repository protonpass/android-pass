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

package proton.android.pass.featureitemcreate.impl.common

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.Option
import proton.android.pass.log.api.PassLogger
import proton.pass.domain.ShareId
import proton.pass.domain.VaultWithItemCount
import proton.pass.domain.canCreate
import proton.pass.domain.toPermissions

@Suppress("ComplexMethod", "CyclomaticComplexMethod", "LongParameterList", "MagicNumber")
fun getShareUiStateFlow(
    navShareIdState: Flow<Option<ShareId>>,
    selectedShareIdState: Flow<Option<ShareId>>,
    observeAllVaultsFlow: Flow<LoadingResult<List<VaultWithItemCount>>>,
    canPerformPaidAction: Flow<LoadingResult<Boolean>>,
    viewModelScope: CoroutineScope,
    tag: String
): StateFlow<ShareUiState> = combine(
    navShareIdState,
    selectedShareIdState,
    observeAllVaultsFlow,
    canPerformPaidAction
) { navShareId, selectedShareId, allSharesResult, canDoPaidAction ->
    val allShares = when (allSharesResult) {
        is LoadingResult.Error -> return@combine ShareUiState.Error(ShareError.SharesNotAvailable)
        LoadingResult.Loading -> return@combine ShareUiState.Loading
        is LoadingResult.Success -> allSharesResult.data
    }
    val canSwitchVaults = when (canDoPaidAction) {
        is LoadingResult.Error -> return@combine ShareUiState.Error(ShareError.UpgradeInfoNotAvailable)
        LoadingResult.Loading -> return@combine ShareUiState.Loading
        is LoadingResult.Success -> canDoPaidAction.data
    }

    if (allShares.isEmpty()) {
        return@combine ShareUiState.Error(ShareError.EmptyShareList)
    }
    val selectedVault = if (!canSwitchVaults) {
        val primaryVault = allShares.firstOrNull { it.vault.isPrimary }
        if (primaryVault == null) {
            PassLogger.w(tag, "No primary vault found")
            return@combine ShareUiState.Error(ShareError.NoPrimaryVault)
        }
        primaryVault
    } else {
        val selectedOrNavVault = allShares
            .firstOrNull { it.vault.shareId == selectedShareId.value() }
            ?: allShares.firstOrNull { it.vault.shareId == navShareId.value() }

        val primaryVault = allShares.firstOrNull { it.vault.isPrimary }
            ?: allShares.firstOrNull()
            ?: return@combine ShareUiState.Error(ShareError.EmptyShareList)

        if (selectedOrNavVault != null) {
            val selectedVaultPermissions = selectedOrNavVault.vault.role.toPermissions()
            if (selectedVaultPermissions.canCreate()) {
                selectedOrNavVault
            } else {
                PassLogger.i(
                    tag,
                    "Changing selected vault to primary as user cannot create items in selected vault"
                )
                primaryVault
            }
        } else {
            primaryVault
        }
    }
    ShareUiState.Success(
        vaultList = allShares,
        currentVault = selectedVault
    )
}.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5_000),
    initialValue = ShareUiState.NotInitialised
)

