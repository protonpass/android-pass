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

package proton.android.pass.data.impl.usecases

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.Before
import org.junit.Test
import proton.android.pass.account.fakes.FakeAccountManager
import proton.android.pass.data.api.errors.UserIdNotAvailableError
import proton.android.pass.data.fakes.repositories.FakeShareMembersRepository
import proton.android.pass.data.impl.fakes.FakeShareRepository
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareRole
import proton.android.pass.domain.ShareType
import proton.android.pass.domain.shares.ShareMember

class RemoveShareMemberImplTest {

    private lateinit var accountManager: FakeAccountManager
    private lateinit var shareMembersRepository: FakeShareMembersRepository
    private lateinit var shareRepository: FakeShareRepository
    private lateinit var instance: RemoveShareMemberImpl

    @Before
    fun setUp() {
        accountManager = FakeAccountManager()
        shareMembersRepository = FakeShareMembersRepository()
        shareRepository = FakeShareRepository()

        instance = RemoveShareMemberImpl(
            accountManager = accountManager,
            shareMemberRepository = shareMembersRepository,
            shareRepository = shareRepository
        )
    }

    @Test
    fun `removes member and updates member count`() = runTest {
        accountManager.sendPrimaryUserId(USER_ID)
        shareMembersRepository.setMembers(
            listOf(
                ShareMember(
                    email = "member1@proton.test",
                    shareId = ShareId("member-share-1"),
                    username = "Member One",
                    role = ShareRole.Admin,
                    isCurrentUser = false,
                    isOwner = false,
                    isGroup = false,
                    shareType = ShareType.Vault
                ),
                ShareMember(
                    email = "member2@proton.test",
                    shareId = ShareId("member-share-2"),
                    username = "Member Two",
                    role = ShareRole.Read,
                    isCurrentUser = false,
                    isOwner = false,
                    isGroup = false,
                    shareType = ShareType.Vault
                )
            )
        )

        instance(shareId = SHARE_ID, memberShareId = MEMBER_SHARE_ID)

        val updateCalls = shareRepository.getUpdateMembersCountMemory()
        assertThat(updateCalls).hasSize(1)
        assertThat(updateCalls.first().userId).isEqualTo(USER_ID)
        assertThat(updateCalls.first().shareId).isEqualTo(SHARE_ID)
        assertThat(updateCalls.first().count).isEqualTo(2)
    }

    @Test
    fun `throws when no primary user`() = runTest {
        accountManager.sendPrimaryUserId(null)

        runCatching { instance(shareId = SHARE_ID, memberShareId = MEMBER_SHARE_ID) }
            .also { assertThat(it.isFailure).isTrue() }
            .also { assertThat(it.exceptionOrNull()).isInstanceOf(UserIdNotAvailableError::class.java) }

        assertThat(shareRepository.getUpdateMembersCountMemory()).isEmpty()
    }

    @Test
    fun `member count update failure does not propagate — deletion succeeds`() = runTest {
        accountManager.sendPrimaryUserId(USER_ID)
        shareMembersRepository.setGetMembersTotalError(RuntimeException("network error"))

        // Should not throw even though the count refresh failed
        instance(shareId = SHARE_ID, memberShareId = MEMBER_SHARE_ID)

        assertThat(shareRepository.getUpdateMembersCountMemory()).isEmpty()
    }

    private companion object {
        private val USER_ID = UserId("user-id")
        private val SHARE_ID = ShareId("share-id")
        private val MEMBER_SHARE_ID = ShareId("member-share-id")
    }
}
