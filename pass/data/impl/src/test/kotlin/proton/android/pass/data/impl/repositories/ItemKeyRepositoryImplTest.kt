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

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.AddressId
import org.junit.Test
import proton.android.pass.crypto.api.error.KeyNotFound
import proton.android.pass.crypto.fakes.usecases.FakeOpenItemKey
import proton.android.pass.data.impl.fakes.FakeRemoteItemKeyDataSource
import proton.android.pass.data.impl.fakes.FakeShareKeyRepository
import proton.android.pass.data.impl.responses.ItemLatestKeyResponse
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.key.FolderKey
import proton.android.pass.domain.key.ItemKey
import proton.android.pass.test.domain.ShareKeyTestFactory
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class ItemKeyRepositoryImplTest {

    private val userId = UserId("user-id")
    private val addressId = AddressId("address-id")
    private val shareId = ShareId("share-id")
    private val itemId = ItemId("item-id")
    private val remoteResponse = ItemLatestKeyResponse(
        keyRotation = 12L,
        key = "remote-item-key"
    )

    @Test
    fun `getLatestItemKey does not fetch share key when override key is provided`() = runTest {
        val expectedItemKey = ItemKey(12L, EncryptedByteArray(byteArrayOf(1, 2, 3)), "response")
        val overrideKey = FolderKey(
            rotation = 34L,
            key = EncryptedByteArray(byteArrayOf(9, 8, 7)),
            responseKey = "folder-response"
        )
        val shareKeyRepository = FakeShareKeyRepository()
        val openItemKey = FakeOpenItemKey().apply { setResult(expectedItemKey) }
        val remoteDataSource = FakeRemoteItemKeyDataSource().apply {
            setFetchLatestItemKeyResponse(remoteResponse)
        }
        val repository = ItemKeyRepositoryImpl(
            shareKeyRepository = shareKeyRepository,
            remoteItemKeyRepository = remoteDataSource,
            openItemKey = openItemKey
        )

        val result = repository.getLatestItemKey(
            userId = userId,
            addressId = addressId,
            shareId = shareId,
            itemId = itemId,
            scope = ItemKeyRepository.Scope.SharedVault(
                groupEmail = "group@email.test",
                decryptionSource = ItemKeyRepository.VaultDecryptionSource.Folder(overrideKey)
            )
        ).first()

        assertEquals(0, shareKeyRepository.getShareKeyForRotationCallCount)
        assertSame(overrideKey, openItemKey.lastInviteKey)
        assertEquals(expectedItemKey, result)
    }

    @Test
    fun `getLatestItemKey fetches share key when override key is null`() = runTest {
        val expectedItemKey = ItemKey(12L, EncryptedByteArray(byteArrayOf(4, 5, 6)), "response")
        val shareKey = ShareKeyTestFactory.createPrivate()
        val shareKeyRepository = FakeShareKeyRepository().apply {
            emitGetShareKeyForRotation(shareKey)
        }
        val openItemKey = FakeOpenItemKey().apply { setResult(expectedItemKey) }
        val remoteDataSource = FakeRemoteItemKeyDataSource().apply {
            setFetchLatestItemKeyResponse(remoteResponse)
        }
        val repository = ItemKeyRepositoryImpl(
            shareKeyRepository = shareKeyRepository,
            remoteItemKeyRepository = remoteDataSource,
            openItemKey = openItemKey
        )

        val result = repository.getLatestItemKey(
            userId = userId,
            addressId = addressId,
            shareId = shareId,
            itemId = itemId,
            scope = ItemKeyRepository.Scope.SharedVault(
                groupEmail = null,
                decryptionSource = ItemKeyRepository.VaultDecryptionSource.Share
            )
        ).first()

        assertEquals(1, shareKeyRepository.getShareKeyForRotationCallCount)
        assertSame(shareKey, openItemKey.lastInviteKey)
        assertEquals(expectedItemKey, result)
    }

    @Test
    fun `getLatestItemKey throws when share key is missing and override key is null`() = runTest {
        val shareKeyRepository = FakeShareKeyRepository().apply {
            emitGetShareKeyForRotation(null)
        }
        val repository = ItemKeyRepositoryImpl(
            shareKeyRepository = shareKeyRepository,
            remoteItemKeyRepository = FakeRemoteItemKeyDataSource().apply {
                setFetchLatestItemKeyResponse(remoteResponse)
            },
            openItemKey = FakeOpenItemKey().apply {
                setResult(ItemKey(1L, EncryptedByteArray(byteArrayOf(0)), "unused"))
            }
        )

        assertFailsWith<KeyNotFound> {
            repository.getLatestItemKey(
                userId = userId,
                addressId = addressId,
                shareId = shareId,
                itemId = itemId,
                scope = ItemKeyRepository.Scope.SharedVault(
                    groupEmail = null,
                    decryptionSource = ItemKeyRepository.VaultDecryptionSource.Share
                )
            ).first()
        }
    }

    @Test
    fun `getLatestShareAndItemKey returns share key and item key`() = runTest {
        val expectedItemKey = ItemKey(12L, EncryptedByteArray(byteArrayOf(7, 7, 7)), "response")
        val shareKey = ShareKeyTestFactory.createPrivate()
        val shareKeyRepository = FakeShareKeyRepository().apply {
            emitGetShareKeyForRotation(shareKey)
        }
        val openItemKey = FakeOpenItemKey().apply { setResult(expectedItemKey) }
        val remoteDataSource = FakeRemoteItemKeyDataSource().apply {
            setFetchLatestItemKeyResponse(remoteResponse)
        }
        val repository = ItemKeyRepositoryImpl(
            shareKeyRepository = shareKeyRepository,
            remoteItemKeyRepository = remoteDataSource,
            openItemKey = openItemKey
        )

        val (resolvedShareKey, resolvedItemKey) = repository.getLatestShareAndItemKey(
            userId = userId,
            addressId = addressId,
            shareId = shareId,
            itemId = itemId,
            scope = ItemKeyRepository.Scope.SharedVault(
                groupEmail = null,
                decryptionSource = ItemKeyRepository.VaultDecryptionSource.Share
            )
        ).first()

        assertSame(shareKey, resolvedShareKey)
        assertEquals(expectedItemKey, resolvedItemKey)
        assertEquals(1, shareKeyRepository.getShareKeyForRotationCallCount)
    }

    @Test
    fun `getLatestShareAndItemKey still resolves share key when override key is provided`() = runTest {
        val expectedItemKey = ItemKey(12L, EncryptedByteArray(byteArrayOf(5, 5, 5)), "response")
        val shareKey = ShareKeyTestFactory.createPrivate()
        val overrideKey = FolderKey(
            rotation = 99L,
            key = EncryptedByteArray(byteArrayOf(1, 1, 1)),
            responseKey = "override-response"
        )
        val shareKeyRepository = FakeShareKeyRepository().apply {
            emitGetShareKeyForRotation(shareKey)
        }
        val openItemKey = FakeOpenItemKey().apply { setResult(expectedItemKey) }
        val remoteDataSource = FakeRemoteItemKeyDataSource().apply {
            setFetchLatestItemKeyResponse(remoteResponse)
        }
        val repository = ItemKeyRepositoryImpl(
            shareKeyRepository = shareKeyRepository,
            remoteItemKeyRepository = remoteDataSource,
            openItemKey = openItemKey
        )

        val (resolvedShareKey, resolvedItemKey) = repository.getLatestShareAndItemKey(
            userId = userId,
            addressId = addressId,
            shareId = shareId,
            itemId = itemId,
            scope = ItemKeyRepository.Scope.SharedVault(
                groupEmail = null,
                decryptionSource = ItemKeyRepository.VaultDecryptionSource.Folder(overrideKey)
            )
        ).first()

        assertSame(shareKey, resolvedShareKey)
        assertEquals(expectedItemKey, resolvedItemKey)
        assertSame(overrideKey, openItemKey.lastInviteKey)
        assertEquals(1, shareKeyRepository.getShareKeyForRotationCallCount)
    }

    @Test
    fun `getLatestShareAndItemKey for shared item does not fetch item latest key`() = runTest {
        val shareKey = ShareKeyTestFactory.createPrivate()
        val shareKeyRepository = FakeShareKeyRepository().apply {
            emitGetLatestKeyForShare(shareKey)
        }
        val openItemKey = FakeOpenItemKey().apply {
            setResult(ItemKey(0L, EncryptedByteArray(byteArrayOf(0)), "unused"))
        }
        val remoteDataSource = FakeRemoteItemKeyDataSource().apply {
            setFetchLatestItemKeyResponse(remoteResponse)
        }
        val repository = ItemKeyRepositoryImpl(
            shareKeyRepository = shareKeyRepository,
            remoteItemKeyRepository = remoteDataSource,
            openItemKey = openItemKey
        )

        val (resolvedShareKey, resolvedItemKey) = repository.getLatestShareAndItemKey(
            userId = userId,
            addressId = addressId,
            shareId = shareId,
            itemId = itemId,
            scope = ItemKeyRepository.Scope.SharedItem
        ).first()

        assertSame(shareKey, resolvedShareKey)
        assertEquals(shareKey.rotation, resolvedItemKey.rotation)
        assertEquals(shareKey.key, resolvedItemKey.key)
        assertEquals(shareKey.responseKey, resolvedItemKey.responseKey)
        assertEquals(1, shareKeyRepository.getLatestKeyForShareCallCount)
        assertEquals(0, remoteDataSource.fetchLatestItemKeyCallCount)
        assertEquals(0, shareKeyRepository.getShareKeyForRotationCallCount)
    }
}
