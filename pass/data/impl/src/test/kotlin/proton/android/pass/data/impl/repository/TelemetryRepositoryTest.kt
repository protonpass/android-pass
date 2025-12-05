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

package proton.android.pass.data.impl.repository

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import me.proton.core.domain.entity.UserId
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.account.fakes.FakeAccountManager
import proton.android.pass.data.fakes.usecases.FakeGetUserPlan
import proton.android.pass.data.impl.db.entities.TelemetryEntity
import proton.android.pass.data.impl.fakes.FakeIsTelemetryEnabled
import proton.android.pass.data.impl.fakes.FakeLocalTelemetryDataSource
import proton.android.pass.data.impl.fakes.FakeRemoteTelemetryDataSource
import proton.android.pass.data.impl.repositories.TelemetryRepositoryImpl
import proton.android.pass.data.impl.util.DimensionsSerializer
import proton.android.pass.domain.Plan
import proton.android.pass.domain.PlanLimit
import proton.android.pass.domain.PlanType
import proton.android.pass.test.FixedClock
import proton.android.pass.test.MainDispatcherRule

class TelemetryRepositoryTest {

    @get:Rule
    val dispatcher = MainDispatcherRule()

    private lateinit var instance: TelemetryRepositoryImpl
    private lateinit var accountManager: FakeAccountManager
    private lateinit var getUserPlan: FakeGetUserPlan
    private lateinit var localDataSource: FakeLocalTelemetryDataSource
    private lateinit var remoteDataSource: FakeRemoteTelemetryDataSource
    private lateinit var clock: Clock
    private lateinit var telemetryEnabled: FakeIsTelemetryEnabled

    @Before
    fun setup() {
        accountManager = FakeAccountManager()
        getUserPlan = FakeGetUserPlan()
        localDataSource = FakeLocalTelemetryDataSource()
        remoteDataSource = FakeRemoteTelemetryDataSource()
        clock = FixedClock(Clock.System.now())
        telemetryEnabled = FakeIsTelemetryEnabled()

        instance = TelemetryRepositoryImpl(
            localDataSource = localDataSource,
            remoteDataSource = remoteDataSource,
            accountManager = accountManager,
            getUserPlan = getUserPlan,
            clock = clock,
            isTelemetryEnabled = telemetryEnabled
        )
    }

    @Test
    fun `store stores the correct values into local data source`() = runTest {
        // GIVEN
        val event = "testevent"
        val dimensions = mapOf("some" to "dimension", "other" to "value")
        val userId = "123"
        accountManager.sendPrimaryUserId(UserId(userId))

        // WHEN
        instance.storeEntry(event, dimensions)

        // THEN
        val memory = localDataSource.getMemory()
        assertThat(memory.size).isEqualTo(1)

        val item = memory[0]
        assertThat(item.id).isEqualTo(0)
        assertThat(item.userId).isEqualTo(userId)
        assertThat(item.event).isEqualTo(event)
        assertThat(item.createTime).isEqualTo(clock.now().epochSeconds)

        val deserializedDimensions = DimensionsSerializer.deserialize(item.dimensions)
        assertThat(deserializedDimensions.size).isEqualTo(2)
        assertThat(deserializedDimensions["some"]!!.content).isEqualTo("dimension")
        assertThat(deserializedDimensions["other"]!!.content).isEqualTo("value")
    }

    @Test
    fun `sendEvents can work with empty results`() = runTest {
        val plan = PlanType.Paid.Plus("plan", "plan")
        val userId = UserId("123")
        accountManager.sendPrimaryUserId(userId)
        getUserPlan.setResult(userId = userId, value = Result.success(planWithType(plan)))

        instance.sendEvents()

        // THEN
        // Verify there are no sent elements
        val memory = remoteDataSource.getMemory()
        assertThat(memory.size).isEqualTo(0)
    }

