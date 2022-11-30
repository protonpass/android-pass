package me.proton.android.pass.data.impl.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.proton.android.pass.data.impl.db.PassDatabase
import me.proton.android.pass.data.impl.db.entities.PassEventEntity
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.AddressId
import me.proton.pass.domain.ShareId
import java.time.OffsetDateTime
import java.time.ZoneOffset
import javax.inject.Inject

class LocalEventDataSourceImpl @Inject constructor(
    private val database: PassDatabase
) : LocalEventDataSource {
    override fun getLatestEventId(userId: UserId, addressId: AddressId, shareId: ShareId): Flow<String?> = database
        .passEventsDao()
        .getLatestEventId(userId.id, addressId.id, shareId.id)
        .map { it?.eventId }

    override suspend fun storeLatestEventId(userId: UserId, addressId: AddressId, shareId: ShareId, eventId: String) {
        val nowInUtc = OffsetDateTime.now(ZoneOffset.UTC)
        val entity = PassEventEntity(
            eventId = eventId,
            userId = userId.id,
            addressId = addressId.id,
            shareId = shareId.id,
            retrievedAt = nowInUtc.toEpochSecond()
        )
        database.passEventsDao().insertOrUpdate(entity)
    }

}
