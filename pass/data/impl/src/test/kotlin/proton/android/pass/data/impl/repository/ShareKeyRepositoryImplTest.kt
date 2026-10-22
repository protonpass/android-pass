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

package proton.android.pass.data.impl.repository

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import proton.android.pass.crypto.api.error.InvalidSignature
import proton.android.pass.crypto.fakes.context.FakeEncryptionContextProvider
import proton.android.pass.crypto.fakes.usecases.FakeGetGroupKeyDecryptor
import proton.android.pass.crypto.fakes.usecases.FakeGetUserKeys
import proton.android.pass.data.impl.crypto.ReencryptGroupKeyInput
import proton.android.pass.data.impl.exception.UserKeyNotActive
import proton.android.pass.data.impl.fakes.FakeLocalShareKeyDataSource
import proton.android.pass.data.impl.fakes.FakeReencryptShareKey
import proton.android.pass.data.impl.fakes.FakeRemoteShareKeyDataSource
import proton.android.pass.data.impl.fakes.mother.ShareKeyEntityTestFactory
import proton.android.pass.data.impl.repositories.ShareKeyRepositoryImpl
import proton.android.pass.data.impl.responses.ShareKeyResponse
import proton.android.pass.domain.AddressId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.UserId

class ShareKeyRepositoryImplTest {

    private lateinit var reencryptShareKey: FakeReencryptShareKey
    private lateinit var getUserKeys: FakeGetUserKeys
    private lateinit var getGroupKeyDecryptor: FakeGetGroupKeyDecryptor
    private lateinit var local: FakeLocalShareKeyDataSource
    private lateinit var remote: FakeRemoteShareKeyDataSource
    private lateinit var instance: ShareKeyRepositoryImpl

    @Before
    fun setup() {
        reencryptShareKey = FakeReencryptShareKey()
        getUserKeys = FakeGetUserKeys()
        getGroupKeyDecryptor = FakeGetGroupKeyDecryptor()
        local = FakeLocalShareKeyDataSource()
        remote = FakeRemoteShareKeyDataSource()
        instance = ShareKeyRepositoryImpl(
            reencryptShareKey = reencryptShareKey,
            getUserKeys = getUserKeys,
            getGroupKeyDecryptor = getGroupKeyDecryptor,
            encryptionContextProvider = FakeEncryptionContextProvider(),
            localDataSource = local,
            remoteDataSource = remote
        )
    }

    @Test
    fun `active local keys are returned without reencrypting or requesting remote`() = runTest {
        local.setEntity(localEntity(isActive = true))

        val result = getShareKeys(forceRefresh = false)

        assertThat(result).hasSize(1)
        assertThat(reencryptShareKey.calls).isEmpty()
        assertThat(remote.requestCount).isEqualTo(0)
    }

    @Test
    fun `inactive local key is reencrypted for the user and stored as active`() = runTest {
        local.setEntity(localEntity(isActive = false))

        val result = getShareKeys(forceRefresh = false)

        assertThat(result).hasSize(1)
        assertThat(getUserKeys.requestedUserIds).containsExactly(USER_ID)
        assertThat(reencryptShareKey.calls).hasSize(1)
        assertThat(remote.requestCount).isEqualTo(0)
        assertThat(storedEntities().single().isActive).isTrue()
    }

    @Test
    fun `inactive local key that stays inactive falls back to remote`() = runTest {
        local.setEntity(localEntity(isActive = false))
        remote.setKeys(listOf(remoteKey(rotation = ROTATION, key = "remote-key")))
        reencryptShareKey.behavior = { throw UserKeyNotActive() }

        getShareKeys(forceRefresh = false)

        assertThat(remote.requestCount).isEqualTo(1)
        val stored = storedEntities().single()
        assertThat(stored.key).isEqualTo("remote-key")
        assertThat(stored.isActive).isFalse()
    }

    @Test
    fun `force refresh requests remote keys and reencrypts them for the user`() = runTest {
        local.setEntity(localEntity(isActive = true))
        remote.setKeys(listOf(remoteKey(rotation = ROTATION + 1, key = "remote-key")))

        val result = getShareKeys(forceRefresh = true)

        assertThat(result).hasSize(1)
        assertThat(remote.requestCount).isEqualTo(1)
        assertThat(getUserKeys.requestedUserIds).containsExactly(USER_ID)
        assertThat(reencryptShareKey.calls).hasSize(1)
        val stored = storedEntities().first { it.rotation == ROTATION + 1 }
        assertThat(stored.isActive).isTrue()
        assertThat(stored.symmetricallyEncryptedKey.array)
            .isEqualTo(FakeReencryptShareKey.DEFAULT_RESULT.array)
    }

    @Test
    fun `remote keys are not stored when shouldStoreLocally is false`() = runTest {
        remote.setKeys(listOf(remoteKey(rotation = ROTATION, key = "remote-key")))

        val result = getShareKeys(forceRefresh = true, shouldStoreLocally = false)

        assertThat(result).hasSize(1)
        assertThat(storedEntities()).isEmpty()
    }

