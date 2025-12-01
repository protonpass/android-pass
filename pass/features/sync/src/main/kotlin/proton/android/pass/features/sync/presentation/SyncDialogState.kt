/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.features.sync.presentation

import androidx.compose.runtime.Stable
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toPersistentMap
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.toOption
import proton.android.pass.data.api.repositories.ItemSyncStatus
import proton.android.pass.data.api.repositories.ItemSyncStatusPayload
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.Vault

@Stable
internal data class SyncDialogState(
    private val itemSyncStatus: ItemSyncStatus,
    private val downloadedItemsMap: Map<ShareId, ItemSyncStatusPayload>,
    private val insertedItems: Option<ItemSyncStatusPayload>,
    private val vaultsLoadingResult: LoadingResult<List<Vault>>
) {

    internal val hasSyncFailed: Boolean = itemSyncStatus is ItemSyncStatus.SyncError

    internal val hasSyncSucceeded: Boolean = itemSyncStatus is ItemSyncStatus.SyncSuccess

    internal val hasSyncFinished: Boolean = hasSyncFailed || hasSyncSucceeded

    internal val isInserting: Boolean =
        insertedItems.map { it.current > 0 && it.current != it.total }.value() ?: false

    internal val syncItemsMap: ImmutableMap<ShareId, SyncDialogItem> = when (vaultsLoadingResult) {
        is LoadingResult.Error,
        LoadingResult.Loading -> persistentMapOf()

        is LoadingResult.Success ->
            vaultsLoadingResult.data
                .associateBy { vault -> vault.shareId }
                .mapValues { (shareId, vault) ->
                    SyncDialogItem(
                        vault = vault,
                        downloadedItemsCountOption = downloadedItemsMap[shareId]?.current.toOption(),
                        totalDownloadedItemsCountOption = downloadedItemsMap[shareId]?.total.toOption()
                    )
                }
                .toPersistentMap()
    }

    internal companion object {

        internal val Initial: SyncDialogState = SyncDialogState(
            itemSyncStatus = ItemSyncStatus.SyncNotStarted,
            downloadedItemsMap = emptyMap(),
            insertedItems = None,
            vaultsLoadingResult = LoadingResult.Loading
        )

    }

}
