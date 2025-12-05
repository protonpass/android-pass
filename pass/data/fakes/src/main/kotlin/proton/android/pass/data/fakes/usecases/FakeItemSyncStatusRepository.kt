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

package proton.android.pass.data.fakes.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import proton.android.pass.common.api.FlowUtils.testFlow
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.data.api.repositories.ItemSyncStatus
import proton.android.pass.data.api.repositories.ItemSyncStatusPayload
import proton.android.pass.data.api.repositories.ItemSyncStatusRepository
import proton.android.pass.data.api.repositories.SyncMode
import proton.android.pass.data.api.repositories.SyncState
import proton.android.pass.domain.ShareId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeItemSyncStatusRepository @Inject constructor() : ItemSyncStatusRepository {

    private val syncStatusFlow: MutableSharedFlow<ItemSyncStatus> = testFlow<ItemSyncStatus>()
        .apply { tryEmit(ItemSyncStatus.SyncNotStarted) }

    private val accumulatedFlow: MutableSharedFlow<Map<ShareId, ItemSyncStatusPayload>> =
        testFlow<Map<ShareId, ItemSyncStatusPayload>>()
            .apply { emptyMap<ShareId, ItemSyncStatusPayload>() }

    private val syncModeFlow: MutableSharedFlow<SyncMode> = testFlow<SyncMode>()
        .apply { tryEmit(SyncMode.Background) }

    fun emitAccumulated(map: Map<ShareId, ItemSyncStatusPayload>) {
        accumulatedFlow.tryEmit(map)
    }

    override suspend fun emit(status: ItemSyncStatus) {
        syncStatusFlow.emit(status)
    }

    override fun tryEmit(status: ItemSyncStatus) {
        syncStatusFlow.tryEmit(status)
    }

    override suspend fun setMode(mode: SyncMode) {
        syncModeFlow.emit(mode)
    }

    override fun trySetMode(mode: SyncMode) {
        syncModeFlow.tryEmit(mode)
    }

    override suspend fun clear() {
    }

    override fun observeMode(): Flow<SyncMode> = syncModeFlow

    override fun observeSyncStatus(): Flow<ItemSyncStatus> = syncStatusFlow

    override fun observeDownloadedItemsStatus(): Flow<Map<ShareId, ItemSyncStatusPayload>> = accumulatedFlow

    override fun observeInsertedItemsStatus(): Flow<Option<ItemSyncStatusPayload>> = flowOf(None)

    override fun observeSyncState(): Flow<SyncState> = combine(syncStatusFlow, syncModeFlow, ::SyncState)

}
