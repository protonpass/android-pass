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

package proton.android.pass.featuresharing.impl.manage

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.data.api.usecases.VaultMember
import proton.android.pass.domain.InviteId
import proton.android.pass.domain.NewUserInviteId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareRole

class ThemedMVMPreviewProvider : ThemePairPreviewProvider<VaultMemberRowInput>(
    ManageVaultMemberRowPreviewProvider()
)

class ManageVaultMemberRowPreviewProvider : PreviewParameterProvider<VaultMemberRowInput> {
    override val values: Sequence<VaultMemberRowInput>
        get() = sequence {
            // Loading
            yield(VaultMemberRowInput(VaultMemberContent.Loading))

            // Current user
            for (isOwner in listOf(true, false)) {
                val member = VaultMemberContent.Member(
                    member = VaultMember.Member(
                        email = "someuser@test.local",
                        shareId = ShareId("someShareId"),
                        username = "someuser",
                        role = ShareRole.Admin,
                        isCurrentUser = true,
                        isOwner = isOwner
                    )
                )
                yield(VaultMemberRowInput(member))
            }

            // Member Roles
            for (role in listOf(ShareRole.Admin, ShareRole.Write, ShareRole.Read)) {
                val member = VaultMemberContent.Member(
                    member = VaultMember.Member(
                        email = "someuser@test.local",
                        shareId = ShareId("someShareId"),
                        username = "someuser",
                        role = role,
                        isCurrentUser = false,
                        isOwner = false
                    )
                )
                yield(VaultMemberRowInput(member))
            }

            // Invite
            yield(
                VaultMemberRowInput(
                    VaultMemberContent.Member(
                        member = VaultMember.InvitePending(
                            email = "invited@user.test",
                            inviteId = InviteId("someInviteId")
                        ),
                    )
                )
            )

            // New user invite
            for (status in VaultMember.NewUserInvitePending.InviteState.values()) {
                yield(
                    VaultMemberRowInput(
                        VaultMemberContent.Member(
                            member = VaultMember.NewUserInvitePending(
                                email = "invited@user.test",
                                signature = "",
                                newUserInviteId = NewUserInviteId("someInviteId"),
                                role = ShareRole.Write,
                                inviteState = status
                            ),
                        )
                    )
                )
            }
        }

}

data class VaultMemberRowInput(
    val member: VaultMemberContent
)
