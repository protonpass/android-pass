package proton.android.pass.data.impl.repositories

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.Clock
import kotlinx.serialization.json.JsonPrimitive
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.UserId
import me.proton.core.usersettings.domain.repository.DeviceSettingsRepository
import proton.android.pass.data.api.repositories.TelemetryRepository
import proton.android.pass.data.api.usecases.GetUserPlan
import proton.android.pass.data.impl.db.PassDatabase
import proton.android.pass.data.impl.db.entities.TelemetryEntity
import proton.android.pass.data.impl.local.LocalTelemetryDataSource
import proton.android.pass.data.impl.remote.RemoteTelemetryDataSource
import proton.android.pass.data.impl.requests.EventInfo
import proton.android.pass.data.impl.requests.TelemetryRequest
import proton.android.pass.data.impl.util.DimensionsSerializer
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

class TelemetryRepositoryImpl @Inject constructor(
    private val passDatabase: PassDatabase,
    private val localDataSource: LocalTelemetryDataSource,
    private val remoteDataSource: RemoteTelemetryDataSource,
    private val accountManager: AccountManager,
    private val getUserPlan: GetUserPlan,
    private val clock: Clock,
    private val deviceSettingsRepository: DeviceSettingsRepository
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
        val userId = requireNotNull(accountManager.getPrimaryUserId().first())
        val planName = requireNotNull(getUserPlan(userId).firstOrNull())
        val planInternalName = planName.planType.internalName()

        passDatabase.inTransaction {
            val all = localDataSource.getAll(userId)
            if (all.isNotEmpty()) {
                all.chunked(MAX_EVENT_BATCH_SIZE).forEach { eventChunk ->
                    runCatching {
                        performSend(userId, planInternalName, eventChunk)
                    }.onSuccess {
                        val min = eventChunk.first().id
                        val max = eventChunk.last().id
                        localDataSource.removeInRange(min = min, max = max)
                    }.onFailure {
                        PassLogger.w(TAG, it, "Error sending events")
                    }
                }
            }
        }
    }

    private suspend fun performSend(userId: UserId, planName: String, events: List<TelemetryEntity>) {
        if (shouldSendTelemetry()) {
            val request = buildRequest(planName, events)
            remoteDataSource.send(userId, request)
        }
    }

    private fun buildRequest(planName: String, events: List<TelemetryEntity>): TelemetryRequest =
        TelemetryRequest(
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

    private suspend fun shouldSendTelemetry(): Boolean {
        val settings = deviceSettingsRepository.getDeviceSettings()
        return settings.isTelemetryEnabled
    }

    companion object {
        private const val TAG = "TelemetryRepositoryImpl"

        const val MAX_EVENT_BATCH_SIZE = 500
        const val MEASUREMENT_GROUP = "pass.any.user_actions"
        const val PLAN_NAME_KEY = "user_tier"
    }
}
