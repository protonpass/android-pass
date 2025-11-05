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
import proton.android.pass.data.api.repositories.AddressPermission
import proton.android.pass.data.api.usecases.InviteUserMode
import proton.android.pass.data.fakes.usecases.TestGetInviteUserMode
import proton.android.pass.data.impl.fakes.TestCreateNewUserInviteSignature
import proton.android.pass.data.impl.fakes.TestEncryptShareKeysForUser
import proton.android.pass.data.impl.fakes.TestRemoteInviteDataSource
import proton.android.pass.data.impl.fakes.TestShareKeyRepository
import proton.android.pass.data.impl.fakes.TestShareRepository
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareRole
import proton.android.pass.test.TestUtils
import proton.android.pass.test.domain.TestShare

class InviteToVaultImplTest {

    private lateinit var instance: InviteToVaultImpl

    private lateinit var remoteDataSource: TestRemoteInviteDataSource
    private lateinit var accountManager: TestAccountManager
    private lateinit var shareRepository: TestShareRepository
    private lateinit var createNewUserInviteSignature: TestCreateNewUserInviteSignature
    private lateinit var encryptShareKeysForUser: TestEncryptShareKeysForUser
    private lateinit var getInviteUserMode: TestGetInviteUserMode

    @Before
    fun setup() {
        remoteDataSource = TestRemoteInviteDataSource()
        accountManager = TestAccountManager()
        createNewUserInviteSignature = TestCreateNewUserInviteSignature()
        encryptShareKeysForUser = TestEncryptShareKeysForUser()
        getInviteUserMode = TestGetInviteUserMode()
        shareRepository = TestShareRepository().apply {
            val share = TestShare.Vault.create(
                id = SHARE_ID,
                shareRole = ShareRole.Admin
            )
            setGetByIdResult(Result.success(share))
        }

        instance = InviteToVaultImpl(
            accountManager = accountManager,
            encryptShareKeysForUser = encryptShareKeysForUser,
            shareKeyRepository = TestShareKeyRepository().apply {
                emitGetShareKeys(listOf(TestUtils.createShareKey().first))
            },
            remoteInviteDataSource = remoteDataSource,
            shareRepository = shareRepository,
            newUserInviteSignatureManager = createNewUserInviteSignature,
            getInviteUserMode = getInviteUserMode
        )
    }

    @Test
    fun `invite to vault does not go kaboom`() = runTest {
        setupAccountManager()
        setupUserAddress()

        val shareId = ShareId(SHARE_ID)
        val shareRole = ShareRole.Admin
        val res = instance.invoke(
            shareId = shareId,
            inviteAddresses = listOf(AddressPermission(INVITED_ADDRESS, shareRole))
        )
        assertThat(res.isSuccess).isTrue()

        val memory = remoteDataSource.getInviteMemory()
        assertThat(memory.size).isEqualTo(1)

        val memoryValue = memory.first()
        assertThat(memoryValue.userId).isEqualTo(UserId(USER_ID))
        assertThat(memoryValue.shareId).isEqualTo(shareId)

        assertThat(memoryValue.existingRequests.invites.size).isEqualTo(1)

        val firstInvite = memoryValue.existingRequests.invites.first()
        assertThat(firstInvite.email).isEqualTo(INVITED_ADDRESS)
        assertThat(firstInvite.shareRoleId).isEqualTo(shareRole.value)

        assertThat(memoryValue.newUserRequests.invites).isEmpty()
        assertThat(createNewUserInviteSignature.hasCreateBeenInvoked).isFalse()

        val refreshShareMemory = shareRepository.refreshShareMemory()
        val expectedRefreshSharePayload = TestShareRepository.RefreshSharePayload(
            userId = UserId(USER_ID),
            shareId = ShareId(SHARE_ID)
        )
        assertThat(refreshShareMemory).isEqualTo(listOf(expectedRefreshSharePayload))
    }

