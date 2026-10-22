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

package proton.android.pass.data.impl.crypto

import kotlinx.coroutines.test.runTest
import org.junit.Test
import proton.android.pass.account.fakes.FakeUserAddressRepository
import proton.android.pass.data.impl.fakes.FakeShareRepository
import proton.android.pass.domain.AddressId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.UserId
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertSame

class GetUserAddressForShareImplTest {

    private val userId = UserId("user-id")
    private val shareId = ShareId("share-id")
    private val addressId = AddressId("address-id")

    private val shareRepository = FakeShareRepository().apply {
        setGetAddressIdForShareIdResult(Result.success(addressId))
    }
    private val userAddressRepository = FakeUserAddressRepository()
    private val instance = GetUserAddressForShareImpl(
        shareRepository = shareRepository,
        userAddressRepository = userAddressRepository
    )

    @Test
    fun `returns the stored address when it is found by id`() = runTest {
        val stored = userAddressRepository.generateAddress("stored", userId, addressId = addressId)
        userAddressRepository.setAddress(stored)

        val result = instance(userId, shareId)

        assertSame(stored, result)
        assertEquals(addressId, userAddressRepository.lastGetAddressId)
        assertNull(userAddressRepository.lastGetAddressesRefresh)
    }

    @Test
    fun `falls back to the refreshed addresses when the stored lookup is empty`() = runTest {
        val other = userAddressRepository.generateAddress("other", userId, addressId = AddressId("other-id"))
        val expected = userAddressRepository.generateAddress("expected", userId, addressId = addressId)
        userAddressRepository.setAddress(null)
        userAddressRepository.setAddresses(listOf(other, expected))

        val result = instance(userId, shareId)

        assertSame(expected, result)
        assertEquals(addressId, userAddressRepository.lastGetAddressId)
        assertEquals(true, userAddressRepository.lastGetAddressesRefresh)
    }

    @Test
    fun `fails when no address matches the share address id`() = runTest {
        val other = userAddressRepository.generateAddress("other", userId, addressId = AddressId("other-id"))
        userAddressRepository.setAddress(null)
        userAddressRepository.setAddresses(listOf(other))

        val error = assertFailsWith<IllegalStateException> { instance(userId, shareId) }

        assertEquals("Could not find address for share ${shareId.id}", error.message)
    }

    @Test
    fun `propagates the failure when the share address id is not available`() = runTest {
        val failure = IllegalStateException("share not available")
        shareRepository.setGetAddressIdForShareIdResult(Result.failure(failure))

        val error = assertFailsWith<IllegalStateException> { instance(userId, shareId) }

        assertSame(failure, error)
    }
}
