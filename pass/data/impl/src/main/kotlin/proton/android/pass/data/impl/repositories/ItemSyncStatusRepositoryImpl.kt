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
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.proton.core.presentation.app.AppLifecycleProvider
import proton.android.pass.data.api.repositories.ItemSyncStatus
import proton.android.pass.data.api.repositories.ItemSyncStatusPayload
import proton.android.pass.data.api.repositories.ItemSyncStatusRepository
import proton.android.pass.data.api.repositories.SyncMode
import proton.android.pass.domain.ShareId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ItemSyncStatusRepositoryImpl @Inject constructor(
    private val lifecycleProvider: AppLifecycleProvider
) : ItemSyncStatusRepository {

    private val syncStatus: MutableSharedFlow<ItemSyncStatus> =
        MutableSharedFlow<ItemSyncStatus>(replay = 1, extraBufferCapacity = 1)
            .apply { tryEmit(ItemSyncStatus.NotStarted) }
    private val accSyncStatus: MutableSharedFlow<Map<ShareId, ItemSyncStatusPayload>> =
        MutableSharedFlow(replay = 1, extraBufferCapacity = 1)
    private val payloadMutableMap: MutableMap<ShareId, ItemSyncStatusPayload> = mutableMapOf()
    private val modeFlow: MutableSharedFlow<SyncMode> = MutableSharedFlow<SyncMode>(
        replay = 1, extraBufferCapacity = 1
    ).apply { tryEmit(SyncMode.Background) }

    private val mutex: Mutex = Mutex()

    private suspend fun updateSyncStatus(status: ItemSyncStatus, emit: suspend (ItemSyncStatus) -> Unit) {
        mutex.withLock {
            when (status) {
                is ItemSyncStatus.Syncing -> {
                    payloadMutableMap[status.shareId] =
                        ItemSyncStatusPayload(status.current, status.total)
                    accSyncStatus.emit(payloadMutableMap.toMap())
                }

                ItemSyncStatus.NotStarted -> {
                    payloadMutableMap.clear()
                    accSyncStatus.emit(payloadMutableMap.toMap())
                }

                else -> {}
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
        payloadMutableMap.clear()
        accSyncStatus.emit(emptyMap())
        syncStatus.emit(ItemSyncStatus.NotStarted)
    }

    override fun observeSyncStatus(): Flow<ItemSyncStatus> = syncStatus

    override fun observeAccSyncStatus(): Flow<Map<ShareId, ItemSyncStatusPayload>> = accSyncStatus
}
