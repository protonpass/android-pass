package me.proton.android.pass.data.impl.repositories

import me.proton.android.pass.data.impl.responses.EventList
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.AddressId
import me.proton.pass.domain.ShareId

interface EventRepository {
    suspend fun getEvents(userId: UserId, addressId: AddressId, shareId: ShareId): EventList
    suspend fun storeLatestEventId(userId: UserId, addressId: AddressId, shareId: ShareId, eventId: String)
}
