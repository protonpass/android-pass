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
import proton.android.pass.data.api.errors.TooManyExtraPasswordAttemptsException
import proton.android.pass.data.api.errors.WrongExtraPasswordException
import proton.android.pass.data.impl.fakes.FakeRemoteApiGateway
import proton.android.pass.data.impl.network.RemoteApiException
import proton.android.pass.data.impl.network.RemoteResult
import proton.android.pass.data.impl.requests.ExtraPasswordSendSrpDataRequest
import proton.android.pass.domain.UserId

class RemoteExtraPasswordDataSourceImplTest {

    private val gateway = FakeRemoteApiGateway()
    private val instance = RemoteExtraPasswordDataSourceImpl(gateway)

    @Test
    fun `wrong password code maps to WrongExtraPasswordException`() = runTest {
        gateway.error = http(WRONG_PASSWORD)

        assertThrows(WrongExtraPasswordException::class.java) { send() }
    }

    @Test
    fun `too many attempts code maps to TooManyExtraPasswordAttemptsException`() = runTest {
        gateway.error = http(TOO_MANY_WRONG_ATTEMPTS)

        assertThrows(TooManyExtraPasswordAttemptsException::class.java) { send() }
    }

    @Test
    fun `other proton codes surface a RemoteApiException`() = runTest {
        gateway.error = http(OTHER_CODE)

        val thrown = assertThrows(RemoteApiException::class.java) { send() }

        assertThat(thrown.error).isEqualTo(gateway.error)
    }

    @Test
    fun `non http errors surface a RemoteApiException`() = runTest {
        gateway.error = RemoteResult.Error.NoInternet()

        val thrown = assertThrows(RemoteApiException::class.java) { send() }

        assertThat(thrown.error).isEqualTo(gateway.error)
    }

    private fun send() = runBlocking { instance.sendExtraPasswordAuthData(USER_ID, REQUEST) }

    private fun http(protonCode: Int) = RemoteResult.Error.Http(HTTP_UNPROCESSABLE, protonCode, "message")

    private companion object {
        private val USER_ID = UserId("user-id")
        private val REQUEST = ExtraPasswordSendSrpDataRequest(
            clientEphemeral = "ephemeral",
            clientProof = "proof",
            srpSessionId = "session"
        )
        private const val WRONG_PASSWORD = 2_011
        private const val TOO_MANY_WRONG_ATTEMPTS = 2_026
        private const val OTHER_CODE = 2_000
        private const val HTTP_UNPROCESSABLE = 422
    }
}
