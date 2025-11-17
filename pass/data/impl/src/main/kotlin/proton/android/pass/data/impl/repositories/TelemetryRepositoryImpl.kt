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

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.Clock
import kotlinx.serialization.json.JsonPrimitive
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.UserId
import me.proton.core.telemetry.domain.usecase.IsTelemetryEnabled
import proton.android.pass.data.api.repositories.TelemetryRepository
import proton.android.pass.data.api.usecases.GetUserPlan
import proton.android.pass.data.impl.db.entities.TelemetryEntity
import proton.android.pass.data.impl.local.LocalTelemetryDataSource
import proton.android.pass.data.impl.remote.RemoteTelemetryDataSource
import proton.android.pass.data.impl.requests.EventInfo
import proton.android.pass.data.impl.requests.TelemetryRequest
import proton.android.pass.data.impl.util.DimensionsSerializer
import proton.android.pass.data.impl.util.runConcurrently
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

class TelemetryRepositoryImpl @Inject constructor(
    private val localDataSource: LocalTelemetryDataSource,
    private val remoteDataSource: RemoteTelemetryDataSource,
    private val accountManager: AccountManager,
    private val getUserPlan: GetUserPlan,
    private val clock: Clock,
    private val isTelemetryEnabled: IsTelemetryEnabled
) : TelemetryRepository {
    override suspend fun storeEntry(event: String, dimensions: Map<String, String>) {
        val userId = requireNotNull(accountManager.getPrimaryUserId().first())
        val dimensionsAsString = DimensionsSerializer.serialize(dimensions)

        val entity = TelemetryEntity(
            id = 0, // Default value for generating the actual ID with auto increment
            userId = userId.id,
            event = event,
            dimensions = dimensionsAsString,
            createTime = clock.now().epochSeconds
        )
        localDataSource.store(entity)
    }

    override suspend fun sendEvents() {
        val eventsGrouped = localDataSource.getAll().groupBy { it.userId }.mapKeys { UserId(it.key) }
        runConcurrently(
            items = eventsGrouped.entries,
            block = { (userId, events) ->
                val planName = requireNotNull(getUserPlan(userId).firstOrNull())
                val planInternalName = planName.planType.internalName
                events.chunked(MAX_EVENT_BATCH_SIZE).forEach { eventChunk ->
                    runCatching {
                        performSend(userId, planInternalName, eventChunk)
                    }.onSuccess {
                        val min = eventChunk.first().id
                        val max = eventChunk.last().id
                        localDataSource.removeInRange(userId = userId, min = min, max = max)
                    }.onFailure {
                        PassLogger.w(TAG, "Error sending events")
                        PassLogger.w(TAG, it)
                    }
                }
            },
            onSuccess = { _, _ ->
                PassLogger.d(TAG, "Events sent successfully")
            },
            onFailure = { _, throwable ->
                PassLogger.w(TAG, "Error sending events")
                PassLogger.w(TAG, throwable)
            }
        )
    }

    private suspend fun performSend(
        userId: UserId,
        planName: String,
        events: List<TelemetryEntity>
    ) {
        if (shouldSendTelemetry(userId)) {
            val request = buildRequest(planName, events)
            remoteDataSource.send(userId, request)
        }
    }

    private fun buildRequest(planName: String, events: List<TelemetryEntity>): TelemetryRequest = TelemetryRequest(
        eventInfo = events.map { event ->
            val dimensions = DimensionsSerializer.deserialize(event.dimensions).toMutableMap()
            dimensions[PLAN_NAME_KEY] = JsonPrimitive(planName)

            EventInfo(
                measurementGroup = MEASUREMENT_GROUP,
                event = event.event,
                values = emptyMap(),
                dimensions = dimensions
            )
        }
    )

    private suspend fun shouldSendTelemetry(userId: UserId): Boolean = runCatching {
        isTelemetryEnabled(userId)
    }.getOrElse {
        PassLogger.w(TAG, "Error checking telemetry enabled")
        PassLogger.w(TAG, it)
        false
    }

    companion object {
        private const val TAG = "TelemetryRepositoryImpl"

        const val MAX_EVENT_BATCH_SIZE = 500
        const val MEASUREMENT_GROUP = "pass.any.user_actions"
        const val PLAN_NAME_KEY = "user_tier"
    }
}
