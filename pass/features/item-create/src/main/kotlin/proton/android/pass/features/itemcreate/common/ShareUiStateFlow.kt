/*
 * Copyright (c) 2023-2026 Proton AG
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

package proton.android.pass.features.itemcreate.common

import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import me.proton.core.accountmanager.domain.AccountManager
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.commonuimodels.api.FolderUiModel
import proton.android.pass.data.api.usecases.folders.ObserveFolder
import proton.android.pass.domain.FolderId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.VaultWithItemCount
import proton.android.pass.domain.canCreate
import proton.android.pass.domain.toPermissions
import proton.android.pass.log.api.PassLogger

fun getFolderNameFlow(
    accountManager: AccountManager,
    observeFolder: ObserveFolder,
    selectedShareIdState: Flow<Option<ShareId>>,
    selectedFolderIdFlow: Flow<Option<FolderId>>,
    navShareIdState: Flow<Option<ShareId>>
): Flow<String?> = combine(
    accountManager.getPrimaryUserId().distinctUntilChanged(),
    selectedShareIdState,
    navShareIdState,
    selectedFolderIdFlow
) { userId, shareIdOption, navShareIdOption, folderIdOption ->
    val shareId = shareIdOption.value() ?: navShareIdOption.value()
    Triple(userId, shareId, folderIdOption.value())
}.flatMapLatest { (userId, shareId, folderId) ->
    if (userId == null || shareId == null || folderId == null) return@flatMapLatest flowOf(null)
    observeFolder(userId, shareId, folderId)
        .map { folder -> folder?.name }
        .distinctUntilChanged()
}

private data class VaultArgs(
    val navShareId: Option<ShareId>,
    val selectedShareId: Option<ShareId>,
    val allSharesResult: LoadingResult<List<VaultWithItemCount>>,
    val defaultVaultResult: LoadingResult<Option<VaultWithItemCount>>
)

@Suppress("LongParameterList", "MagicNumber")
fun getShareUiStateFlow(
    navShareIdState: Flow<Option<ShareId>>,
    selectedShareIdState: Flow<Option<ShareId>>,
    selectedFolderNameFlow: Flow<String?>,
    selectedFolderIdFlow: Flow<Option<FolderId>>,
    observeAllVaultsFlow: Flow<LoadingResult<List<VaultWithItemCount>>>,
    observeDefaultVaultFlow: Flow<LoadingResult<Option<VaultWithItemCount>>>,
    viewModelScope: CoroutineScope,
    tag: String
): StateFlow<ShareUiState> = combine(
    combine(
        navShareIdState,
        selectedShareIdState,
        observeAllVaultsFlow,
        observeDefaultVaultFlow,
        ::VaultArgs
    ),
    selectedFolderNameFlow,
    selectedFolderIdFlow
) { vaultArgs, selectedFolderName, selectedFolderIdOption ->
    val allShares = when (val result = vaultArgs.allSharesResult) {
        is LoadingResult.Error -> return@combine ShareUiState.Error(ShareError.SharesNotAvailable)
        LoadingResult.Loading -> return@combine ShareUiState.Loading
        is LoadingResult.Success -> result.data
    }
    val defaultVault = when (val result = vaultArgs.defaultVaultResult) {
        is LoadingResult.Error -> return@combine ShareUiState.Error(ShareError.SharesNotAvailable)
        LoadingResult.Loading -> return@combine ShareUiState.Loading
        is LoadingResult.Success -> result.data
    }
    shareUiState(
        tag = tag,
        allShares = allShares,
        navShareId = vaultArgs.navShareId,
        selectedShareId = vaultArgs.selectedShareId,
        defaultVault = defaultVault,
        selectedFolderName = selectedFolderName,
        selectedFolderId = selectedFolderIdOption.value()
    )
}.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = ShareUiState.NotInitialised
)

@Suppress("LongParameterList")
private fun shareUiState(
    tag: String,
    allShares: List<VaultWithItemCount>,
    selectedShareId: Option<ShareId>,
    navShareId: Option<ShareId>,
    defaultVault: Option<VaultWithItemCount>,
    selectedFolderName: String?,
    selectedFolderId: FolderId?
): ShareUiState {
    val writeableVaults = allShares.filter { it.vault.role.toPermissions().canCreate() }
    if (writeableVaults.isEmpty()) {
        PassLogger.w(tag, "No writeable shares (numShares: ${allShares.size})")
        return ShareUiState.Error(ShareError.EmptyShareList)
    }

    val selectedVault = if (selectedShareId is Some) {
        // Pick the selected vault if it is writeable
        // otherwise, pick the nav vault if it is writeable
        // otherwise, pick the default vault if it is there
        // otherwise, just the first writeable vault
        writeableVaults.firstOrNull { it.vault.shareId == selectedShareId.value() }
            ?: writeableVaults.firstOrNull { it.vault.shareId == navShareId.value() }
            ?: defaultVault.value()
            ?: writeableVaults.first()
    } else {
        // Pick the nav vault if it is writeable
        // otherwise, pick the default vault if it is there
        // otherwise, just the first writeable vault
        writeableVaults.firstOrNull { it.vault.shareId == navShareId.value() }
            ?: defaultVault.value()
            ?: writeableVaults.first()
    }

    return ShareUiState.Success(
        vaultList = allShares,
        currentVault = selectedVault,
        selectedFolder = selectedFolderId?.let {
            FolderUiModel(
                id = it,
                name = selectedFolderName.orEmpty(),
                folders = persistentListOf()
            )
        }
    )
}
