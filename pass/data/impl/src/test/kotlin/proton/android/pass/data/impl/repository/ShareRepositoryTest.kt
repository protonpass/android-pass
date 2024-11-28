/*
 * Copyright (c) 2023 Proton AG
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
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.Before
import org.junit.Test
import proton.android.pass.account.fakes.TestUserAddressRepository
import proton.android.pass.account.fakes.TestUserRepository
import proton.android.pass.crypto.fakes.context.TestEncryptionContextProvider
import proton.android.pass.crypto.fakes.usecases.TestCreateVault
import proton.android.pass.data.fakes.repositories.TestUserAccessDataRepository
import proton.android.pass.data.impl.db.entities.ShareEntity
import proton.android.pass.data.impl.fakes.TestLocalShareDataSource
import proton.android.pass.data.impl.fakes.TestPassDatabase
import proton.android.pass.data.impl.fakes.TestReencryptShareContents
import proton.android.pass.data.impl.fakes.TestRemoteShareDataSource
import proton.android.pass.data.impl.fakes.TestShareKeyRepository
import proton.android.pass.data.impl.repositories.ShareRepositoryImpl
import proton.android.pass.data.impl.responses.ShareResponse
import proton.android.pass.domain.Share
import proton.android.pass.test.domain.TestShare

class ShareRepositoryTest {

    private val encryptionContextProvider = TestEncryptionContextProvider()

    private lateinit var instance: ShareRepositoryImpl
    private lateinit var remote: TestRemoteShareDataSource
    private lateinit var local: TestLocalShareDataSource

    @Before
    fun setup() {
        remote = TestRemoteShareDataSource()
        local = TestLocalShareDataSource()

        instance = ShareRepositoryImpl(
            database = TestPassDatabase(),
            userRepository = TestUserRepository(),
            userAddressRepository = TestUserAddressRepository().apply {
                setAddresses(listOf(generateAddress("address1", UserId(USER_ID))))
            },
            remoteShareDataSource = remote,
            localShareDataSource = local,
            reencryptShareContents = TestReencryptShareContents().apply {
                setResponse(Result.success(null))
            },
            createVault = TestCreateVault().apply {
                setResult(Result.success(TestCreateVault.generateOutput()))
            },
            updateVault = proton.android.pass.crypto.fakes.usecases.TestUpdateVault(),
            encryptionContextProvider = encryptionContextProvider,
            shareKeyRepository = TestShareKeyRepository().apply {
                emitGetShareKeys(listOf())
            },
            userAccessDataRepository = TestUserAccessDataRepository().apply {
                sendValue(null)
            }
        )
    }

    @Test
    fun `refresh shares updates shares`() = runTest {
        // GIVEN
        val userId = UserId(USER_ID)
        // initial state: [share1(not-owner), share2, share3, share4]
        // desired state: [share1(yes-owner), share2, share3, share5]
        val share1 = TestShare.Vault.create(id = "123", isOwner = false)
        val share2 = TestShare.Item.create(id = "456")
        val share3 = TestShare.Vault.create(id = "789")
        val share4 = TestShare.Item.create(id = "654")
        val share5 = TestShare.Vault.create(id = "321")

        val asEntities = listOf(
            share1.toEntity(),
            share2.toEntity(),
            share3.toEntity(),
            share4.toEntity()
        )
        val asResponses = listOf(
            share1.toResponse().copy(owner = true),
            share2.toResponse(),
            share3.toResponse(),
            share5.toResponse()
        )

        remote.setGetSharesResponse(Result.success(asResponses))
        local.emitAllSharesForUser(asEntities)

        local.setDeleteSharesResponse(Result.success(true))
        local.setUpsertResponse(Result.success(Unit))

        // WHEN
        instance.refreshShares(userId)

        // THEN

        val upsertMemory = local.getUpsertMemory()
        assertThat(upsertMemory.size).isEqualTo(2)

        val firstUpsertMemory = upsertMemory[0]
        assertThat(firstUpsertMemory.size).isEqualTo(1)
        assertThat(firstUpsertMemory[0].id).isEqualTo(share1.id.id)

        val secondUpsertMemory = upsertMemory[1]
        assertThat(secondUpsertMemory).isEqualTo(listOf(share5.toEntity()))

        // share4 should be deleted
        val deleteMemory = local.getDeleteMemory()
        assertThat(deleteMemory.size).isEqualTo(1)
        assertThat(deleteMemory[0]).isEqualTo(setOf(share4.id))
    }

    private fun Share.toResponse(): ShareResponse = ShareResponse(
        shareId = id.id,
        vaultId = vaultId.id,
        addressId = "addressid-123",
        targetType = 1,
        targetId = vaultId.id,
        permission = 1,
        content = null,
        contentKeyRotation = null,
        contentFormatVersion = null,
        expirationTime = null,
        createTime = 0,
        owner = isOwner,
        shareRoleId = shareRole.value,
        targetMembers = memberCount,
        shared = shared,
        targetMaxMembers = maxMembers,
        newUserInvitesReady = newUserInvitesReady,
        pendingInvites = pendingInvites,
        canAutofill = canAutofill
    )

    private fun Share.toEntity(): ShareEntity = ShareEntity(
        id = id.id,
        userId = USER_ID,
        addressId = "addressid-123",
        vaultId = vaultId.id,
        targetType = 1,
        targetId = vaultId.id,
        permission = 1,
        content = null,
        contentKeyRotation = null,
        contentFormatVersion = null,
        expirationTime = null,
        createTime = 0,
        encryptedContent = null,
        isActive = true,
        owner = isOwner,
        shareRoleId = shareRole.value,
        targetMembers = memberCount,
        shared = shared,
        targetMaxMembers = maxMembers,
        newUserInvitesReady = newUserInvitesReady,
        pendingInvites = pendingInvites,
        canAutofill = canAutofill
    )

    companion object {
        private const val USER_ID = "123456"
    }

}
