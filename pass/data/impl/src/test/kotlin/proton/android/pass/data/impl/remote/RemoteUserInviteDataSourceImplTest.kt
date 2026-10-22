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
import proton.android.pass.data.impl.requests.invites.AcceptInviteRequest
import proton.android.pass.domain.InviteToken
import proton.android.pass.domain.UserId

class RemoteUserInviteDataSourceImplTest {

    private val gateway = FakeRemoteApiGateway()
    private val instance = RemoteUserInviteDataSourceImpl(gateway)

    @Test
    fun `acceptInvite maps the cannot create more vaults code`() = runTest {
        gateway.error = RemoteResult.Error.Http(HTTP_UNPROCESSABLE, CANNOT_CREATE_MORE_VAULTS_CODE, "message")

        assertThrows(CannotCreateMoreVaultsError::class.java) { acceptInvite() }
    }

    @Test
    fun `acceptInvite rethrows other proton codes as RemoteApiException`() = runTest {
        gateway.error = RemoteResult.Error.Http(HTTP_UNPROCESSABLE, OTHER_CODE, "message")

        val thrown = assertThrows(RemoteApiException::class.java) { acceptInvite() }

        assertThat(thrown.protonCode).isEqualTo(OTHER_CODE)
    }

    @Test
    fun `acceptInvite rethrows non http errors as retryable RemoteApiException`() = runTest {
        gateway.error = RemoteResult.Error.NoInternet()

        val thrown = assertThrows(RemoteApiException::class.java) { acceptInvite() }

        assertThat(thrown.protonCode).isNull()
        assertThat(thrown.isRetryable).isTrue()
    }

    private fun acceptInvite() = runBlocking {
        instance.acceptInvite(
            userId = UserId("user-id"),
            inviteToken = InviteToken("token"),
            body = AcceptInviteRequest(keys = emptyList())
        )
    }

    private companion object {
        const val CANNOT_CREATE_MORE_VAULTS_CODE = 300_007
        const val OTHER_CODE = 2002
        const val HTTP_UNPROCESSABLE = 422
    }
}
