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
import me.proton.core.user.domain.entity.AddressId
import org.junit.Test
import proton.android.pass.data.impl.fakes.FakeItemKeyRepository
import proton.android.pass.data.impl.fakes.FakeShareRepository
import proton.android.pass.data.impl.repositories.ItemKeyRepository
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.key.FolderKey
import proton.android.pass.domain.key.ItemKey
import proton.android.pass.test.domain.ShareKeyTestFactory
import proton.android.pass.test.domain.ShareTestFactory
import proton.android.pass.test.domain.UserAddressTestFactory
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GetShareAndItemKeyImplTest {

    private val userAddress = UserAddressTestFactory.create(
        userId = UserId("user-id"),
        addressId = AddressId("address-id")
    )
    private val shareId = ShareId("share-id")
    private val itemId = ItemId("item-id")

    @Test
    fun `invoke returns latest share key mapped as item key for item share`() = runTest {
        val share = ShareTestFactory.Item.create(id = shareId.id, userId = userAddress.userId.id)
        val shareKey = ShareKeyTestFactory.createPrivate()
        val itemKey = ItemKey(shareKey.rotation, shareKey.key, shareKey.responseKey)
        val expected = shareKey to itemKey
        val shareRepository = FakeShareRepository().apply {
            setGetByIdResult(shareId, Result.success(share))
        }
        val itemKeyRepository = FakeItemKeyRepository().apply {
            emitGetLatestShareAndItemKey(expected)
        }

        val result = GetShareAndItemKeyImpl(
            shareRepository = shareRepository,
            itemKeyRepository = itemKeyRepository
        ).invoke(userAddress, shareId, itemId, decryptionKeyOverride = null)

        assertEquals(expected, result)
        assertEquals(1, itemKeyRepository.getLatestShareAndItemKeyCallCount)
        assertEquals(
            ItemKeyRepository.Scope.SharedItem,
            itemKeyRepository.lastGetLatestShareAndItemKeyScope
        )
    }

    @Test
    fun `invoke ignores folder override for item share and still uses shared item scope`() = runTest {
        val share = ShareTestFactory.Item.create(id = shareId.id, userId = userAddress.userId.id)
        val shareKey = ShareKeyTestFactory.createPrivate()
        val itemKey = ItemKey(shareKey.rotation, shareKey.key, shareKey.responseKey)
        val expected = shareKey to itemKey
        val folderKey = FolderKey(
            rotation = 8L,
            key = EncryptedByteArray(byteArrayOf(4, 4, 4)),
            responseKey = "folder-response"
        )
        val shareRepository = FakeShareRepository().apply {
            setGetByIdResult(shareId, Result.success(share))
        }
        val itemKeyRepository = FakeItemKeyRepository().apply {
            emitGetLatestShareAndItemKey(expected)
        }

        val result = GetShareAndItemKeyImpl(
            shareRepository = shareRepository,
            itemKeyRepository = itemKeyRepository
        ).invoke(userAddress, shareId, itemId, decryptionKeyOverride = folderKey)

        assertEquals(expected, result)
        assertEquals(1, itemKeyRepository.getLatestShareAndItemKeyCallCount)
        assertEquals(
            ItemKeyRepository.Scope.SharedItem,
            itemKeyRepository.lastGetLatestShareAndItemKeyScope
        )
    }

    @Test
    fun `invoke delegates to getLatestShareAndItemKey for vault share`() = runTest {
        val share = ShareTestFactory.Vault.create(id = shareId.id, userId = userAddress.userId.id)
        val shareKey = ShareKeyTestFactory.createPrivate()
        val itemKey = ItemKey(5L, EncryptedByteArray(byteArrayOf(1, 2)), "item-response")
        val expected = shareKey to itemKey
        val shareRepository = FakeShareRepository().apply {
            setGetByIdResult(shareId, Result.success(share))
        }
        val itemKeyRepository = FakeItemKeyRepository().apply {
            emitGetLatestShareAndItemKey(expected)
        }

        val result = GetShareAndItemKeyImpl(
            shareRepository = shareRepository,
            itemKeyRepository = itemKeyRepository
        ).invoke(userAddress, shareId, itemId, decryptionKeyOverride = null)

        assertEquals(expected, result)
        assertEquals(1, itemKeyRepository.getLatestShareAndItemKeyCallCount)
        assertEquals(0, itemKeyRepository.getLatestItemKeyCallCount)
        assertEquals(
            ItemKeyRepository.Scope.SharedVault(
                groupEmail = share.groupEmail,
                decryptionSource = ItemKeyRepository.VaultDecryptionSource.Share
            ),
            itemKeyRepository.lastGetLatestShareAndItemKeyScope
        )
    }

    @Test
    fun `invoke maps folder override to vault folder decryption source`() = runTest {
        val share = ShareTestFactory.Vault.create(id = shareId.id, userId = userAddress.userId.id)
        val shareKey = ShareKeyTestFactory.createPrivate()
        val itemKey = ItemKey(5L, EncryptedByteArray(byteArrayOf(1, 2)), "item-response")
        val expected = shareKey to itemKey
        val folderKey = FolderKey(
            rotation = 8L,
            key = EncryptedByteArray(byteArrayOf(4, 4, 4)),
            responseKey = "folder-response"
        )
        val shareRepository = FakeShareRepository().apply {
            setGetByIdResult(shareId, Result.success(share))
        }
        val itemKeyRepository = FakeItemKeyRepository().apply {
            emitGetLatestShareAndItemKey(expected)
        }

        GetShareAndItemKeyImpl(
            shareRepository = shareRepository,
            itemKeyRepository = itemKeyRepository
        ).invoke(userAddress, shareId, itemId, decryptionKeyOverride = folderKey)

        assertEquals(
            ItemKeyRepository.Scope.SharedVault(
                groupEmail = share.groupEmail,
                decryptionSource = ItemKeyRepository.VaultDecryptionSource.Folder(folderKey)
            ),
            itemKeyRepository.lastGetLatestShareAndItemKeyScope
        )
    }

    @Test
    fun `invoke throws when vault override is not a folder key`() = runTest {
        val share = ShareTestFactory.Vault.create(id = shareId.id, userId = userAddress.userId.id)
        val shareRepository = FakeShareRepository().apply {
            setGetByIdResult(shareId, Result.success(share))
        }
        val itemKeyRepository = FakeItemKeyRepository().apply {
            emitGetLatestShareAndItemKey(
                ShareKeyTestFactory.createPrivate() to ItemKey(
                    rotation = 1L,
                    key = EncryptedByteArray(byteArrayOf(1)),
                    responseKey = "unused"
                )
            )
        }

        assertFailsWith<IllegalArgumentException> {
            GetShareAndItemKeyImpl(
                shareRepository = shareRepository,
                itemKeyRepository = itemKeyRepository
            ).invoke(
                userAddress = userAddress,
                shareId = shareId,
                itemId = itemId,
                decryptionKeyOverride = ShareKeyTestFactory.createPrivate()
            )
        }
    }

}
