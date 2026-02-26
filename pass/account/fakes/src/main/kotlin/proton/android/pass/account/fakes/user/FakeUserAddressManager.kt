/*
 * Copyright (c) 2026 Proton AG
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
