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
import proton.android.pass.account.fakes.FakePublicAddressRepository
import proton.android.pass.account.fakes.FakeUserAddressRepository
import proton.android.pass.crypto.fakes.context.FakeEncryptionContextProvider
import proton.android.pass.data.impl.exception.UserKeyNotActive
import proton.android.pass.data.impl.fakes.FakeGetUserKeys
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
    private lateinit var local: FakeLocalShareKeyDataSource
    private lateinit var remote: FakeRemoteShareKeyDataSource
    private lateinit var userAddressRepository: FakeUserAddressRepository
    private lateinit var instance: ShareKeyRepositoryImpl

    @Before
    fun setup() {
        reencryptShareKey = FakeReencryptShareKey()
        getUserKeys = FakeGetUserKeys()
        local = FakeLocalShareKeyDataSource()
        remote = FakeRemoteShareKeyDataSource()
        userAddressRepository = FakeUserAddressRepository()
        instance = ShareKeyRepositoryImpl(
            reencryptShareKey = reencryptShareKey,
            getUserKeys = getUserKeys,
            encryptionContextProvider = FakeEncryptionContextProvider(),
            localDataSource = local,
            remoteDataSource = remote,
            userAddressRepository = userAddressRepository,
            publicAddressRepository = FakePublicAddressRepository()
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
    fun `group key fails when the invited address is not stored locally`() = runTest {
        remote.setKeys(listOf(remoteKey(rotation = ROTATION, key = "remote-key")))
        userAddressRepository.setAddress(null)

        val result = runCatching { getShareKeys(forceRefresh = true, groupEmail = "group@example.com") }

        assertThat(result.exceptionOrNull()).isInstanceOf(IllegalStateException::class.java)
        assertThat(reencryptShareKey.calls).isEmpty()
        assertThat(getUserKeys.requestedUserIds).isEmpty()
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
    }
}