    @Test
    fun `sendEvents sends the events`() = runTest {
        // GIVEN
        val plan = "theplan"
        val event = "testEvent"

        runSetup(TelemetryRepositoryImpl.MAX_EVENT_BATCH_SIZE + 1, "123", plan, event)
        remoteDataSource.setResult(Result.success(Unit))

        // WHEN
        instance.sendEvents()

        // THEN
        // Verify sent elements
        val memory = remoteDataSource.getMemory()
        assertThat(memory.size).isEqualTo(2)

        memory[0].eventInfo.forEach {
            assertThat(it.event).isEqualTo(event)
            assertThat(it.measurementGroup).isEqualTo(TelemetryRepositoryImpl.MEASUREMENT_GROUP)

            assertThat(it.dimensions[TelemetryRepositoryImpl.PLAN_NAME_KEY]!!.content)
                .isEqualTo(plan)
        }

        // Verify removals
        val operations = localDataSource.getOperationMemory()
        val removalOperations =
            operations.filterIsInstance<FakeLocalTelemetryDataSource.Operation.RemoveInRange>()
        assertThat(removalOperations.size).isEqualTo(2)

        assertThat(removalOperations[0].min).isEqualTo(0)
        assertThat(removalOperations[0].max).isEqualTo(TelemetryRepositoryImpl.MAX_EVENT_BATCH_SIZE - 1)

        assertThat(removalOperations[1].min).isEqualTo(TelemetryRepositoryImpl.MAX_EVENT_BATCH_SIZE)
        assertThat(removalOperations[1].max).isEqualTo(TelemetryRepositoryImpl.MAX_EVENT_BATCH_SIZE)
    }

    @Test
    fun `sendEvents does not send events if telemetry is disabled`() = runTest {
        // GIVEN
        val plan = "theplan"
        val event = "testEvent"

        runSetup(1, "123", plan, event)
        remoteDataSource.setResult(Result.success(Unit))
        telemetryEnabled.setValue(false)

        // WHEN
        instance.sendEvents()

        // THEN
        // Verify remote data source was not called
        val memory = remoteDataSource.getMemory()
        assertThat(memory).isEmpty()
    }

    @Test
    fun `if sending to remote has an error local events are not deleted`() = runTest {
        // GIVEN
        val plan = "theplan"
        val event = "testEvent"

        runSetup(TelemetryRepositoryImpl.MAX_EVENT_BATCH_SIZE + 1, "123", plan, event)
        remoteDataSource.setResult(Result.failure(IllegalStateException("test")))

        // WHEN
        instance.sendEvents()

        // THEN
        // Verify sent elements
        val memory = remoteDataSource.getMemory()
        assertThat(memory.size).isEqualTo(2) // All elements have been tried to be sent
        assertThat(memory[0].eventInfo.size).isEqualTo(TelemetryRepositoryImpl.MAX_EVENT_BATCH_SIZE)
        assertThat(memory[1].eventInfo.size).isEqualTo(1)

        // Verify removals
        val operations = localDataSource.getOperationMemory()
        val removalOperations = operations
            .filterIsInstance<FakeLocalTelemetryDataSource.Operation.RemoveInRange>()
        assertThat(removalOperations).isEmpty() // No removals should have been called
    }

    private suspend fun runSetup(
        numItems: Int,
        userId: String,
        plan: String,
        event: String
    ) {
        accountManager.sendPrimaryUserId(UserId(userId))
        getUserPlan.setResult(
            userId = UserId(userId),
            value = Result.success(planWithType(PlanType.Paid.Plus(plan, plan)))
        )

        repeat(numItems) { idx ->
            localDataSource.store(
                TelemetryEntity(
                    id = idx.toLong(),
                    userId = userId,
                    event = event,
                    dimensions = "{}",
                    createTime = clock.now().epochSeconds
                )
            )
        }
    }

    private fun planWithType(planType: PlanType) = Plan(
        planType = planType,
        hideUpgrade = false,
        vaultLimit = PlanLimit.Unlimited,
        aliasLimit = PlanLimit.Unlimited,
        totpLimit = PlanLimit.Unlimited,
        updatedAt = Clock.System.now().epochSeconds
    )
}
