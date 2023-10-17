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

package proton.android.pass.data.impl.usecases

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.entity.key.KeyId
import me.proton.core.key.domain.entity.key.PrivateKey
import me.proton.core.user.domain.entity.AddressId
import me.proton.core.user.domain.entity.UserAddress
import me.proton.core.user.domain.entity.UserAddressKey
import org.junit.Before
import org.junit.Test
import proton.android.pass.account.fakes.TestAccountManager
import proton.android.pass.account.fakes.TestUserAddressRepository
import proton.android.pass.crypto.fakes.utils.TestUtils
import proton.android.pass.data.api.usecases.InviteToVault
import proton.android.pass.data.impl.db.entities.ShareEntity
import proton.android.pass.data.impl.fakes.TestCreateNewUserInviteSignature
import proton.android.pass.data.impl.fakes.TestEncryptShareKeysForUser
import proton.android.pass.data.impl.fakes.TestLocalShareDataSource
import proton.android.pass.data.impl.fakes.TestRemoteInviteDataSource
import proton.android.pass.data.impl.fakes.TestShareKeyRepository
import proton.android.pass.data.impl.local.LocalShareDataSource
import proton.pass.domain.ShareId
import proton.pass.domain.ShareRole

class InviteToVaultImplTest {

    private lateinit var instance: InviteToVaultImpl

    private lateinit var remoteDataSource: TestRemoteInviteDataSource
    private lateinit var accountManager: TestAccountManager
    private lateinit var userAddressRepository: TestUserAddressRepository
    private lateinit var localShareDataSource: LocalShareDataSource
    private lateinit var createNewUserInviteSignature: TestCreateNewUserInviteSignature
    private lateinit var encryptShareKeysForUser: TestEncryptShareKeysForUser

    @Before
    fun setup() {
        remoteDataSource = TestRemoteInviteDataSource()
        accountManager = TestAccountManager()
        userAddressRepository = TestUserAddressRepository()
        createNewUserInviteSignature = TestCreateNewUserInviteSignature()
        encryptShareKeysForUser = TestEncryptShareKeysForUser()
        localShareDataSource = TestLocalShareDataSource().apply {
            val entity = ShareEntity(
                id = SHARE_ID,
                userId = USER_ID,
                addressId = ADDRESS_ID,
                vaultId = "vaultid-123",
                targetType = 1,
                targetId = "vaultid-123",
                permission = 1,
                isPrimary = false,
                content = null,
                contentKeyRotation = null,
                contentFormatVersion = null,
                expirationTime = null,
                createTime = 0,
                encryptedContent = null,
                isActive = true,
                owner = true,
                shareRoleId = ShareRole.SHARE_ROLE_ADMIN,
                targetMembers = 0,
                shared = false,
                targetMaxMembers = 0
            )
            setGetByIdResponse(Result.success(entity))
        }

        instance = InviteToVaultImpl(
            userAddressRepository = userAddressRepository,
            accountManager = accountManager,
            encryptShareKeysForUser = encryptShareKeysForUser,
            shareKeyRepository = TestShareKeyRepository().apply {
                emitGetShareKeys(listOf(TestUtils.createShareKey().first))
            },
            remoteInviteDataSource = remoteDataSource,
            localShareDataSource = localShareDataSource,
            newUserInviteSignatureManager = createNewUserInviteSignature
        )
    }

    @Test
    fun `invite to vault does not go kaboom`() = runTest {
        setupAccountManager()
        setupUserAddress()

        val shareId = ShareId(SHARE_ID)
        val shareRole = ShareRole.Admin
        val res = instance.invoke(
            targetEmail = INVITED_ADDRESS,
            shareId = shareId,
            shareRole = shareRole,
            userMode = InviteToVault.UserMode.ExistingUser
        )
        assertThat(res.isSuccess).isTrue()

        val memory = remoteDataSource.getInviteMemory()
        assertThat(memory.size).isEqualTo(1)

        val memoryValue = memory.first()
        assertThat(memoryValue.userId).isEqualTo(UserId(USER_ID))
        assertThat(memoryValue.shareId).isEqualTo(shareId)
        assertThat(memoryValue.request.email).isEqualTo(INVITED_ADDRESS)
        assertThat(memoryValue.request.shareRoleId).isEqualTo(shareRole.value)
        assertThat(createNewUserInviteSignature.hasCreateBeenInvoked).isFalse()
    }

