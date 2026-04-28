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

package proton.android.pass.data.impl.repositories

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.Clock
import kotlinx.serialization.json.JsonPrimitive
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.UserId
import me.proton.core.telemetry.domain.usecase.IsTelemetryEnabled
import proton.android.pass.appconfig.api.AppConfig
import proton.android.pass.appconfig.api.BuildFlavor.Companion.supportTelemetryGrowth
import proton.android.pass.common.api.safeRunCatching
import proton.android.pass.data.api.repositories.TelemetryRepository
import proton.android.pass.data.api.usecases.GetUserPlan
import proton.android.pass.data.impl.db.entities.TelemetryEntity
import proton.android.pass.data.impl.db.entities.TelemetryGrowthEntity
import proton.android.pass.data.impl.local.LocalTelemetryGrowthDataSource
import proton.android.pass.data.impl.local.LocalTelemetryDataSource
import proton.android.pass.data.impl.remote.RemoteTelemetryGrowthDataSource
import proton.android.pass.data.impl.remote.RemoteTelemetryDataSource
import proton.android.pass.data.impl.requests.EventInfo
import proton.android.pass.data.impl.requests.TelemetryGrowthBatchRequest
import proton.android.pass.data.impl.requests.TelemetryRequest
import proton.android.pass.data.impl.util.DimensionsSerializer
import proton.android.pass.data.impl.util.buildTelemetryGrowthEventRequest
import proton.android.pass.data.impl.util.runConcurrently
import proton.android.pass.log.api.PassLogger
import proton.android.pass.telemetry.api.TelemetryEvent
import proton.android.pass.telemetry.api.TelemetryEvent.DeferredTelemetryGrowthEvent
import proton.android.pass.telemetry.api.TelemetryGrowthDeviceInfoProvider
import proton.android.pass.telemetry.api.TelemetryGrowthEventNames
import javax.inject.Inject