    @Test
    fun `invite to vault returns failure if there is no current user`() = runTest {
        setupAccountManager(null)
        setupUserAddress()

        val res = instance.invoke(
            shareId = ShareId(SHARE_ID),
            inviteAddresses = listOf(AddressPermission(INVITED_ADDRESS, ShareRole.Admin))
        )
        assertThat(res.isFailure).isTrue()
    }

    @Test
    fun `invite to vault returns failure if encryptShareKeysForUser fails`() = runTest {
        setupAccountManager()
        setupUserAddress()
        encryptShareKeysForUser.setResult(Result.failure(IllegalStateException("test")))

        val res = instance.invoke(
            shareId = ShareId(SHARE_ID),
            inviteAddresses = listOf(AddressPermission(INVITED_ADDRESS, ShareRole.Admin))
        )
        assertThat(res.isFailure).isTrue()
    }

    @Test
    fun `invite to vault returns failure if there is no user address for current user`() = runTest {
        shareRepository.setGetAddressForShareIdResult(Result.failure(IllegalStateException("test")))
        setupAccountManager()

        val res = instance.invoke(
            shareId = ShareId(SHARE_ID),
            inviteAddresses = listOf(AddressPermission(INVITED_ADDRESS, ShareRole.Admin))
        )
        assertThat(res.isFailure).isTrue()
    }

    @Test
    fun `invite NewUser to vault returns success`() = runTest {
        setupAccountManager()
        setupUserAddress()
        getInviteUserMode.setResult(Result.success(InviteUserMode.NewUser))

        val shareId = ShareId(SHARE_ID)
        val shareRole = ShareRole.Admin
        val res = instance.invoke(
            shareId = shareId,
            inviteAddresses = listOf(AddressPermission(INVITED_ADDRESS, shareRole))
        )
        assertThat(res.isSuccess).isTrue()

        val memory = remoteDataSource.getInviteMemory()
        assertThat(memory.size).isEqualTo(1)

        val memoryValue = memory.first()
        assertThat(memoryValue.userId).isEqualTo(UserId(USER_ID))
        assertThat(memoryValue.shareId).isEqualTo(shareId)

        assertThat(memoryValue.existingRequests.invites).isEmpty()

        assertThat(memoryValue.newUserRequests.invites.size).isEqualTo(1)

        val firstInvite = memoryValue.newUserRequests.invites.first()
        assertThat(firstInvite.email).isEqualTo(INVITED_ADDRESS)
        assertThat(firstInvite.shareRoleId).isEqualTo(shareRole.value)
        assertThat(createNewUserInviteSignature.hasCreateBeenInvoked).isTrue()
    }

    @Test
    fun `invite to vault returns success even if refresh share fails`() = runTest {
        setupAccountManager()
        setupUserAddress()
        shareRepository.setRefreshShareResult(Result.failure(IllegalStateException("test")))

        val shareId = ShareId(SHARE_ID)
        val shareRole = ShareRole.Admin
        val res = instance.invoke(
            shareId = shareId,
            inviteAddresses = listOf(AddressPermission(INVITED_ADDRESS, shareRole))
        )
        assertThat(res.isSuccess).isTrue()

        val refreshShareMemory = shareRepository.refreshShareMemory()
        val expectedRefreshSharePayload = TestShareRepository.RefreshSharePayload(
            userId = UserId(USER_ID),
            shareId = ShareId(SHARE_ID)
        )
        assertThat(refreshShareMemory).isEqualTo(listOf(expectedRefreshSharePayload))
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
        shareRepository.setGetAddressForShareIdResult(Result.success(userAddress))
        getInviteUserMode.setResult(Result.success(InviteUserMode.ExistingUser))
    }

    companion object {
        private const val INVITER_ADDRESS = "inviter@local"
        private const val INVITED_ADDRESS = "invited@remote"
        private const val USER_ID = "InviteToVaultImplTest-UserId"
        private const val SHARE_ID = "InviteToVaultImplTest-ShareId"
        private const val ADDRESS_ID = "InviteToVaultImplTest-AddressID"
    }
}
