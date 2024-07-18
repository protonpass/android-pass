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

package proton.android.pass.data.impl.repositories

import androidx.lifecycle.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.proton.core.presentation.app.AppLifecycleProvider
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.some
import proton.android.pass.data.api.repositories.ItemSyncStatus
import proton.android.pass.data.api.repositories.ItemSyncStatusPayload
import proton.android.pass.data.api.repositories.ItemSyncStatusRepository
import proton.android.pass.data.api.repositories.SyncMode
import proton.android.pass.data.api.repositories.SyncState
import proton.android.pass.domain.ShareId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ItemSyncStatusRepositoryImpl @Inject constructor(
    private val lifecycleProvider: AppLifecycleProvider
) : ItemSyncStatusRepository {

    private val syncStatus: MutableSharedFlow<ItemSyncStatus> =
        MutableSharedFlow<ItemSyncStatus>(replay = 1, extraBufferCapacity = 3)
            .apply { tryEmit(ItemSyncStatus.SyncNotStarted) }
    private val downloadedItemsState: MutableSharedFlow<Map<ShareId, ItemSyncStatusPayload>> =
        MutableSharedFlow(replay = 1, extraBufferCapacity = 3)
    private val downloadedItemsMutableMap: MutableMap<ShareId, ItemSyncStatusPayload> =
        mutableMapOf()
    private val insertedItemsState: MutableSharedFlow<Option<ItemSyncStatusPayload>> =
        MutableSharedFlow(replay = 1, extraBufferCapacity = 3)
    private val modeFlow: MutableSharedFlow<SyncMode> = MutableSharedFlow<SyncMode>(
        replay = 1, extraBufferCapacity = 1
    ).apply { tryEmit(SyncMode.Background) }

    private val mutex: Mutex = Mutex()

    private suspend fun updateSyncStatus(status: ItemSyncStatus, emit: suspend (ItemSyncStatus) -> Unit) {
        mutex.withLock {
            when (status) {
                is ItemSyncStatus.SyncInserting -> {
                    insertedItemsState.emit(ItemSyncStatusPayload(status.current, status.total).some())
                }
                is ItemSyncStatus.SyncDownloading -> {
                    downloadedItemsMutableMap[status.shareId] =
                        ItemSyncStatusPayload(status.current, status.total)
                    downloadedItemsState.emit(downloadedItemsMutableMap.toMap())
                }

                ItemSyncStatus.SyncNotStarted -> {
                    downloadedItemsMutableMap.clear()
                    downloadedItemsState.emit(emptyMap())
                    insertedItemsState.emit(None)
                }

                ItemSyncStatus.SyncError,
                ItemSyncStatus.SyncStarted,
                ItemSyncStatus.SyncSuccess -> {
                }
            }
        }
        emit(status)
    }

    override suspend fun emit(status: ItemSyncStatus) {
        updateSyncStatus(status) { syncStatus.emit(it) }
    }

    override fun tryEmit(status: ItemSyncStatus) {
        lifecycleProvider.lifecycle.coroutineScope
            .launch { updateSyncStatus(status) { syncStatus.tryEmit(it) } }
    }

    override suspend fun setMode(mode: SyncMode) {
        modeFlow.emit(mode)
    }

    override fun trySetMode(mode: SyncMode) {
        modeFlow.tryEmit(mode)
    }

    override fun observeMode(): Flow<SyncMode> = modeFlow

    override suspend fun clear() {
        downloadedItemsMutableMap.clear()
        downloadedItemsState.emit(emptyMap())
        insertedItemsState.emit(None)
        syncStatus.emit(ItemSyncStatus.SyncNotStarted)
    }

    override fun observeSyncStatus(): Flow<ItemSyncStatus> = syncStatus

    override fun observeDownloadedItemsStatus(): Flow<Map<ShareId, ItemSyncStatusPayload>> =
        downloadedItemsState.onStart { emit(emptyMap()) }

    override fun observeInsertedItemsStatus(): Flow<Option<ItemSyncStatusPayload>> =
        insertedItemsState.onStart { emit(None) }

    override fun observeSyncState(): Flow<SyncState> = combine(
        observeSyncStatus(),
        observeMode(),
        ::SyncState
    )

}
