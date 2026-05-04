/*
 * Copyright (c) 2026 Proton AG
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

import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.safeRunCatching
import proton.android.pass.common.api.toOption
import proton.android.pass.data.api.repositories.BulkMoveToVaultEvent
import proton.android.pass.data.api.repositories.BulkMoveToVaultRepository
import proton.android.pass.data.api.repositories.MigrateItemsResult
import proton.android.pass.data.api.usecases.MigrateItems
import proton.android.pass.data.api.usecases.MigrateVault
import proton.android.pass.data.api.usecases.folders.DissolveFolder
import proton.android.pass.data.api.usecases.folders.MoveAllItemsInFolder
import proton.android.pass.data.api.usecases.folders.MoveFolder
import proton.android.pass.data.api.usecases.folders.MoveItemsInsideShare
import proton.android.pass.domain.FolderId
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.features.migrate.MigrateSnackbarMessage
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarDispatcher
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MigrateConfirmVaultMigrator @Inject constructor(
    private val migrateItems: MigrateItems,
    private val migrateVault: MigrateVault,
    private val moveFolder: MoveFolder,
    private val dissolveFolder: DissolveFolder,
    private val moveAllItemsInFolder: MoveAllItemsInFolder,
    private val moveItemsInsideShare: MoveItemsInsideShare,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val bulkMoveToVaultRepository: BulkMoveToVaultRepository
) {

    internal suspend fun performFolderDissolve(shareId: ShareId, folderId: FolderId): Option<ConfirmMigrateEvent> =
        safeRunCatching { dissolveFolder(shareId = shareId, folderId = folderId) }
            .onSuccess { snackbarDispatcher(MigrateSnackbarMessage.FolderMoved) }
            .onFailure {
                PassLogger.w(TAG, "Error dissolving folder")
                PassLogger.w(TAG, it)
                snackbarDispatcher(MigrateSnackbarMessage.FolderNotMoved)
            }
            .map { ConfirmMigrateEvent.FolderMoved }
            .getOrNull()
            .toOption()

    internal suspend fun performFolderMove(
        shareId: ShareId,
        folderId: FolderId,
        newParentFolderId: FolderId?
    ): Option<ConfirmMigrateEvent> =
        safeRunCatching { moveFolder(shareId = shareId, folderId = folderId, newParentFolderId = newParentFolderId) }
            .onSuccess { snackbarDispatcher(MigrateSnackbarMessage.FolderMoved) }
            .onFailure {
                PassLogger.w(TAG, "Error moving folder")
                PassLogger.w(TAG, it)
                snackbarDispatcher(MigrateSnackbarMessage.FolderNotMoved)
            }
            .map { ConfirmMigrateEvent.FolderMoved }
            .getOrNull()
            .toOption()

    internal suspend fun performAllItemsMigration(
        sourceShareId: ShareId,
        destShareId: ShareId
    ): Option<ConfirmMigrateEvent> = safeRunCatching { migrateVault(origin = sourceShareId, dest = destShareId) }
        .onSuccess { snackbarDispatcher(MigrateSnackbarMessage.VaultItemsMigrated) }
        .onFailure {
            PassLogger.w(TAG, "Error migrating all items")
            PassLogger.w(TAG, it)
            snackbarDispatcher(MigrateSnackbarMessage.VaultItemsNotMigrated)
        }
        .map { ConfirmMigrateEvent.AllItemsMigrated }
        .getOrNull()
        .toOption()

    internal suspend fun performMoveAllItemsInFolder(
        sourceShareId: ShareId,
        sourceFolderId: FolderId,
        destShareId: ShareId,
        destFolderId: FolderId?
    ): Option<ConfirmMigrateEvent> = safeRunCatching {
        moveAllItemsInFolder(
            shareId = sourceShareId,
            folderId = sourceFolderId,
            destShareId = destShareId,
            destFolderId = destFolderId
        )
    }
        .onSuccess { snackbarDispatcher(MigrateSnackbarMessage.FolderItemsMoved) }
        .onFailure {
            PassLogger.w(TAG, "Error moving all items in folder")
            PassLogger.w(TAG, it)
            snackbarDispatcher(MigrateSnackbarMessage.FolderItemsNotMoved)
        }
        .map { ConfirmMigrateEvent.FolderMoved }
        .getOrNull()
        .toOption()

    @Suppress("LongMethod")
    internal suspend fun performItemMigration(
        destShareId: ShareId,
        destFolderId: Option<FolderId>,
        itemsToMigrate: Map<ShareId, List<ItemId>>
    ): Option<ConfirmMigrateEvent> {
        val isSameVaultFolderMove = itemsToMigrate.keys.singleOrNull() == destShareId

        return if (isSameVaultFolderMove) {
            safeRunCatching {
                moveItemsInsideShare(
                    shareId = destShareId,
                    folderId = destFolderId.value(),
                    itemIds = itemsToMigrate.values.flatten()
                )
            }.onSuccess {
                val firstEntry = itemsToMigrate.entries.first()
                bulkMoveToVaultRepository.emitEvent(BulkMoveToVaultEvent.Completed)
                bulkMoveToVaultRepository.delete()
                snackbarDispatcher(MigrateSnackbarMessage.ItemMigrated)
                return Some(
                    ConfirmMigrateEvent.ItemMigrated(
                        shareId = firstEntry.key,
                        itemId = firstEntry.value.first()
                    )
                )
            }.onFailure {
                PassLogger.w(TAG, "Error moving items to folder")
                PassLogger.w(TAG, it)
                snackbarDispatcher(MigrateSnackbarMessage.ItemNotMigrated)
            }
            None
        } else {
            performCrossVaultItemMigration(destShareId, destFolderId, itemsToMigrate)
        }
    }

    private suspend fun performCrossVaultItemMigration(
        destShareId: ShareId,
        destFolderId: Option<FolderId>,
        itemsToMigrate: Map<ShareId, List<ItemId>>
    ): Option<ConfirmMigrateEvent> = safeRunCatching {
        migrateItems(
            items = itemsToMigrate,
            destinationShare = destShareId,
            destinationFolderId = destFolderId
        )
    }.map { migrateResult ->
        when (migrateResult) {
            is MigrateItemsResult.AllMigrated -> {
                val migratedItem = migrateResult.items.firstOrNull() ?: run {
                    PassLogger.w(TAG, "No items were migrated")
                    return None
                }
                bulkMoveToVaultRepository.emitEvent(BulkMoveToVaultEvent.Completed)
                bulkMoveToVaultRepository.delete()
                snackbarDispatcher(MigrateSnackbarMessage.ItemMigrated)
                Some(ConfirmMigrateEvent.ItemMigrated(shareId = migratedItem.shareId, itemId = migratedItem.id))
            }
            is MigrateItemsResult.SomeMigrated -> {
                val migratedItem = migrateResult.migratedItems.firstOrNull() ?: run {
                    PassLogger.w(TAG, "No items were migrated")
                    return None
                }
                snackbarDispatcher(MigrateSnackbarMessage.SomeItemsNotMigrated)
                Some(ConfirmMigrateEvent.ItemMigrated(shareId = migratedItem.shareId, itemId = migratedItem.id))
            }
            is MigrateItemsResult.NoneMigrated -> {
                PassLogger.w(TAG, "Error migrating items")
                PassLogger.w(TAG, migrateResult.exception)
                snackbarDispatcher(MigrateSnackbarMessage.ItemNotMigrated)
                None
            }
        }
    }.getOrElse {
        PassLogger.w(TAG, "Error migrating item")
        PassLogger.w(TAG, it)
        snackbarDispatcher(MigrateSnackbarMessage.ItemNotMigrated)
        None
    }

    private companion object {
        private const val TAG = "MigrateConfirmVaultMigrator"
    }
}
