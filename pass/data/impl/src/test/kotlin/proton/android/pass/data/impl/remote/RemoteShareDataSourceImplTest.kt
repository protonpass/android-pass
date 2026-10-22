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

package proton.android.pass.data.impl.remote

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThrows
import org.junit.Test
import proton.android.pass.data.api.errors.CannotCreateMoreVaultsError
import proton.android.pass.data.impl.fakes.FakeRemoteApiGateway
import proton.android.pass.data.impl.network.RemoteApiException
import proton.android.pass.data.impl.network.RemoteResult
import proton.android.pass.data.impl.requests.CreateVaultRequest
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.UserId

class RemoteShareDataSourceImplTest {

    private val gateway = FakeRemoteApiGateway()
    private val instance = RemoteShareDataSourceImpl(gateway)

    @Test
    fun `createVault maps the vault limit code to CannotCreateMoreVaultsError`() = runTest {
        gateway.error = http(CODE_CANNOT_CREATE_MORE_VAULTS)

        assertThrows(CannotCreateMoreVaultsError::class.java) {
            runBlocking { instance.createVault(USER_ID, CREATE_VAULT_REQUEST) }
        }
    }

    @Test
    fun `createVault rethrows the cause for other errors`() = runTest {
        val cause = IllegalStateException("boom")
        gateway.error = RemoteResult.Error.Http(HTTP_UNPROCESSABLE, OTHER_CODE, "message", cause)

        val thrown = assertThrows(IllegalStateException::class.java) {
            runBlocking { instance.createVault(USER_ID, CREATE_VAULT_REQUEST) }
        }

        assertThat(thrown).isSameInstanceAs(cause)
    }

    @Test
    fun `createVault throws a generic exception when there is no cause`() = runTest {
        gateway.error = http(OTHER_CODE)

        val thrown = assertThrows(Exception::class.java) {
            runBlocking { instance.createVault(USER_ID, CREATE_VAULT_REQUEST) }
        }

        assertThat(thrown).hasMessageThat().isEqualTo("Create vault failed")
    }

    @Test
    fun `value or throw calls surface a RemoteApiException with the proton code`() = runTest {
        gateway.error = http(OTHER_CODE)

        val thrown = assertThrows(RemoteApiException::class.java) {
            runBlocking { instance.deleteVault(USER_ID, ShareId("share-id")) }
        }

        assertThat(thrown.error).isEqualTo(gateway.error)
    }

    private fun http(protonCode: Int) = RemoteResult.Error.Http(HTTP_UNPROCESSABLE, protonCode, "message")

    private companion object {
        private val USER_ID = UserId("user-id")
        private val CREATE_VAULT_REQUEST = CreateVaultRequest(
            addressId = "address-id",
            content = "content",
            contentFormatVersion = 1,
            encryptedVaultKey = "key"
        )
        private const val CODE_CANNOT_CREATE_MORE_VAULTS = 300_007
        private const val OTHER_CODE = 2_011
        private const val HTTP_UNPROCESSABLE = 422
    }
}
