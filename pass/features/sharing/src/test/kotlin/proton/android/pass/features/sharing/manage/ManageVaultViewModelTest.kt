/*
 * Copyright (c) 2023-2024 Proton AG
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

package proton.android.pass.features.sharing.manage

import com.google.common.truth.Truth.assertThat
import kotlinx.collections.immutable.persistentListOf
import org.junit.Before
import org.junit.Test
import proton.android.pass.commonui.fakes.TestSavedStateHandleProvider
import proton.android.pass.data.api.usecases.VaultMember
import proton.android.pass.data.fakes.usecases.TestCanShareShare
import proton.android.pass.data.fakes.usecases.TestConfirmNewUserInvite
import proton.android.pass.data.fakes.usecases.TestGetUserPlan
import proton.android.pass.data.fakes.usecases.TestGetVaultMembers
import proton.android.pass.data.fakes.usecases.TestObserveVaultWithItemCountById
import proton.android.pass.domain.InviteId
import proton.android.pass.domain.NewUserInviteId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareRole
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.fakes.TestSnackbarDispatcher

internal class ManageVaultViewModelTest {

    private lateinit var instance: ManageVaultViewModel

    private lateinit var confirmNewUserInvite: TestConfirmNewUserInvite

    @Before
    fun setup() {
        confirmNewUserInvite = TestConfirmNewUserInvite()
        instance = ManageVaultViewModel(
            observeVaultById = TestObserveVaultWithItemCountById(),
            getVaultMembers = TestGetVaultMembers(),
            canShareShare = TestCanShareShare(),
            savedStateHandleProvider = TestSavedStateHandleProvider().apply {
                get()[CommonNavArgId.ShareId.key] = SHARE_ID
            },
            snackbarDispatcher = TestSnackbarDispatcher(),
            confirmNewUserInvite = confirmNewUserInvite,
            getUserPlan = TestGetUserPlan()
        )
    }

    @Test
    fun `can sort empty member list`() {
        val res = instance.partitionMembers(emptyList())
        val expected = ManageVaultViewModel.PartitionedMembers(
            members = persistentListOf(),
            invites = persistentListOf()
        )
        assertThat(res).isEqualTo(expected)
    }

    @Test
    fun `can sort member and invite list`() {
        val member1 = VaultMember.Member(
            email = "email1",
            shareId = ShareId(SHARE_ID),
            username = "username1",
            role = ShareRole.Admin,
            isCurrentUser = true,
            isOwner = true
        )
        val member2 = VaultMember.Member(
            email = "email2",
            shareId = ShareId(SHARE_ID),
            username = "username2",
            role = ShareRole.Admin,
            isCurrentUser = false,
            isOwner = false
        )
        val invite1 = VaultMember.InvitePending(
            email = "invited1",
            inviteId = InviteId("123")
        )
        val invite2 = VaultMember.InvitePending(
            email = "invited2",
            inviteId = InviteId("456")
        )
        val newUserInvite1 = VaultMember.NewUserInvitePending(
            email = "invited3",
            newUserInviteId = NewUserInviteId("789"),
            role = ShareRole.Write,
            signature = "",
            inviteState = VaultMember.NewUserInvitePending.InviteState.PendingAccountCreation
        )
        val newUserInvite2 = VaultMember.NewUserInvitePending(
            email = "invited4",
            newUserInviteId = NewUserInviteId("abcde"),
            role = ShareRole.Write,
            signature = "",
            inviteState = VaultMember.NewUserInvitePending.InviteState.PendingAcceptance
        )
        val allInvites = listOf(member1, member2, invite1, invite2, newUserInvite1, newUserInvite2)
        val res = instance.partitionMembers(allInvites)
        val expected = ManageVaultViewModel.PartitionedMembers(
            members = persistentListOf(
                member1,
                member2
            ),
            invites = persistentListOf(
                newUserInvite2,
                newUserInvite1,
                invite1,
                invite2
            )
        )
        assertThat(res).isEqualTo(expected)
    }

    companion object {
        const val SHARE_ID = "ManageVaultViewModelTest-ShareId"
    }

}
