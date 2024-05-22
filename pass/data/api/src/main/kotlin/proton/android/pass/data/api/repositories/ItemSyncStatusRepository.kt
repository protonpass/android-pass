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

package proton.android.pass.data.api.repositories

import kotlinx.coroutines.flow.Flow
import proton.android.pass.domain.ShareId

sealed interface ItemSyncStatus {

    data object SyncNotStarted : ItemSyncStatus

    data object SyncStarted : ItemSyncStatus

    data class Syncing(val shareId: ShareId, val current: Int, val total: Int) : ItemSyncStatus

    data object SyncSuccess : ItemSyncStatus

    data object SyncError : ItemSyncStatus

}

data class ItemSyncStatusPayload(
    val current: Int,
    val total: Int
)

sealed interface SyncMode {
    data object ShownToUser : SyncMode
    data object Background : SyncMode
}

interface ItemSyncStatusRepository {
    suspend fun emit(status: ItemSyncStatus)
    fun tryEmit(status: ItemSyncStatus)
    suspend fun setMode(mode: SyncMode)
    fun trySetMode(mode: SyncMode)
    suspend fun clear()
    fun observeMode(): Flow<SyncMode>
    fun observeSyncStatus(): Flow<ItemSyncStatus>
    fun observeAccSyncStatus(): Flow<Map<ShareId, ItemSyncStatusPayload>>
}
