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
import proton.android.pass.data.api.errors.DisplayableApiError
import proton.android.pass.data.api.errors.InvalidContentFormatVersionError
import proton.android.pass.data.api.errors.ItemNewerRevisionAvailable
import proton.android.pass.data.impl.fakes.FakeRemoteApiGateway
import proton.android.pass.data.impl.network.RemoteApiException
import proton.android.pass.data.impl.network.RemoteResult
import proton.android.pass.data.impl.requests.MigrateItemsRequest
import proton.android.pass.data.impl.requests.UpdateItemRequest
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.UserId

class RemoteItemDataSourceImplTest {

    private val gateway = FakeRemoteApiGateway()
    private val instance = RemoteItemDataSourceImpl(gateway)

    @Test
    fun `updateItem maps the invalid content code`() = runTest {
        gateway.error = http(CODE_INVALID_CONTENT)

        assertThrows(InvalidContentFormatVersionError::class.java) {
            runBlocking { instance.updateItem(USER_ID, SHARE_ID, ITEM_ID, UPDATE_ITEM_REQUEST) }
        }
    }

    @Test
    fun `updateItem maps the not latest revision code`() = runTest {
        gateway.error = http(CODE_NOT_LATEST_REVISION)

        assertThrows(ItemNewerRevisionAvailable::class.java) {
            runBlocking { instance.updateItem(USER_ID, SHARE_ID, ITEM_ID, UPDATE_ITEM_REQUEST) }
        }
    }

    @Test
    fun `updateItem throws a generic exception for unmapped codes without cause`() = runTest {
        gateway.error = http(OTHER_CODE)

        val thrown = assertThrows(Exception::class.java) {
            runBlocking { instance.updateItem(USER_ID, SHARE_ID, ITEM_ID, UPDATE_ITEM_REQUEST) }
        }

        assertThat(thrown).hasMessageThat().isEqualTo("Update item failed")
    }

    @Test
    fun `migrateItems exposes the proton message as a displayable error`() = runTest {
        val cause = IllegalStateException("boom")
        gateway.error = RemoteResult.Error.Http(HTTP_UNPROCESSABLE, OTHER_CODE, "proton message", cause)

        val thrown = assertThrows(DisplayableApiError::class.java) {
            runBlocking { instance.migrateItems(USER_ID, SHARE_ID, MIGRATE_ITEMS_REQUEST) }
        }

        assertThat(thrown).hasMessageThat().isEqualTo("proton message")
        assertThat(thrown).hasCauseThat().isSameInstanceAs(cause)
    }

    @Test
    fun `migrateItems without proton data throws the generic exception`() = runTest {
        gateway.error = RemoteResult.Error.Http(HTTP_SERVER_ERROR, null, "http message")

        val thrown = assertThrows(Exception::class.java) {
            runBlocking { instance.migrateItems(USER_ID, SHARE_ID, MIGRATE_ITEMS_REQUEST) }
        }

        assertThat(thrown).isNotInstanceOf(DisplayableApiError::class.java)
        assertThat(thrown).hasMessageThat().isEqualTo("Migrate items failed")
    }

    @Test
    fun `value or throw calls surface a RemoteApiException`() = runTest {
        gateway.error = RemoteResult.Error.NoInternet()

        val thrown = assertThrows(RemoteApiException::class.java) {
            runBlocking { instance.getItem(USER_ID, SHARE_ID, ITEM_ID) }
        }

        assertThat(thrown.error).isEqualTo(gateway.error)
    }

    private fun http(protonCode: Int) = RemoteResult.Error.Http(HTTP_UNPROCESSABLE, protonCode, "message")

    private companion object {
        private val USER_ID = UserId("user-id")
        private val SHARE_ID = ShareId("share-id")
        private val ITEM_ID = ItemId("item-id")
        private val UPDATE_ITEM_REQUEST = UpdateItemRequest(
            keyRotation = 1,
            lastRevision = 1,
            contentFormatVersion = 1,
            content = "content"
        )
        private val MIGRATE_ITEMS_REQUEST = MigrateItemsRequest(shareId = "share-id", items = emptyList())
        private const val OTHER_CODE = 2_500
        private const val HTTP_UNPROCESSABLE = 422
        private const val HTTP_SERVER_ERROR = 500
    }
}
