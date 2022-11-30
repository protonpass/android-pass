package me.proton.android.pass.data.impl.local

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.AddressId
import me.proton.pass.domain.ShareId

interface LocalEventDataSource {
    fun getLatestEventId(userId: UserId, addressId: AddressId, shareId: ShareId): Flow<String?>
    suspend fun storeLatestEventId(userId: UserId, addressId: AddressId, shareId: ShareId, eventId: String)
}
