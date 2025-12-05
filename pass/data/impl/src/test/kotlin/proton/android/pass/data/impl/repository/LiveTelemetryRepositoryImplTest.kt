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

package proton.android.pass.data.impl.repository

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.data.fakes.usecases.FakeGetUserPlan
import proton.android.pass.data.impl.db.entities.LiveTelemetryEntity
import proton.android.pass.data.impl.fakes.FakeLocalLiveTelemetryDataSource
import proton.android.pass.data.impl.fakes.FakeRemoteLiveTelemetryDataSource
import proton.android.pass.data.impl.repositories.LiveTelemetryRepositoryImpl
import proton.android.pass.data.impl.requests.ItemReadBody
import proton.android.pass.data.impl.requests.ItemReadRequest
import proton.android.pass.data.impl.util.DimensionsSerializer
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.Plan
import proton.android.pass.domain.PlanLimit
import proton.android.pass.domain.PlanType
import proton.android.pass.domain.ShareId
import proton.android.pass.network.api.NetworkStatus
import proton.android.pass.network.fakes.FakeNetworkMonitor
import proton.android.pass.telemetry.api.TelemetryEvent.LiveTelemetryEvent
import proton.android.pass.telemetry.api.events.ItemViewed
import proton.android.pass.test.FixedClock
import proton.android.pass.test.MainDispatcherRule

class LiveTelemetryRepositoryImplTest {

    @get:Rule
    val rule = MainDispatcherRule()

    private lateinit var instance: LiveTelemetryRepositoryImpl

    private lateinit var networkMonitor: FakeNetworkMonitor
    private lateinit var clock: FixedClock
    private lateinit var getUserPlan: FakeGetUserPlan
    private lateinit var local: FakeLocalLiveTelemetryDataSource
    private lateinit var remote: FakeRemoteLiveTelemetryDataSource

    @Before
    fun setup() {
        clock = FixedClock()
        networkMonitor = FakeNetworkMonitor()
        getUserPlan = FakeGetUserPlan()
        local = FakeLocalLiveTelemetryDataSource()
        remote = FakeRemoteLiveTelemetryDataSource()

        instance = LiveTelemetryRepositoryImpl(
            networkMonitor = networkMonitor,
            local = local,
            remote = remote,
            clock = clock,
            getUserPlan = getUserPlan
        )

        // Initialize to business plan for most of the tests
        setupPlan(businessPlan)
    }

    @Test
    fun `does not send event for free user`() = runTest {
        setupPlan(freePlan)
        instance.sendEvent(userId = USER_ID, event = sampleEvent)

        // Check that it has not called any repository
        assertThat(local.getMemory(USER_ID)).isEmpty()
        assertThat(remote.getMemory()).isEmpty()
    }

    @Test
    fun `does not send event for plus user`() = runTest {
        setupPlan(plusPlan)
        instance.sendEvent(userId = USER_ID, event = sampleEvent)

        // Check that it has not called any repository
        assertThat(local.getMemory(USER_ID)).isEmpty()
        assertThat(remote.getMemory()).isEmpty()
    }

    @Test
    fun `stores for business user in case of no network`() = runTest {
        networkMonitor.emit(NetworkStatus.Offline)
        instance.sendEvent(userId = USER_ID, event = sampleEvent)

        // Check that it has not called the remote
        assertThat(remote.getMemory()).isEmpty()

        // Check that it has stored it locally
        val localMemory = local.getMemory(USER_ID)
        assertThat(localMemory.size).isEqualTo(1)
    }

    @Test
    fun `sends for business user in case of network`() = runTest {
        instance.sendEvent(userId = USER_ID, event = sampleEvent)

        // Check that it has not called the local
        assertThat(local.getMemory()).isEmpty()

        // Check that it has called the remote
        val remoteMemory = remote.getMemory()
        assertThat(remoteMemory.size).isEqualTo(1)
    }

    @Test
    fun `does not send from worker in case of no network`() = runTest {
        networkMonitor.emit(NetworkStatus.Offline)
        local.setPending(USER_ID, listOf(sampleEvent.toEntity(USER_ID)))

        instance.flushPendingEvents(USER_ID)

        assertThat(local.getMemory()).isEmpty()
        assertThat(remote.getMemory()).isEmpty()
    }

    @Test
    fun `deletes sent events from local`() = runTest {
        val entities = (0 until 3).map { sampleEvent.toEntity(USER_ID, it.toLong()) }
        local.setPending(USER_ID, entities)
        assertThat(local.getPendingEvents(USER_ID)).isEqualTo(entities)

        instance.flushPendingEvents(USER_ID)

        assertThat(local.getPendingEvents(USER_ID)).isEmpty()

        val remoteMemory = remote.getMemory()
        assertThat(remoteMemory.size).isEqualTo(1)

        val remoteMemoryItem = remoteMemory[0]
        assertThat(remoteMemoryItem.userId).isEqualTo(USER_ID)
        assertThat(remoteMemoryItem.shareId).isEqualTo(SHARE_ID)

        val request = ItemReadRequest(
            itemTimes = entities.map { entity ->
                ItemReadBody(
                    itemId = ITEM_ID.id,
                    timestamp = entity.createTime
                )
            }
        )
        assertThat(remoteMemoryItem.request).isEqualTo(request)
    }

