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
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.domain.entity.UserId
import org.junit.Test
import proton.android.pass.data.api.crypto.GetShareAndItemKey
import proton.android.pass.data.fakes.usecases.FakeGetAllKeysByAddress
import proton.android.pass.data.impl.fakes.FakeFolderKeyRepository
import proton.android.pass.domain.FolderId
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.key.FolderKey
import proton.android.pass.domain.key.ItemKey
import proton.android.pass.domain.key.ShareKey
import proton.android.pass.test.domain.ShareKeyTestFactory
import proton.android.pass.test.domain.UserAddressTestFactory
import proton.android.pass.crypto.fakes.usecases.FakeEncryptInviteKeys
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class EncryptItemsKeysForUserImplTest {

    private val userAddress = UserAddressTestFactory.create(userId = UserId("user-id"))
    private val shareId = ShareId("share-id")
    private val itemId = ItemId("item-id")
    private val getAllKeysByAddress = FakeGetAllKeysByAddress()
    private val encryptInviteKeys = FakeEncryptInviteKeys()
    private val folderKeyRepository = FakeFolderKeyRepository()
    private val getShareAndItemKey = RecordingGetShareAndItemKey()

    @Test
    fun `invoke passes folder key override when item belongs to folder`() = runTest {
        val folderKey = FolderKey(
            rotation = 12L,
            key = EncryptedByteArray(byteArrayOf(1, 2, 3)),
            responseKey = "folder-response"
        )
        val folderId = FolderId("folder-id")
        folderKeyRepository.setGetFolderKeyResult(Result.success(folderKey))

        val result = instance().invoke(
            shareId = shareId,
            itemId = itemId,
            folderId = folderId,
            userAddress = userAddress,
            targetEmail = "target@test.local"
        )

        assertTrue(result.isFailure)
        assertSame(folderKey, getShareAndItemKey.lastDecryptionKeyOverride)
        assertEquals(1, getShareAndItemKey.callCount)
    }

    @Test
    fun `invoke uses null override when item is not in folder`() = runTest {
        folderKeyRepository.setGetFolderKeyResult(Result.success(null))

        val result = instance().invoke(
            shareId = shareId,
            itemId = itemId,
            folderId = null,
            userAddress = userAddress,
            targetEmail = "target@test.local"
        )

        assertTrue(result.isFailure)
        assertNull(getShareAndItemKey.lastDecryptionKeyOverride)
        assertEquals(1, getShareAndItemKey.callCount)
    }

    @Test
    fun `invoke fails when item is in folder but folder key is missing`() = runTest {
        val folderId = FolderId("folder-id")
        folderKeyRepository.setGetFolderKeyResult(Result.success(null))

        val result = instance().invoke(
            shareId = shareId,
            itemId = itemId,
            folderId = folderId,
            userAddress = userAddress,
            targetEmail = "target@test.local"
        )

        assertTrue(result.isFailure)
        assertEquals(0, getShareAndItemKey.callCount)
        val error = result.exceptionOrNull()
        assertIs<IllegalStateException>(error)
        assertEquals("Folder key not found for folderId=folder-id", error.message)
    }

    private fun instance() = EncryptItemsKeysForUserImpl(
        getAllKeysByAddress = getAllKeysByAddress,
        encryptInviteKeys = encryptInviteKeys,
        getShareAndItemKey = getShareAndItemKey,
        folderKeyRepository = folderKeyRepository
    )

    private class RecordingGetShareAndItemKey : GetShareAndItemKey {
        var callCount: Int = 0
            private set
        var lastDecryptionKeyOverride: FolderKey? = null
            private set

        private val response: Pair<ShareKey, ItemKey> = ShareKeyTestFactory.createPrivate() to ItemKey(
            rotation = 1L,
            key = EncryptedByteArray(byteArrayOf(7, 7, 7)),
            responseKey = "item-response"
        )

        override suspend fun invoke(
            userAddress: me.proton.core.user.domain.entity.UserAddress,
            shareId: ShareId,
            itemId: ItemId,
            decryptionKeyOverride: FolderKey?
        ): Pair<ShareKey, ItemKey> {
            callCount++
            lastDecryptionKeyOverride = decryptionKeyOverride
            return response
        }
    }

}
