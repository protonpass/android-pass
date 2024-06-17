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
import proton.android.pass.data.impl.remote.RemoteLiveTelemetryDataSource
import proton.android.pass.data.impl.requests.ItemReadRequest
import proton.android.pass.domain.ShareId

class FakeRemoteLiveTelemetryDataSource : RemoteLiveTelemetryDataSource {

    private var result: Result<Unit> = Result.success(Unit)
    private var timedResult: MutableList<Result<Unit>> = mutableListOf()

    private val memory: MutableList<Payload> = mutableListOf()
    fun getMemory(): List<Payload> = memory

    fun setResult(value: Result<Unit>) {
        result = value
    }

    fun setTimedResult(value: List<Result<Unit>>) {
        timedResult = value.toMutableList()
    }

    private fun getResult(): Result<Unit> {
        val result = timedResult.firstOrNull()
        if (result != null) {
            timedResult.removeAt(0)
        }
        return result ?: this.result
    }

    override suspend fun sendEvent(
        userId: UserId,
        shareId: ShareId,
        request: ItemReadRequest
    ) {
        memory.add(Payload(userId, shareId, request))
        getResult().getOrThrow()
    }

    data class Payload(
        val userId: UserId,
        val shareId: ShareId,
        val request: ItemReadRequest
    )
}
