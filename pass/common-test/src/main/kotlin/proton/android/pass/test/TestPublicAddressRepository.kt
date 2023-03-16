package proton.android.pass.test

import me.proton.core.domain.entity.SessionUserId
import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.entity.key.PublicAddress
import me.proton.core.key.domain.entity.key.PublicSignedKeyList
import me.proton.core.key.domain.repository.PublicAddressRepository
import me.proton.core.key.domain.repository.Source

class TestPublicAddressRepository : PublicAddressRepository {

    private var address: PublicAddress? = null

    fun setAddress(address: PublicAddress?) {
        this.address = address
    }

    override suspend fun clearAll() {}

    override suspend fun getPublicAddress(
        sessionUserId: SessionUserId,
        email: String,
        source: Source
    ): PublicAddress = address ?: throw IllegalStateException("address variable is not set")

    override suspend fun getSKLsAfterEpoch(
        userId: UserId,
        epochId: Int,
        email: String
    ): List<PublicSignedKeyList> {
        throw IllegalStateException("This method should not be called")
    }

    override suspend fun getSKLAtEpoch(
        userId: UserId,
        epochId: Int,
        email: String
    ): PublicSignedKeyList {
        throw IllegalStateException("This method should not be called")
    }
}
