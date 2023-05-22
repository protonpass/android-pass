package proton.android.pass.data.impl.repository

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import me.proton.core.domain.entity.UserId
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.account.fakes.TestAccountManager
import proton.android.pass.data.fakes.usecases.TestGetUserPlan
import proton.android.pass.data.impl.db.entities.TelemetryEntity
import proton.android.pass.data.impl.fakes.TestLocalTelemetryDataSource
import proton.android.pass.data.impl.fakes.TestPassDatabase
import proton.android.pass.data.impl.fakes.TestRemoteTelemetryDataSource
import proton.android.pass.data.impl.repositories.TelemetryRepositoryImpl
import proton.android.pass.data.impl.util.DimensionsSerializer
import proton.android.pass.test.FixedClock
import proton.android.pass.test.MainDispatcherRule
import proton.pass.domain.Plan
import proton.pass.domain.PlanType

class TelemetryRepositoryTest {

    @get:Rule
    val dispatcher = MainDispatcherRule()

    private lateinit var instance: TelemetryRepositoryImpl
    private lateinit var accountManager: TestAccountManager
    private lateinit var getUserPlan: TestGetUserPlan
    private lateinit var localDataSource: TestLocalTelemetryDataSource
    private lateinit var remoteDataSource: TestRemoteTelemetryDataSource
    private lateinit var clock: Clock

    @Before
    fun setup() {
        accountManager = TestAccountManager()
        getUserPlan = TestGetUserPlan()
        localDataSource = TestLocalTelemetryDataSource()
        remoteDataSource = TestRemoteTelemetryDataSource()
        clock = FixedClock(Clock.System.now())

        instance = TelemetryRepositoryImpl(
            passDatabase = TestPassDatabase(),
            localDataSource = localDataSource,
            remoteDataSource = remoteDataSource,
            accountManager = accountManager,
            getUserPlan = getUserPlan,
            clock = clock
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
        assertThat(deserializedDimensions.get("some")!!.content).isEqualTo("dimension")
        assertThat(deserializedDimensions.get("other")!!.content).isEqualTo("value")
    }

    @Test
    fun `sendEvents can work with empty results`() = runTest {
        // GIVEN
        accountManager.sendPrimaryUserId(UserId("123"))
        getUserPlan.setResult(Result.success(planWithType(PlanType.Paid("plan", "plan"))))

        // WHEN
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

            assertThat(it.dimensions.get(TelemetryRepositoryImpl.PLAN_NAME_KEY)!!.content)
                .isEqualTo(plan)
        }

        // Verify removals
        val operations = localDataSource.getOperationMemory()
        val removalOperations = operations
            .filter { it is TestLocalTelemetryDataSource.Operation.RemoveInRange }
            .map { it as TestLocalTelemetryDataSource.Operation.RemoveInRange }
        assertThat(removalOperations.size).isEqualTo(2)

        assertThat(removalOperations[0].min).isEqualTo(0)
        assertThat(removalOperations[0].max).isEqualTo(TelemetryRepositoryImpl.MAX_EVENT_BATCH_SIZE - 1)

        assertThat(removalOperations[1].min).isEqualTo(TelemetryRepositoryImpl.MAX_EVENT_BATCH_SIZE)
        assertThat(removalOperations[1].max).isEqualTo(TelemetryRepositoryImpl.MAX_EVENT_BATCH_SIZE)
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
            .filter { it is TestLocalTelemetryDataSource.Operation.RemoveInRange }
        assertThat(removalOperations).isEmpty() // No removals should have been called
    }

    private suspend fun runSetup(
        numItems: Int,
        userId: String,
        plan: String,
        event: String
    ) {
        accountManager.sendPrimaryUserId(UserId(userId))
        getUserPlan.setResult(Result.success(planWithType(PlanType.Paid(plan, plan))))

        (0 until numItems).forEach { idx ->
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
        vaultLimit = 1,
        aliasLimit = 1,
        totpLimit = 1,
        updatedAt = Clock.System.now().epochSeconds
    )
}
