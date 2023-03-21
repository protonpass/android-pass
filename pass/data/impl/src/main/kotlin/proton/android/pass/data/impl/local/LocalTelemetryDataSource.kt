package proton.android.pass.data.impl.local

import me.proton.core.domain.entity.UserId
import proton.android.pass.data.impl.db.PassDatabase
import proton.android.pass.data.impl.db.entities.TelemetryEntity
import javax.inject.Inject

interface LocalTelemetryDataSource {
    suspend fun store(entity: TelemetryEntity)
    suspend fun getAll(userId: UserId): List<TelemetryEntity>
    suspend fun removeInRange(min: Long, max: Long)
}

class LocalTelemetryDataSourceImpl @Inject constructor(
    private val db: PassDatabase
) : LocalTelemetryDataSource {
    override suspend fun store(entity: TelemetryEntity) {
        db.telemetryEventsDao().insertOrUpdate(entity)
    }

    override suspend fun getAll(userId: UserId): List<TelemetryEntity> =
        db.telemetryEventsDao().getAll(userId.id)

    override suspend fun removeInRange(min: Long, max: Long) =
        db.telemetryEventsDao().deleteInRange(min, max)
}
