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

package proton.android.pass.features.migrate.confirmvault

import androidx.compose.runtime.Stable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.toOption
import proton.android.pass.commonuimodels.api.FolderUiModel
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.api.repositories.BulkMoveToVaultSelection
import proton.android.pass.data.api.repositories.ParentContainer
import proton.android.pass.domain.FolderId
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.VaultWithItemCount

internal sealed interface ConfirmMigrateEvent {

    data object Close : ConfirmMigrateEvent

    data class ItemMigrated(val shareId: ShareId, val itemId: ItemId) : ConfirmMigrateEvent

    data object AllItemsMigrated : ConfirmMigrateEvent

    data object FolderMoved : ConfirmMigrateEvent

}

@Stable
internal sealed interface MigrateMode {

    @Stable
    @JvmInline
    value class MigrateSelectedItems(val number: Int) : MigrateMode

    @Stable
    data object MigrateAll : MigrateMode

    @Stable
    data object MoveFolder : MigrateMode

    @Stable
    data object MoveAllItemsInFolder : MigrateMode

}

@Stable
internal data class MigrateConfirmVaultUiState(
    val isLoading: IsLoadingState,
    val isLoadingVaults: Boolean,
    val event: Option<ConfirmMigrateEvent>,
    val vaultList: ImmutableList<MigrateVaultState>,
    val folderIdToExpand: Option<FolderId>,
    val disabledFolderId: Option<FolderId>,
    val disabledFolderItemCount: Int,
    val disabledDescendantFolderIds: Set<FolderId>,
    val movingFolderId: Option<FolderId>,
    val selectedShareId: Option<ShareId>,
    val selectedFolderId: Option<FolderId>,
    val mode: MigrateMode,
    val hasAssociatedSecureLinks: Boolean,
    val canDisplayWarningVaultSharedDialog: Boolean,
    val isSameVaultMove: Boolean,
    val showDissolveFolderDialog: Boolean,
    val hasItemsWithHighRevisionCount: Boolean,
    val sourceHasChildFolders: Boolean,
    val sourceName: String
) {
    val showHistoryWarning: Boolean
        get() = selectedShareId is Some && !isSameVaultMove && hasItemsWithHighRevisionCount

    val showSecureLinkWarning: Boolean
        get() = selectedShareId is Some && hasAssociatedSecureLinks

    internal companion object {
        internal fun initial(mode: MigrateMode) = MigrateConfirmVaultUiState(
            isLoading = IsLoadingState.NotLoading,
            isLoadingVaults = true,
            event = None,
            vaultList = persistentListOf(),
            folderIdToExpand = None,
            disabledFolderId = None,
            disabledFolderItemCount = 0,
            disabledDescendantFolderIds = emptySet(),
            movingFolderId = None,
            selectedShareId = None,
            selectedFolderId = None,
            mode = mode,
            hasAssociatedSecureLinks = false,
            canDisplayWarningVaultSharedDialog = false,
            isSameVaultMove = false,
            showDissolveFolderDialog = false,
            hasItemsWithHighRevisionCount = false,
            sourceHasChildFolders = false,
            sourceName = ""
        )
    }
}

// Vault-list shared types (moved from selectvault package)

@Stable
sealed interface VaultStatus {
    @Stable
    data object Enabled : VaultStatus

    @JvmInline
    @Stable
    value class Disabled(val reason: DisabledReason) : VaultStatus

    @Stable
    sealed interface DisabledReason {
        @Stable data object NoPermission : DisabledReason

        @Stable data object SameVault : DisabledReason
    }
}

data class MigrateVaultState(
    val vaultWithItemCount: VaultWithItemCount,
    val status: VaultStatus,
    val folderTree: PersistentList<FolderUiModel> = persistentListOf()
) {
    val hasFolders: Boolean get() = folderTree.isNotEmpty()
}

internal data class SelectedItemsAnalysis(
    val sourceShareId: ShareId?,
    val disableSourceVault: Boolean,
    val disabledFolderId: Option<FolderId>,
    val disabledFolderItemCount: Int
) {
    companion object {
        val Empty = SelectedItemsAnalysis(
            sourceShareId = null,
            disableSourceVault = false,
            disabledFolderId = None,
            disabledFolderItemCount = 0
        )
    }
}

internal fun analyzeSelectedItems(selection: BulkMoveToVaultSelection): SelectedItemsAnalysis {
    if (selection.size != 1) return SelectedItemsAnalysis.Empty
    val sourceShareId = selection.keys.firstOrNull() ?: return SelectedItemsAnalysis.Empty
    val containers = selection[sourceShareId].orEmpty().filterValues { it.isNotEmpty() }
    val hasRootItems = containers.keys.any { it is ParentContainer.Share }
    val folderIds = containers.keys.mapNotNull { it as? ParentContainer.Folder }.map { it.folderId }.toSet()
    val disableSourceVault = hasRootItems && folderIds.isEmpty()
    val disabledFolderId = when {
        !hasRootItems && folderIds.size == 1 -> folderIds.first().toOption()
        else -> None
    }
    val disabledFolderItemCount = if (disabledFolderId is Some) {
        containers.entries
            .firstOrNull { (c, _) -> c is ParentContainer.Folder && c.folderId == disabledFolderId.value }
            ?.value
            ?.size
            ?: 0
    } else 0
    return SelectedItemsAnalysis(
        sourceShareId = sourceShareId,
        disableSourceVault = disableSourceVault,
        disabledFolderId = disabledFolderId,
        disabledFolderItemCount = disabledFolderItemCount
    )
}
