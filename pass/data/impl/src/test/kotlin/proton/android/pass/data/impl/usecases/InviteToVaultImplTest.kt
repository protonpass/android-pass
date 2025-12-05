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
import proton.android.pass.account.fakes.FakeAccountManager
import proton.android.pass.data.api.repositories.UserTarget
import proton.android.pass.data.api.usecases.InviteUserMode
import proton.android.pass.data.fakes.repositories.FakeUserInviteRepository
import proton.android.pass.data.fakes.usecases.FakeGetInviteUserMode
import proton.android.pass.data.impl.fakes.FakeShareRepository
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareRole
import proton.android.pass.test.domain.ShareTestFactory

class InviteToVaultImplTest {

    private lateinit var instance: InviteToVaultImpl

    private lateinit var accountManager: FakeAccountManager
    private lateinit var shareRepository: FakeShareRepository
    private lateinit var userInviteRepository: FakeUserInviteRepository
    private lateinit var getInviteUserMode: FakeGetInviteUserMode

    @Before
    fun setup() {
        accountManager = FakeAccountManager()
        getInviteUserMode = FakeGetInviteUserMode()
        shareRepository = FakeShareRepository().apply {
            val share = ShareTestFactory.Vault.create(
                id = SHARE_ID,
                shareRole = ShareRole.Admin
            )
            setGetByIdResult(Result.success(share))
        }

        userInviteRepository = FakeUserInviteRepository()

        instance = InviteToVaultImpl(
            accountManager = accountManager,
            userInviteRepository = userInviteRepository,
            shareRepository = shareRepository,
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
            inviteTargets = listOf(UserTarget(INVITED_ADDRESS, shareRole))
        )
        assertThat(res.isSuccess).isTrue()

        val existingUsersCalls = userInviteRepository.getExistingUsersInviteCalls()
        assertThat(existingUsersCalls.size).isEqualTo(1)

        val call = existingUsersCalls.first()
        assertThat(call.userId).isEqualTo(UserId(USER_ID))
        assertThat(call.shareId).isEqualTo(shareId)
        assertThat(call.inviteTargets.size).isEqualTo(1)
        assertThat(call.inviteTargets.first().email).isEqualTo(INVITED_ADDRESS)
        assertThat(call.inviteTargets.first().shareRole).isEqualTo(shareRole)

        val newUsersCalls = userInviteRepository.getNewUsersInviteCalls()
        assertThat(newUsersCalls).isEmpty()

        val refreshShareMemory = shareRepository.refreshShareMemory()
        val expectedRefreshSharePayload = FakeShareRepository.RefreshSharePayload(
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
            inviteTargets = listOf(UserTarget(INVITED_ADDRESS, ShareRole.Admin))
        )
        assertThat(res.isFailure).isTrue()
    }

    @Test
    fun `invite to vault returns failure if there is no user address for current user`() = runTest {
        setupAccountManager()
        setupUserAddress()
        userInviteRepository.setSendInvitesToExistingUsersResult(Result.failure(IllegalStateException("test")))

        val res = instance.invoke(
            shareId = ShareId(SHARE_ID),
            inviteTargets = listOf(UserTarget(INVITED_ADDRESS, ShareRole.Admin))
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
            inviteTargets = listOf(UserTarget(INVITED_ADDRESS, shareRole))
        )
        assertThat(res.isSuccess).isTrue()

        val existingUsersCalls = userInviteRepository.getExistingUsersInviteCalls()
        assertThat(existingUsersCalls).isEmpty()

        val newUsersCalls = userInviteRepository.getNewUsersInviteCalls()
        assertThat(newUsersCalls.size).isEqualTo(1)

        val call = newUsersCalls.first()
        assertThat(call.userId).isEqualTo(UserId(USER_ID))
        assertThat(call.shareId).isEqualTo(shareId)
        assertThat(call.inviteTargets.size).isEqualTo(1)
        assertThat(call.inviteTargets.first().email).isEqualTo(INVITED_ADDRESS)
        assertThat(call.inviteTargets.first().shareRole).isEqualTo(shareRole)
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
            inviteTargets = listOf(UserTarget(INVITED_ADDRESS, shareRole))
        )
        assertThat(res.isSuccess).isTrue()

        val refreshShareMemory = shareRepository.refreshShareMemory()
        val expectedRefreshSharePayload = FakeShareRepository.RefreshSharePayload(
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
