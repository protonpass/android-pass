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
import proton.android.pass.data.api.errors.CustomEmailDoesNotExistException
import proton.android.pass.data.api.errors.InvalidVerificationCodeError
import proton.android.pass.data.impl.fakes.FakeRemoteApiGateway
import proton.android.pass.data.impl.network.RemoteApiException
import proton.android.pass.data.impl.network.RemoteResult
import proton.android.pass.domain.UserId
import proton.android.pass.domain.breach.CustomEmailId

class RemoteBreachDataSourceImplTest {

    private val gateway = FakeRemoteApiGateway()
    private val instance = RemoteBreachDataSourceImpl(gateway)

    @Test
    fun `invalid value code maps to InvalidVerificationCodeError`() = runTest {
        gateway.error = http(INVALID_VALUE)

        assertThrows(InvalidVerificationCodeError::class.java) { verify() }
    }

    @Test
    fun `not allowed code maps to CustomEmailDoesNotExistException`() = runTest {
        gateway.error = http(NOT_ALLOWED)

        assertThrows(CustomEmailDoesNotExistException::class.java) { verify() }
    }

    @Test
    fun `other proton codes surface a RemoteApiException`() = runTest {
        gateway.error = http(OTHER_CODE)

        val thrown = assertThrows(RemoteApiException::class.java) { verify() }

        assertThat(thrown.error).isEqualTo(gateway.error)
    }

    @Test
    fun `non http errors surface a RemoteApiException`() = runTest {
        gateway.error = RemoteResult.Error.Failure()

        val thrown = assertThrows(RemoteApiException::class.java) { verify() }

        assertThat(thrown.error).isEqualTo(gateway.error)
    }

    private fun verify() = runBlocking { instance.verifyCustomEmail(USER_ID, CustomEmailId("email-id"), "123456") }

    private fun http(protonCode: Int) = RemoteResult.Error.Http(HTTP_UNPROCESSABLE, protonCode, "message")

    private companion object {
        private val USER_ID = UserId("user-id")
        private const val INVALID_VALUE = 2_001
        private const val NOT_ALLOWED = 2_011
        private const val OTHER_CODE = 2_000
        private const val HTTP_UNPROCESSABLE = 422
    }
}
