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

package proton.android.pass.crypto.impl.usecases

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import me.proton.core.key.domain.entity.key.PublicAddressKey
import me.proton.core.key.domain.entity.key.PublicKey
import org.junit.Before
import org.junit.Test
import proton.android.pass.account.fakes.FakePublicAddressRepository
import proton.android.pass.account.fakes.FakeUserAddressRepository
import proton.android.pass.crypto.fakes.context.FakeCryptoContext
import proton.android.pass.domain.AddressId
import proton.android.pass.domain.UserId

class GetGroupKeyDecryptorImplTest {

    private lateinit var userAddressRepository: FakeUserAddressRepository
    private lateinit var publicAddressRepository: FakePublicAddressRepository
    private lateinit var instance: GetGroupKeyDecryptorImpl

    @Before
    fun setup() {
        userAddressRepository = FakeUserAddressRepository()
        publicAddressRepository = FakePublicAddressRepository()
        instance = GetGroupKeyDecryptorImpl(
            cryptoContext = FakeCryptoContext(),
            userAddressRepository = userAddressRepository,
            publicAddressRepository = publicAddressRepository
        )
    }

    @Test
    fun `returns a decryptor when the address is stored and the group keys are fetched`() = runTest {
        userAddressRepository.setAddress(userAddressRepository.generateAddress("invited"))
        publicAddressRepository.setAddressInfo(GROUP_EMAIL, listOf(groupPublicKey()))

        val result = instance(USER_ID, ADDRESS_ID, GROUP_EMAIL)

        assertThat(result).isNotNull()
        assertThat(userAddressRepository.lastGetAddressId).isEqualTo(ADDRESS_ID)
        assertThat(publicAddressRepository.infoRequestedEmails).containsExactly(GROUP_EMAIL)
    }

    @Test
    fun `fails when the invited address is not stored locally`() = runTest {
        userAddressRepository.setAddress(null)

        val result = runCatching { instance(USER_ID, ADDRESS_ID, GROUP_EMAIL) }

        assertThat(result.exceptionOrNull()).isInstanceOf(IllegalStateException::class.java)
        assertThat(result.exceptionOrNull()?.message).isEqualTo("Invited address not found")
        assertThat(publicAddressRepository.infoRequestedEmails).isEmpty()
    }

    @Test
    fun `rethrows a failed public key fetch`() = runTest {
        userAddressRepository.setAddress(userAddressRepository.generateAddress("invited"))
        publicAddressRepository.addressInfoFailure = IllegalStateException("network down")

        val result = runCatching { instance(USER_ID, ADDRESS_ID, GROUP_EMAIL) }

        assertThat(result.exceptionOrNull()).isInstanceOf(IllegalStateException::class.java)
        assertThat(result.exceptionOrNull()?.message).isEqualTo("network down")
    }

    private fun groupPublicKey() = PublicAddressKey(
        email = GROUP_EMAIL,
        flags = 0,
        publicKey = PublicKey(
            key = "GroupKey",
            isPrimary = true,
            isActive = true,
            canEncrypt = true,
            canVerify = true
        )
    )

    private companion object {
        val USER_ID = UserId("user-id")
        val ADDRESS_ID = AddressId("address-id")
        const val GROUP_EMAIL = "group@example.com"
    }
}
