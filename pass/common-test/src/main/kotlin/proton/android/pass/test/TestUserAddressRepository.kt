package proton.android.pass.test

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import me.proton.core.domain.arch.DataResult
import me.proton.core.domain.arch.ResponseSource
import me.proton.core.domain.entity.SessionUserId
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.AddressId
import me.proton.core.user.domain.entity.UserAddress
import me.proton.core.user.domain.repository.UserAddressRepository

class TestUserAddressRepository : UserAddressRepository {

    private var address: UserAddress? = null
    private var addresses: List<UserAddress> = emptyList()

    // Test methods

    fun setAddress(address: UserAddress?) {
        this.address = address
    }

    fun setAddresses(addresses: List<UserAddress>) {
        this.addresses = addresses
    }
    // End of test methods

    override suspend fun addAddresses(addresses: List<UserAddress>) {}

    override suspend fun createAddress(
        sessionUserId: SessionUserId,
        displayName: String,
        domain: String
    ): UserAddress = generateAddress(displayName, sessionUserId)


    override suspend fun deleteAddresses(addressIds: List<AddressId>) {}

    override suspend fun deleteAllAddresses(userId: UserId) {}

    override suspend fun getAddress(
        sessionUserId: SessionUserId,
        addressId: AddressId,
        refresh: Boolean
    ): UserAddress? = address

    override suspend fun getAddresses(
        sessionUserId: SessionUserId,
        refresh: Boolean
    ): List<UserAddress> = addresses

    override fun getAddressesFlow(
        sessionUserId: SessionUserId,
        refresh: Boolean
    ): Flow<DataResult<List<UserAddress>>> =
        flowOf(DataResult.Success(ResponseSource.Local, addresses))

    override fun observeAddress(
        sessionUserId: SessionUserId,
        addressId: AddressId,
        refresh: Boolean
    ): Flow<UserAddress?> = flowOf(address)

    override fun observeAddresses(
        sessionUserId: SessionUserId,
        refresh: Boolean
    ): Flow<List<UserAddress>> = flowOf(addresses)

    override suspend fun updateAddress(
        sessionUserId: SessionUserId,
        addressId: AddressId,
        displayName: String?,
        signature: String?
    ): UserAddress {
        val userAddress = address
        if (userAddress != null) {
            return userAddress
        }
        return generateAddress("test")
    }

    override suspend fun updateAddresses(addresses: List<UserAddress>) {}

    override suspend fun updateOrder(
        sessionUserId: SessionUserId,
        addressIds: List<AddressId>
    ): List<UserAddress> = addresses

    fun generateAddress(
        displayName: String,
        userId: UserId = UserId("userid-123")
    ): UserAddress =
        UserAddress(
            userId = userId,
            addressId = AddressId("addressid-123"),
            email = "test@email.local",
            displayName = displayName,
            signature = null,
            domainId = null,
            canSend = true,
            canReceive = true,
            enabled = true,
            type = null,
            order = 1,
            keys = emptyList(),
            signedKeyList = null
        )

}
