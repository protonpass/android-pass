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

package proton.android.pass.data.fakes.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.some
import proton.android.pass.data.api.repositories.BulkMoveToVaultEvent
import proton.android.pass.data.api.repositories.BulkMoveToVaultRepository
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeBulkMoveToVaultRepository @Inject constructor() : BulkMoveToVaultRepository {
    private val flow: MutableStateFlow<Option<Map<ShareId, List<ItemId>>>> = MutableStateFlow(None)
    private val eventFlow: MutableStateFlow<BulkMoveToVaultEvent> =
        MutableStateFlow(BulkMoveToVaultEvent.Idle)

    override suspend fun save(value: Map<ShareId, List<ItemId>>) {
        flow.emit(value.some())
    }

    override fun observe(): Flow<Option<Map<ShareId, List<ItemId>>>> = flow

    override suspend fun delete() {
        flow.emit(None)
    }

    override suspend fun emitEvent(event: BulkMoveToVaultEvent) {
        eventFlow.emit(event)
    }

    override fun observeEvent(): Flow<BulkMoveToVaultEvent> = eventFlow

}