    @Test
    fun `deletes events that do not need to be sent`() = runTest {
        // These events are in the pending list but should not be sent
        setupPlan(freePlan)
        val entities = (0 until 3).map { sampleEvent.toEntity(USER_ID, it.toLong()) }
        local.setPending(USER_ID, entities)
        assertThat(local.getPendingEvents(USER_ID)).isEqualTo(entities)

        // Try to send the events
        instance.flushPendingEvents(USER_ID)

        // There should be 0 pending events
        assertThat(local.getPendingEvents(USER_ID)).isEmpty()

        // There should be no events in the remote
        assertThat(remote.getMemory()).isEmpty()

        // There should be 3 events in the delete memory
        val deletedEntityIds = entities.map { it.id }
        assertThat(local.getDeleteMemory(USER_ID)).isEqualTo(deletedEntityIds)
    }

    @Test
    fun `chunks requests when sending`() = runTest {
        val entities = (0 until 100).map { sampleEvent.toEntity(USER_ID, it.toLong()) }
        local.setPending(USER_ID, entities)

        instance.flushPendingEvents(USER_ID)

        assertThat(local.getPendingEvents(USER_ID)).isEmpty()

        val remoteMemory = remote.getMemory()
        assertThat(remoteMemory.size).isEqualTo(2)

        remoteMemory.forEachIndexed { idx, remoteMemoryItem ->
            assertThat(remoteMemoryItem.userId).isEqualTo(USER_ID)
            assertThat(remoteMemoryItem.shareId).isEqualTo(SHARE_ID)

            val expectedEntities = when (idx) {
                0 -> entities.subList(0, 50)
                1 -> entities.subList(50, 100)
                else -> error("Unexpected index")
            }

            val request = ItemReadRequest(
                itemTimes = expectedEntities.map { entity ->
                    ItemReadBody(
                        itemId = ITEM_ID.id,
                        timestamp = entity.createTime
                    )
                }
            )
            assertThat(remoteMemoryItem.request).isEqualTo(request)
        }
    }

    @Test
    fun `does not delete pending when error in sending`() = runTest {
        val entities = (0 until 100).map { sampleEvent.toEntity(USER_ID, it.toLong()) }
        local.setPending(USER_ID, entities)

        // Make the first one fail, the second one succeed
        val remoteResults = listOf(
            Result.failure(IllegalStateException("test")),
            Result.success(Unit)
        )
        remote.setTimedResult(remoteResults)

        instance.flushPendingEvents(USER_ID)

        // Check that the first chunk is still in memory
        val pendingEvents = local.getPendingEvents(USER_ID)
        assertThat(pendingEvents).isNotEmpty()
        assertThat(pendingEvents.size).isEqualTo(50)
        assertThat(pendingEvents).isEqualTo(entities.subList(0, 50))


        // Check that the two requests reached the remote
        val remoteMemory = remote.getMemory()
        assertThat(remoteMemory.size).isEqualTo(2)

        remoteMemory.forEachIndexed { idx, remoteMemoryItem ->
            assertThat(remoteMemoryItem.userId).isEqualTo(USER_ID)
            assertThat(remoteMemoryItem.shareId).isEqualTo(SHARE_ID)

            val expectedEntities = when (idx) {
                0 -> entities.subList(0, 50)
                1 -> entities.subList(50, 100)
                else -> error("Unexpected index")
            }

            val request = ItemReadRequest(
                itemTimes = expectedEntities.map { entity ->
                    ItemReadBody(
                        itemId = ITEM_ID.id,
                        timestamp = entity.createTime
                    )
                }
            )
            assertThat(remoteMemoryItem.request).isEqualTo(request)
        }
    }

    private fun setupPlan(planType: PlanType) {
        val plan = Plan(
            planType = planType,
            hideUpgrade = false,
            vaultLimit = PlanLimit.Unlimited,
            aliasLimit = PlanLimit.Unlimited,
            totpLimit = PlanLimit.Unlimited,
            updatedAt = clock.now().epochSeconds
        )
        getUserPlan.setResult(userId = USER_ID, value = Result.success(plan))
    }

    private fun LiveTelemetryEvent.toEntity(userId: UserId, id: Long = 0) = LiveTelemetryEntity(
        id = id,
        userId = userId.id,
        event = eventName,
        dimensions = DimensionsSerializer.serialize(dimensions()),
        createTime = clock.now().epochSeconds
    )

    companion object {
        private val USER_ID = UserId("LiveTelemetryRepositoryImplTest-UserId")
        private val SHARE_ID = ShareId("LiveTelemetryRepositoryImplTest-ShareId")
        private val ITEM_ID = ItemId("LiveTelemetryRepositoryImplTest-ItemId")

        private val freePlan = PlanType.Free("free", "Free")
        private val plusPlan = PlanType.Paid.Plus("plus", "Plus")
        private val businessPlan = PlanType.Paid.Business("business", "Business")

        private val sampleEvent = ItemViewed(
            shareId = SHARE_ID,
            itemId = ITEM_ID
        )
    }
}
