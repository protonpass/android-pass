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
import proton.android.pass.common.api.Some
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
    removePrimaryVaultFlow: Flow<Boolean>,
    viewModelScope: CoroutineScope,
    tag: String
): StateFlow<ShareUiState> = combine(
    navShareIdState,
    selectedShareIdState,
    observeAllVaultsFlow,
    canPerformPaidAction,
    removePrimaryVaultFlow
) { navShareId, selectedShareId, allSharesResult, canDoPaidAction, removePrimaryVault ->
    val allShares = when (allSharesResult) {
        is LoadingResult.Error -> return@combine ShareUiState.Error(ShareError.SharesNotAvailable)
        LoadingResult.Loading -> return@combine ShareUiState.Loading
        is LoadingResult.Success -> allSharesResult.data
    }
    shareUiStateWithRemovePrimaryVault(
        tag = tag,
        allShares = allShares,
        navShareId = navShareId,
        selectedShareId = selectedShareId
    )
}.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5_000),
    initialValue = ShareUiState.NotInitialised
)

private fun shareUiStateWithRemovePrimaryVault(
    tag: String,
    allShares: List<VaultWithItemCount>,
    selectedShareId: Option<ShareId>,
    navShareId: Option<ShareId>
): ShareUiState {
    val writeableVaults = allShares.filter { it.vault.role.toPermissions().canCreate() }
    if (writeableVaults.isEmpty()) {
        PassLogger.w(tag, "No writeable shares (numShares: ${allShares.size})")
        return ShareUiState.Error(ShareError.EmptyShareList)
    }

    val selectedVault = if (selectedShareId is Some) {
        // Pick the selected vault if it is writeable
        // otherwise, pick the nav vault if it is writeable
        // otherwise, just the first writeable vault
        writeableVaults.firstOrNull { it.vault.shareId == selectedShareId.value() }
            ?: writeableVaults.firstOrNull { it.vault.shareId == navShareId.value() }
            ?: writeableVaults.first()
    } else {
        // Pick the nav vault if it is writeable
        // otherwise, just the first writeable vault
        writeableVaults.firstOrNull { it.vault.shareId == navShareId.value() }
            ?: writeableVaults.first()
    }

    return ShareUiState.Success(
        vaultList = allShares,
        currentVault = selectedVault
    )
}

