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
import proton.android.pass.common.api.FlowUtils.testFlow
import proton.android.pass.data.api.repositories.ItemSyncStatus
import proton.android.pass.data.api.repositories.ItemSyncStatusPayload
import proton.android.pass.data.api.repositories.ItemSyncStatusRepository
import proton.android.pass.data.api.repositories.SyncMode
import proton.pass.domain.ShareId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestItemSyncStatusRepository @Inject constructor() : ItemSyncStatusRepository {

    private val flow: MutableSharedFlow<ItemSyncStatus> = testFlow<ItemSyncStatus>()
        .apply { tryEmit(ItemSyncStatus.NotStarted) }
    private val accumulatedFlow: MutableSharedFlow<Map<ShareId, ItemSyncStatusPayload>> =
        testFlow<Map<ShareId, ItemSyncStatusPayload>>()
            .apply { emptyMap<ShareId, ItemSyncStatusPayload>() }
    private val modeFlow: MutableSharedFlow<SyncMode> = testFlow<SyncMode>()
        .apply { tryEmit(SyncMode.Background) }

    fun emitAccumulated(map: Map<ShareId, ItemSyncStatusPayload>) {
        accumulatedFlow.tryEmit(map)
    }

    override suspend fun emit(status: ItemSyncStatus) {
        flow.emit(status)
    }

    override fun tryEmit(status: ItemSyncStatus) {
        flow.tryEmit(status)
    }

    override suspend fun setMode(mode: SyncMode) {
        modeFlow.emit(mode)
    }

    override fun trySetMode(mode: SyncMode) {
        modeFlow.tryEmit(mode)
    }

    override suspend fun clear() {
    }

    override fun observeMode(): Flow<SyncMode> = modeFlow

    override fun observeSyncStatus(): Flow<ItemSyncStatus> = flow

    override fun observeAccSyncStatus(): Flow<Map<ShareId, ItemSyncStatusPayload>> =
        accumulatedFlow


}
