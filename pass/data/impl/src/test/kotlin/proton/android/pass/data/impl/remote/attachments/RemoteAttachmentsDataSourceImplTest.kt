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

package proton.android.pass.data.impl.remote.attachments

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThrows
import org.junit.Test
import proton.android.pass.data.api.errors.ProtonCodeException
import proton.android.pass.data.impl.fakes.FakeRemoteApiGateway
import proton.android.pass.data.impl.network.RemoteApiException
import proton.android.pass.data.impl.network.RemoteResult
import proton.android.pass.domain.UserId

class RemoteAttachmentsDataSourceImplTest {

    private val gateway = FakeRemoteApiGateway()
    private val instance = RemoteAttachmentsDataSourceImpl(gateway)

    @Test
    fun `too many files code surfaces as a ProtonCodeException with the code`() = runTest {
        gateway.error = RemoteResult.Error.Http(HTTP_UNPROCESSABLE, TOO_MANY_FILES_CODE, "message")

        val thrown = assertThrows(RemoteApiException::class.java) { createPendingFile() }

        assertThat(thrown).isInstanceOf(ProtonCodeException::class.java)
        assertThat((thrown as ProtonCodeException).protonCode).isEqualTo(TOO_MANY_FILES_CODE)
    }

    @Test
    fun `non http errors surface a ProtonCodeException without a code`() = runTest {
        gateway.error = RemoteResult.Error.NoInternet()

        val thrown = assertThrows(RemoteApiException::class.java) { createPendingFile() }

        assertThat(thrown.protonCode).isNull()
    }

    private fun createPendingFile() = runBlocking {
        instance.createPendingFile(
            userId = UserId("user-id"),
            metadata = "metadata",
            chunkCount = 1,
            encryptionVersion = 1
        )
    }

    private companion object {
        const val TOO_MANY_FILES_CODE = 2028
        const val HTTP_UNPROCESSABLE = 422
    }
}
