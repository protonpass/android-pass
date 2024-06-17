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

package proton.android.pass.data.impl.fakes

import me.proton.core.domain.entity.UserId
import proton.android.pass.data.impl.db.entities.LiveTelemetryEntity
import proton.android.pass.data.impl.local.LocalLiveTelemetryDataSource

class FakeLocalLiveTelemetryDataSource : LocalLiveTelemetryDataSource {

    private var pending: MutableMap<UserId, List<LiveTelemetryEntity>> = mutableMapOf()
    private var memory: MutableMap<UserId, List<LiveTelemetryEntity>> = mutableMapOf()

    fun getMemory(): Map<UserId, List<LiveTelemetryEntity>> = memory
    fun getMemory(userId: UserId): List<LiveTelemetryEntity> = memory[userId] ?: emptyList()

    fun setPending(userId: UserId, events: List<LiveTelemetryEntity>) {
        pending[userId] = events
    }

    override suspend fun getPendingEvents(userId: UserId): List<LiveTelemetryEntity> = pending[userId] ?: emptyList()

    override suspend fun deletePendingEvents(userId: UserId, events: List<Long>) {
        pending.computeIfPresent(userId) { _, list ->
            list.filter { it.id !in events }
        }
    }

    override suspend fun storeEvents(events: List<LiveTelemetryEntity>) {
        events.groupBy { it.userId }.forEach { (userId, eventsToStore) ->
            memory.compute(UserId(userId)) { _, existing ->
                (existing ?: emptyList()) + eventsToStore
            }
        }
    }
}
