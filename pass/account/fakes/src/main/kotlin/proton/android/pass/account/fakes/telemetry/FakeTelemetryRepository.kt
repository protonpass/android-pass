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

package proton.android.pass.account.fakes.telemetry

import me.proton.core.domain.entity.UserId
import me.proton.core.telemetry.domain.entity.TelemetryEvent
import me.proton.core.telemetry.domain.repository.TelemetryRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeTelemetryRepository @Inject constructor() : TelemetryRepository {
    override suspend fun addEvent(userId: UserId?, event: TelemetryEvent) {
        // no-op
    }

    override suspend fun deleteAllEvents(userId: UserId?) {
        // no-op
    }

    override suspend fun deleteEvents(userId: UserId?, events: List<TelemetryEvent>) {
        // no-op
    }

    override suspend fun getEvents(userId: UserId?, limit: Int): List<TelemetryEvent> = emptyList()

    override suspend fun sendEvents(userId: UserId?, events: List<TelemetryEvent>) {
        // no-op
    }

}
