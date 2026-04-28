/*
 * Copyright (c) 2023-2026 Proton AG
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

import proton.android.pass.data.api.repositories.TelemetryRepository
import proton.android.pass.telemetry.api.TelemetryEvent
import javax.inject.Inject

class FakeTelemetryRepository @Inject constructor() : TelemetryRepository {

    private val memory: MutableList<TelemetryEvent.DeferredTelemetryEvent> = mutableListOf()
    private var sendInvoked = false
    private var storeResult: Result<Unit> = Result.failure(IllegalStateException("storeResult not set"))
    private var sendResult: Result<Unit> = Result.success(Unit)

    fun getMemory(): List<TelemetryEvent.DeferredTelemetryEvent> = memory
    fun getSendInvoked() = sendInvoked
    fun setStoreResult(value: Result<Unit>) {
        storeResult = value
    }

    fun setSendResult(value: Result<Unit>) {
        sendResult = value
    }

    override suspend fun storeEntry(event: TelemetryEvent.DeferredTelemetryEvent) {
        memory.add(event)
        storeResult.onFailure { throw it }
    }

    override suspend fun sendEvents() {
        sendInvoked = true
        sendResult.getOrThrow()
    }

}
