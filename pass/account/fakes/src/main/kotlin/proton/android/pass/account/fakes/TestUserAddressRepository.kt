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

package proton.android.pass.account.fakes

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import me.proton.core.domain.entity.SessionUserId
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.AddressId
import me.proton.core.user.domain.entity.UserAddress
import me.proton.core.user.domain.repository.UserAddressRepository
import javax.inject.Inject

class TestUserAddressRepository @Inject constructor() : UserAddressRepository {

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

    override suspend fun getAddresses(sessionUserId: SessionUserId, refresh: Boolean): List<UserAddress> = addresses

    override fun observeAddress(
        sessionUserId: SessionUserId,
        addressId: AddressId,
        refresh: Boolean
    ): Flow<UserAddress?> = flowOf(address)

    override fun observeAddresses(sessionUserId: SessionUserId, refresh: Boolean): Flow<List<UserAddress>> =
        flowOf(addresses)

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

    override suspend fun updateOrder(sessionUserId: SessionUserId, addressIds: List<AddressId>): List<UserAddress> =
        addresses

    fun generateAddress(
        displayName: String,
        userId: UserId = UserId("userid-123"),
        email: String = "test@email.local",
        addressId: AddressId = AddressId("addressid-123")
    ): UserAddress = UserAddress(
        userId = userId,
        addressId = addressId,
        email = email,
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