    @Test
    fun `invite to vault returns failure if there is no current user`() = runTest {
        setupAccountManager(null)
        setupUserAddress()

        val res = instance.invoke(
            targetEmail = INVITED_ADDRESS,
            shareId = ShareId(SHARE_ID),
            shareRole = ShareRole.Admin,
            userMode = InviteToVault.UserMode.ExistingUser
        )
        assertThat(res.isFailure).isTrue()
    }

    @Test
    fun `invite to vault returns failure if encryptShareKeysForUser fails`() = runTest {
        setupAccountManager()
        setupUserAddress()
        encryptShareKeysForUser.setResult(Result.failure(IllegalStateException("test")))

        val res = instance.invoke(
            targetEmail = INVITED_ADDRESS,
            shareId = ShareId(SHARE_ID),
            shareRole = ShareRole.Admin,
            userMode = InviteToVault.UserMode.ExistingUser
        )
        assertThat(res.isFailure).isTrue()
    }

    @Test
    fun `invite to vault returns failure if there is no user address for current user`() = runTest {
        setupAccountManager()

        val res = instance.invoke(
            targetEmail = INVITED_ADDRESS,
            shareId = ShareId(SHARE_ID),
            shareRole = ShareRole.Admin,
            userMode = InviteToVault.UserMode.ExistingUser
        )
        assertThat(res.isFailure).isTrue()
    }

    @Test
    fun `invite NewUser to vault returns success`() = runTest {
        setupAccountManager()
        setupUserAddress()

        val shareId = ShareId(SHARE_ID)
        val shareRole = ShareRole.Admin
        val res = instance.invoke(
            targetEmail = INVITED_ADDRESS,
            shareId = shareId,
            shareRole = shareRole,
            userMode = InviteToVault.UserMode.NewUser
        )
        assertThat(res.isSuccess).isTrue()

        val memory = remoteDataSource.getNewUserInviteMemory()
        assertThat(memory.size).isEqualTo(1)

        val memoryValue = memory.first()
        assertThat(memoryValue.userId).isEqualTo(UserId(USER_ID))
        assertThat(memoryValue.shareId).isEqualTo(shareId)
        assertThat(memoryValue.request.email).isEqualTo(INVITED_ADDRESS)
        assertThat(memoryValue.request.shareRoleId).isEqualTo(shareRole.value)
        assertThat(createNewUserInviteSignature.hasCreateBeenInvoked).isTrue()
    }

    private fun setupAccountManager(userId: UserId? = UserId(USER_ID)) {
        accountManager.sendPrimaryUserId(userId)
    }


    private fun setupUserAddress() {
        val addressId = AddressId(ADDRESS_ID)
        val key = UserAddressKey(
            addressId = addressId,
            version = 1,
            flags = 0,
            active = true,
            keyId = KeyId("KeyId123"),
            privateKey = PrivateKey(
                key = "key",
                isPrimary = true,
                passphrase = null
            )
        )
        val userAddress = UserAddress(
            userId = UserId(USER_ID),
            addressId = addressId,
            email = INVITER_ADDRESS,
            canSend = true,
            canReceive = true,
            enabled = true,
            keys = listOf(key),
            signedKeyList = null,
            order = 1
        )
        userAddressRepository.setAddresses(listOf(userAddress))
    }

    companion object {
        private const val INVITER_ADDRESS = "inviter@local"
        private const val INVITED_ADDRESS = "invited@remote"
        private const val USER_ID = "InviteToVaultImplTest-UserId"
        private const val SHARE_ID = "InviteToVaultImplTest-ShareId"
        private const val ADDRESS_ID = "InviteToVaultImplTest-AddressID"
    }
}
