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

package proton.android.pass.data.impl.repositories

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.Clock
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.safeRunCatching
import proton.android.pass.common.api.some
import proton.android.pass.data.api.repositories.LiveTelemetryRepository
import proton.android.pass.data.api.usecases.GetUserPlan
import proton.android.pass.data.impl.db.entities.LiveTelemetryEntity
import proton.android.pass.data.impl.local.LocalLiveTelemetryDataSource
import proton.android.pass.data.impl.remote.RemoteLiveTelemetryDataSource
import proton.android.pass.data.impl.requests.ItemReadBody
import proton.android.pass.data.impl.requests.ItemReadRequest
import proton.android.pass.data.impl.util.DimensionsSerializer
import proton.android.pass.domain.Plan
import proton.android.pass.domain.ShareId
import proton.android.pass.log.api.PassLogger
import proton.android.pass.network.api.NetworkMonitor
import proton.android.pass.network.api.NetworkStatus
import proton.android.pass.telemetry.api.TelemetryEvent.LiveTelemetryEvent
import proton.android.pass.telemetry.api.events.ItemViewed
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LiveTelemetryRepositoryImpl @Inject constructor(
    private val networkMonitor: NetworkMonitor,
    private val local: LocalLiveTelemetryDataSource,
    private val remote: RemoteLiveTelemetryDataSource,
    private val clock: Clock,
    private val getUserPlan: GetUserPlan
) : LiveTelemetryRepository {

    private var userIdPlanCache: MutableMap<UserId, Plan> = mutableMapOf()

    override suspend fun sendEvent(userId: UserId, event: LiveTelemetryEvent) {
        val plan = getPlanForUser(userId)
        if (!shouldSendEvent(plan, event)) {
            PassLogger.d(TAG, "Discarding event as it does not need to be sent")
            return
        }

        val networkStatus = networkMonitor.connectivity.firstOrNull() ?: NetworkStatus.Offline

        when (networkStatus) {
            NetworkStatus.Online -> trySendEvents(userId, listOf(EventToSend(None, event)))
            NetworkStatus.Offline -> storeEvents(userId, listOf(event))
        }
    }

    override suspend fun flushPendingEvents(userId: UserId) {
        val networkStatus = networkMonitor.connectivity.firstOrNull() ?: NetworkStatus.Offline
        if (networkStatus == NetworkStatus.Offline) return

        val pending = getPendingEvents(userId)
        trySendEvents(userId, pending)
    }

    private suspend fun trySendEvents(userId: UserId, events: List<EventToSend>) {
        val plan = getPlanForUser(userId)
        val (eventsToSend, eventsToDelete) = events.partition { shouldSendEvent(plan, it.event) }

        // Delete events that are not going to be sent
        local.deletePendingEvents(userId, eventsToDelete.mapNotNull { it.entity.value()?.id })

        sendItemViewedEvents(userId, eventsToSend)
    }

    private suspend fun sendItemViewedEvents(userId: UserId, events: List<EventToSend>) {
        val itemViewedEvents: List<ItemViewedEventToSend> = events
            .filter { it.event is ItemViewed }
            .map { ItemViewedEventToSend(it.entity, it.event as ItemViewed) }

        sendItemViewed(userId, itemViewedEvents)
    }

    private suspend fun sendItemViewed(userId: UserId, events: List<ItemViewedEventToSend>) {
        if (events.isEmpty()) return

        val requestsPerShare: Map<ShareId, List<ItemViewedEventToSend>> =
            events.groupBy { it.event.shareId }

        coroutineScope {
            requestsPerShare.map { (shareId, items) ->
                items.chunked(CHUNK_SIZE).map { entitiesItems ->
                    async {
                        val request = ItemReadRequest(
                            entitiesItems.map { item ->
                                ItemReadBody(
                                    itemId = item.event.itemId.id,
                                    timestamp = clock.now().epochSeconds
                                )
                            }
                        )

                        safeRunCatching {
                            remote.sendEvent(
                                userId = userId,
                                shareId = shareId,
                                request = request
                            )
                        }.onSuccess {
                            PassLogger.i(TAG, "Sent LiveTelemetryEvent")
                            deleteIfStored(userId, entitiesItems)
                        }.onFailure {
                            PassLogger.w(TAG, "Could not send LiveTelemetryEvent. Storing it locally")
                            PassLogger.w(TAG, it)
                            storeIfNotStored(userId, entitiesItems)
                        }

                    }
                }
            }
        }.flatten().awaitAll()
    }

    private suspend fun deleteIfStored(userId: UserId, events: List<ItemViewedEventToSend>) {
        val ids = events.mapNotNull { (entity, _) -> entity.value()?.id }
        local.deletePendingEvents(userId, ids)
    }

    private suspend fun storeIfNotStored(userId: UserId, events: List<ItemViewedEventToSend>) {
        val notStored = events.filter { it.entity.isEmpty() }
        storeEvents(userId, notStored.map { it.event })
    }

    private suspend fun getPendingEvents(userId: UserId): List<EventToSend> =
        local.getPendingEvents(userId).mapNotNull { entity ->
            when (entity.event) {
                ItemViewed.EVENT_NAME -> {
                    val dimensions: Map<String, String> = DimensionsSerializer
                        .deserialize(entity.dimensions)
                        .mapValues { it.value.content }

                    when (val item = ItemViewed.fromDimensions(dimensions)) {
                        None -> {
                            PassLogger.w(TAG, "Could not deserialize ItemViewed event")
                            null
                        }
                        is Some -> EventToSend(entity.some(), item.value)
                    }
                }

                else -> {
                    PassLogger.w(TAG, "Unknown event type: ${entity.event}")
                    null
                }
            }
        }

    private fun shouldSendEvent(plan: Plan?, event: LiveTelemetryEvent): Boolean {
        return when (event) {
            is ItemViewed -> plan?.isBusinessPlan ?: false
            else -> false
        }
    }

    private suspend fun storeEvents(userId: UserId, events: List<LiveTelemetryEvent>) {
        local.storeEvents(events.map { it.toEntity(userId) })
    }

    private suspend fun getPlanForUser(userId: UserId): Plan {
        return userIdPlanCache.getOrPut(userId) {
            getUserPlan(userId).firstOrNull()
                ?: throw IllegalStateException("Cannot get plan for user ${userId.id}")
        }
    }

    private fun LiveTelemetryEvent.toEntity(userId: UserId) = LiveTelemetryEntity(
        id = 0,
        userId = userId.id,
        event = eventName,
        dimensions = DimensionsSerializer.serialize(dimensions()),
        createTime = clock.now().epochSeconds
    )

    private data class EventToSend(
        val entity: Option<LiveTelemetryEntity>,
        val event: LiveTelemetryEvent
    )

    private data class ItemViewedEventToSend(
        val entity: Option<LiveTelemetryEntity>,
        val event: ItemViewed
    )

    companion object {
        private const val TAG = "LiveTelemetryRepositoryImpl"

        private const val CHUNK_SIZE = 50
    }
}
