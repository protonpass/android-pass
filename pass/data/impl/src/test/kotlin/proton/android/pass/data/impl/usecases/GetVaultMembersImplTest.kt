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
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.Before
import org.junit.Test
import proton.android.pass.data.api.usecases.GroupMembers
import proton.android.pass.data.fakes.repositories.FakeShareMembersRepository
import proton.android.pass.data.fakes.usecases.FakeObserveCurrentUser
import proton.android.pass.data.fakes.usecases.FakeObserveGroupMembersByGroup
import proton.android.pass.data.fakes.usecases.shares.FakeObserveSharePendingInvites
import proton.android.pass.domain.GroupId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareRole
import proton.android.pass.domain.ShareType
import proton.android.pass.domain.shares.ShareMember
import proton.android.pass.test.domain.GroupTestFactory
import proton.android.pass.test.domain.UserTestFactory
import proton.android.pass.test.domain.VaultMemberTestFactory

class GetVaultMembersImplTest {

    private lateinit var observeCurrentUser: FakeObserveCurrentUser
    private lateinit var observeSharePendingInvites: FakeObserveSharePendingInvites
    private lateinit var observeGroupMembersByGroup: FakeObserveGroupMembersByGroup
    private lateinit var shareMembersRepository: FakeShareMembersRepository
    private lateinit var instance: GetVaultMembersImpl

    @Before
    fun setUp() {
        observeCurrentUser = FakeObserveCurrentUser()
        observeSharePendingInvites = FakeObserveSharePendingInvites()
        observeGroupMembersByGroup = FakeObserveGroupMembersByGroup()
        shareMembersRepository = FakeShareMembersRepository()
        instance = GetVaultMembersImpl(
            observeCurrentUser = observeCurrentUser,
            observeSharePendingInvites = observeSharePendingInvites,
            observeGroupMembersByGroup = observeGroupMembersByGroup,
            shareMembersRepository = shareMembersRepository
        )
    }

    @Test
    fun `group vault members include group id from observed group members`() = runTest {
        val user = UserTestFactory.create(email = "owner@proton.test", userId = UserId("user-id"))
        val shareId = ShareId("share-id")
        val groupId = GroupId("group-id")
        val groupEmail = "security-team@proton.test"
        shareMembersRepository.setMembers(
            listOf(
                ShareMember(
                    email = groupEmail,
                    shareId = ShareId("member-share-id"),
                    username = "Old name",
                    role = ShareRole.Admin,
                    isCurrentUser = false,
                    isOwner = false,
                    isGroup = true,
                    shareType = ShareType.Vault
                )
            )
        )
        observeCurrentUser.sendUser(user)
        observeSharePendingInvites.emitValue(emptyList())
        observeGroupMembersByGroup.emit(
            listOf(
                GroupMembers(
                    group = GroupTestFactory.create(id = groupId, email = groupEmail),
                    members = emptyList()
                )
            )
        )

        val result = instance(shareId).drop(1).first()

        assertThat(result).containsExactly(
            VaultMemberTestFactory.Group.create(
                email = groupEmail,
                shareId = ShareId("member-share-id"),
                groupId = groupId
            )
        )
    }
}
