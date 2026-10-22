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

package proton.android.pass.data.impl.repositories

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import proton.android.pass.crypto.fakes.context.FakeEncryptionContextProvider
import proton.android.pass.data.impl.fakes.FakeGetItemKey
import proton.android.pass.data.impl.fakes.FakeLocalItemDataSource
import proton.android.pass.data.impl.fakes.FakeLocalShareKeyDataSource
import proton.android.pass.data.impl.fakes.FakeRemoteApiGateway
import proton.android.pass.data.impl.fakes.FakeSecureLinksLocalDataSource
import proton.android.pass.data.impl.network.RemoteApiException
import proton.android.pass.data.impl.network.RemoteResult
import proton.android.pass.data.impl.remote.RemoteSecureLinkDataSourceImpl
import proton.android.pass.domain.UserId
import proton.android.pass.domain.securelinks.SecureLinkId

class SecureLinkRepositoryImplTest {

    private lateinit var gateway: FakeRemoteApiGateway
    private lateinit var secureLinksLocal: FakeSecureLinksLocalDataSource
    private lateinit var instance: SecureLinkRepositoryImpl

    @Before
    fun setup() {
        gateway = FakeRemoteApiGateway()
        secureLinksLocal = FakeSecureLinksLocalDataSource()
        instance = SecureLinkRepositoryImpl(
            localItemDataSource = FakeLocalItemDataSource(),
            localShareKeyDataSource = FakeLocalShareKeyDataSource(),
            remoteSecureLinkDataSource = RemoteSecureLinkDataSourceImpl(gateway),
            secureLinksLocalDataSource = secureLinksLocal,
            encryptionContextProvider = FakeEncryptionContextProvider(),
            getItemKey = FakeGetItemKey()
        )
    }

    @Test
    fun `deleteSecureLink swallows the secure link not found code and deletes locally`() = runTest {
        gateway.error = http(SECURE_LINK_NOT_FOUND_CODE)

        instance.deleteSecureLink(USER_ID, SECURE_LINK_ID)

        assertThat(secureLinksLocal.deleted).containsExactly(SECURE_LINK_ID)
    }

    @Test
    fun `deleteSecureLink rethrows other proton codes and keeps the local link`() = runTest {
        gateway.error = http(OTHER_CODE)

        val thrown = assertThrows(RemoteApiException::class.java) {
            runBlocking { instance.deleteSecureLink(USER_ID, SECURE_LINK_ID) }
        }

        assertThat(thrown.protonCode).isEqualTo(OTHER_CODE)
        assertThat(secureLinksLocal.deleted).isEmpty()
    }

    @Test
    fun `deleteSecureLink rethrows non http errors and keeps the local link`() = runTest {
        gateway.error = RemoteResult.Error.NoInternet()

        assertThrows(RemoteApiException::class.java) {
            runBlocking { instance.deleteSecureLink(USER_ID, SECURE_LINK_ID) }
        }

        assertThat(secureLinksLocal.deleted).isEmpty()
    }

    private fun http(protonCode: Int) = RemoteResult.Error.Http(HTTP_UNPROCESSABLE, protonCode, "message")

    private companion object {
        val USER_ID = UserId("user-id")
        val SECURE_LINK_ID = SecureLinkId("secure-link-id")
        const val SECURE_LINK_NOT_FOUND_CODE = 2001
        const val OTHER_CODE = 2002
        const val HTTP_UNPROCESSABLE = 422
    }
}
