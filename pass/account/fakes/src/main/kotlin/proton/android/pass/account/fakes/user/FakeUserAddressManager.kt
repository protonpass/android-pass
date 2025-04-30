package proton.android.pass.account.fakes.user

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.SessionUserId
import me.proton.core.user.domain.UserAddressManager
import me.proton.core.user.domain.entity.AddressId
import me.proton.core.user.domain.entity.UserAddress
import javax.inject.Inject

@Suppress("NotImplementedDeclaration")
class FakeUserAddressManager @Inject constructor() : UserAddressManager {
    override fun observeAddresses(sessionUserId: SessionUserId, refresh: Boolean): Flow<List<UserAddress>> {
        TODO("Not yet implemented")
    }

    override suspend fun getAddresses(sessionUserId: SessionUserId, refresh: Boolean): List<UserAddress> {
        TODO("Not yet implemented")
    }

    override suspend fun getAddress(
        sessionUserId: SessionUserId,
        addressId: AddressId,
        refresh: Boolean
    ): UserAddress? {
        TODO("Not yet implemented")
    }

    override suspend fun createAddressKey(
        sessionUserId: SessionUserId,
        addressId: AddressId,
        isPrimary: Boolean
    ): UserAddress {
        TODO("Not yet implemented")
    }

    override suspend fun setupInternalAddress(
        sessionUserId: SessionUserId,
        displayName: String,
        domain: String
    ): UserAddress {
        TODO("Not yet implemented")
    }

    override suspend fun updateAddress(
        sessionUserId: SessionUserId,
        addressId: AddressId,
        displayName: String?,
        signature: String?
    ): UserAddress {
        TODO("Not yet implemented")
    }

    override suspend fun updateOrder(sessionUserId: SessionUserId, addressIds: List<AddressId>): List<UserAddress> {
        TODO("Not yet implemented")
    }
}