    @Test
    fun `unexpected reencrypt failure is propagated`() = runTest {
        remote.setKeys(listOf(remoteKey(rotation = ROTATION, key = "remote-key")))
        reencryptShareKey.behavior = { throw IllegalStateException("boom") }

        val result = runCatching { getShareKeys(forceRefresh = true) }

        assertThat(result.exceptionOrNull()).isInstanceOf(IllegalStateException::class.java)
        assertThat(storedEntities()).isEmpty()
    }

    @Test
    fun `group key from remote is reencrypted with the group decryptor`() = runTest {
        remote.setKeys(listOf(remoteKey(rotation = ROTATION, key = "remote-key")))

        val result = getShareKeys(forceRefresh = true, groupEmail = GROUP_EMAIL)

        assertThat(result).hasSize(1)
        assertThat(getGroupKeyDecryptor.requests)
            .containsExactly(FakeGetGroupKeyDecryptor.Request(USER_ID, AddressId(ADDRESS_ID), GROUP_EMAIL))
        val input = reencryptShareKey.calls.single() as ReencryptGroupKeyInput
        assertThat(input.key).isEqualTo("remote-key")
        assertThat(input.decryptor).isSameInstanceAs(getGroupKeyDecryptor.decryptor)
        assertThat(getUserKeys.requestedUserIds).isEmpty()
        assertThat(storedEntities().single().isActive).isTrue()
    }

    @Test
    fun `group key from local inactive keys is reencrypted with the group decryptor and stored as active`() = runTest {
        local.setEntity(localEntity(isActive = false))

        val result = getShareKeys(forceRefresh = false, groupEmail = GROUP_EMAIL)

        assertThat(result).hasSize(1)
        assertThat(reencryptShareKey.calls.single()).isInstanceOf(ReencryptGroupKeyInput::class.java)
        assertThat(getGroupKeyDecryptor.requests).hasSize(1)
        assertThat(getUserKeys.requestedUserIds).isEmpty()
        assertThat(remote.requestCount).isEqualTo(0)
        assertThat(storedEntities().single().isActive).isTrue()
    }

    @Test
    fun `group key rethrows a failure to get the decryptor`() = runTest {
        remote.setKeys(listOf(remoteKey(rotation = ROTATION, key = "remote-key")))
        getGroupKeyDecryptor.failure = IllegalStateException("Invited address not found")

        val result = runCatching { getShareKeys(forceRefresh = true, groupEmail = GROUP_EMAIL) }

        assertThat(result.exceptionOrNull()).isInstanceOf(IllegalStateException::class.java)
        assertThat(result.exceptionOrNull()?.message).isEqualTo("Invited address not found")
        assertThat(reencryptShareKey.calls).isEmpty()
        assertThat(storedEntities()).isEmpty()
    }

    @Test
    fun `group key propagates InvalidSignature from the reencryption`() = runTest {
        remote.setKeys(listOf(remoteKey(rotation = ROTATION, key = "remote-key")))
        reencryptShareKey.behavior = { throw InvalidSignature("ShareKey signature did not match") }

        val result = runCatching { getShareKeys(forceRefresh = true, groupEmail = GROUP_EMAIL) }

        assertThat(result.exceptionOrNull()).isInstanceOf(InvalidSignature::class.java)
        assertThat(storedEntities()).isEmpty()
    }

    @Test
    fun `user is requested once per non group key`() = runTest {
        remote.setKeys(
            listOf(
                remoteKey(rotation = ROTATION, key = "key-1"),
                remoteKey(rotation = ROTATION + 1, key = "key-2")
            )
        )

        getShareKeys(forceRefresh = true)

        assertThat(getUserKeys.requestedUserIds).containsExactly(USER_ID, USER_ID)
    }

    private suspend fun getShareKeys(
        forceRefresh: Boolean,
        shouldStoreLocally: Boolean = true,
        groupEmail: String? = null
    ) = instance.getShareKeys(
        userId = USER_ID,
        addressId = AddressId(ADDRESS_ID),
        shareId = SHARE_ID,
        groupEmail = groupEmail,
        forceRefresh = forceRefresh,
        shouldStoreLocally = shouldStoreLocally
    ).first()

    private suspend fun storedEntities() = local.getAllShareKeysForShare(USER_ID, SHARE_ID).first()

    private fun localEntity(isActive: Boolean) = ShareKeyEntityTestFactory.create(
        rotation = ROTATION,
        userId = USER_ID.id,
        addressId = ADDRESS_ID,
        shareId = SHARE_ID.id,
        isActive = isActive
    )

    private fun remoteKey(rotation: Long, key: String) = ShareKeyResponse(
        keyRotation = rotation,
        userKeyId = "user-key-id",
        key = key,
        createTime = 0L
    )

    private companion object {
        val USER_ID = UserId("user-id")
        val SHARE_ID = ShareId("share-id")
        const val ADDRESS_ID = "address-id"
        const val ROTATION = 1L
        const val GROUP_EMAIL = "group@example.com"
    }
}