class TelemetryRepositoryImpl @Inject constructor(
    private val localDataSource: LocalTelemetryDataSource,
    private val localTelemetryGrowthDataSource: LocalTelemetryGrowthDataSource,
    private val remoteDataSource: RemoteTelemetryDataSource,
    private val remoteTelemetryGrowthDataSource: RemoteTelemetryGrowthDataSource,
    private val telemetryGrowthDeviceInfoProvider: TelemetryGrowthDeviceInfoProvider,
    private val accountManager: AccountManager,
    private val getUserPlan: GetUserPlan,
    private val clock: Clock,
    private val isTelemetryEnabled: IsTelemetryEnabled,
    private val appConfig: AppConfig
) : TelemetryRepository {
    override suspend fun storeEntry(event: TelemetryEvent.DeferredTelemetryEvent) {
        val dimensionsAsString = DimensionsSerializer.serialize(event.dimensions())
        when (event) {
            is DeferredTelemetryGrowthEvent -> {
                val entity = TelemetryGrowthEntity(
                    id = 0,
                    event = event.eventName,
                    dimensions = dimensionsAsString,
                    createTime = clock.now().epochSeconds
                )
                localTelemetryGrowthDataSource.store(entity)
            }

            else -> {
                val userId = requireNotNull(accountManager.getPrimaryUserId().first())
                val entity = TelemetryEntity(
                    id = 0,
                    userId = userId.id,
                    event = event.eventName,
                    dimensions = dimensionsAsString,
                    createTime = clock.now().epochSeconds
                )
                localDataSource.store(entity)
            }
        }
    }

    override suspend fun sendEvents() {
        sendRegularTelemetryEvents()
        sendTelemetryGrowthEvents()
    }

    private suspend fun sendRegularTelemetryEvents() {
        val regularEvents = localDataSource.getAll()
        if (regularEvents.isEmpty()) return

        val eventsGrouped = regularEvents.groupBy { it.userId }.mapKeys { UserId(it.key) }
        runConcurrently(
            items = eventsGrouped.entries,
            block = { (userId, events) -> sendRegularEvents(userId, events) },
            onSuccess = { _, _ -> PassLogger.d(TAG, "Regular events sent successfully") },
            onFailure = { _, throwable ->
                PassLogger.w(TAG, "Error sending regular events")
                PassLogger.w(TAG, throwable)
            }
        )
    }

    private suspend fun sendTelemetryGrowthEvents() {
        val telemetryGrowthEvents = localTelemetryGrowthDataSource.getAll()
        if (telemetryGrowthEvents.isEmpty()) return

        when {
            !appConfig.flavor.supportTelemetryGrowth() -> {
                deleteTelemetryGrowthEvents(telemetryGrowthEvents)
            }

            else -> {
                // userID is here just to be able to send the event with correct bearer
                val userId = accountManager.getPrimaryUserId().first()
                if (userId != null) {
                    sendTelemetryGrowthEvents(userId, telemetryGrowthEvents)
                } else {
                    PassLogger.w(TAG, "No primary user found, cannot send TelemetryGrowth events")
                }
            }
        }
    }

    private suspend fun deleteTelemetryEvent(userId: UserId, events: List<TelemetryEntity>) {
        val min = events.first().id
        val max = events.last().id
        localDataSource.removeInRange(userId = userId, min = min, max = max)
    }

    private suspend fun deleteTelemetryGrowthEvents(events: List<TelemetryGrowthEntity>) {
        val min = events.first().id
        val max = events.last().id
        localTelemetryGrowthDataSource.removeInRange(min = min, max = max)
    }

    private suspend fun sendRegularEvents(userId: UserId, events: List<TelemetryEntity>) {
        if (!shouldSendTelemetry(userId)) {
            deleteTelemetryEvent(userId, events)
            PassLogger.i(TAG, "Regular events dropped (telemetry disabled)")
            return
        }
        val planName = requireNotNull(getUserPlan(userId).firstOrNull())
        val planInternalName = planName.planType.internalName
        events.chunked(MAX_EVENT_BATCH_SIZE).forEach { eventChunk ->
            safeRunCatching {
                performSend(userId, planInternalName, eventChunk)
            }.onSuccess {
                val min = eventChunk.first().id
                val max = eventChunk.last().id
                localDataSource.removeInRange(userId = userId, min = min, max = max)
            }.onFailure {
                PassLogger.w(TAG, "Error sending regular events")
                PassLogger.w(TAG, it)
            }
        }
    }

    private suspend fun sendTelemetryGrowthEvents(authUserId: UserId, events: List<TelemetryGrowthEntity>) {
        if (!shouldSendTelemetry(authUserId)) {
            deleteTelemetryGrowthEvents(events)
            PassLogger.i(TAG, "TelemetryGrowth events dropped (telemetry disabled)")
            return
        }
        events.chunked(MAX_TELEMETRY_GROWTH_EVENT_BATCH_SIZE).forEach { eventChunk ->
            safeRunCatching {
                performTelemetryGrowthSend(eventChunk)
            }.onSuccess {
                deleteTelemetryGrowthEvents(eventChunk)
            }.onFailure {
                PassLogger.w(TAG, "Error sending TelemetryGrowth events")
                PassLogger.w(TAG, it)
            }
        }
    }

    private suspend fun performSend(
        userId: UserId,
        planName: String,
        events: List<TelemetryEntity>
    ) {
        val request = buildRequest(planName, events)
        remoteDataSource.send(userId, request)
    }

    private suspend fun performTelemetryGrowthSend(events: List<TelemetryGrowthEntity>) {
        val request = buildTelemetryGrowthRequest(events)
        remoteTelemetryGrowthDataSource.send(request.events)
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

    @SuppressWarnings("ReturnCount")
    private suspend fun buildTelemetryGrowthRequest(events: List<TelemetryGrowthEntity>): TelemetryGrowthBatchRequest {
        val info = telemetryGrowthDeviceInfoProvider
        val asid = info.getAsid()
        val telemetryGrowthEvents = events.mapNotNull { entity ->
            val dimensions = DimensionsSerializer.deserialize(entity.dimensions)
            val eventType = TelemetryGrowthEventNames.entries.find { it.eventName == entity.event }
            if (eventType == null) {
                PassLogger.w(TAG, "Unknown TelemetryGrowth event: ${entity.event}")
                return@mapNotNull null
            }
            buildTelemetryGrowthEventRequest(
                eventType = eventType,
                timestampMs = entity.createTime * 1000,
                dimensions = dimensions,
                asid = asid,
                info = info
            )
        }
        return TelemetryGrowthBatchRequest(events = telemetryGrowthEvents)
    }

    private suspend fun shouldSendTelemetry(userId: UserId): Boolean = safeRunCatching {
        isTelemetryEnabled(userId)
    }.getOrElse {
        PassLogger.w(TAG, "Error checking telemetry enabled")
        PassLogger.w(TAG, it)
        false
    }

    companion object {
        private const val TAG = "TelemetryRepositoryImpl"

        const val MAX_EVENT_BATCH_SIZE = 500
        const val MAX_TELEMETRY_GROWTH_EVENT_BATCH_SIZE = 50
        const val MEASUREMENT_GROUP = "pass.any.user_actions"
        const val PLAN_NAME_KEY = "user_tier"
    }
}
