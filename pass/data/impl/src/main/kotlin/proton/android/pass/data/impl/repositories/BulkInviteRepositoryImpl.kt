/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.data.impl.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import proton.android.pass.data.api.repositories.BulkInviteRepository
import proton.android.pass.data.api.repositories.GroupTarget
import proton.android.pass.data.api.repositories.InviteTarget
import proton.android.pass.data.api.repositories.UserTarget
import proton.android.pass.domain.ShareRole
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BulkInviteRepositoryImpl @Inject constructor() : BulkInviteRepository {

    private val invitesFlow: MutableStateFlow<List<InviteTarget>> =
        MutableStateFlow(emptyList())

    private val invalidAddressesFlow = MutableStateFlow<Set<String>>(emptySet())

    override fun storeInvites(inviteTargets: List<InviteTarget>) {
        val uniqueInvites = inviteTargets.distinctBy { invite ->
            when (invite) {
                is UserTarget -> "user_${invite.email}"
                is GroupTarget -> "group_${invite.groupId.id}"
            }
        }
        invitesFlow.update { uniqueInvites }
    }

    override fun setIndividualPermission(email: String, permission: ShareRole) {
        invitesFlow.update { state ->
            state.map { invite ->
                if (invite.email == email) {
                    when (invite) {
                        is UserTarget -> invite.copy(shareRole = permission)
                        is GroupTarget -> invite.copy(shareRole = permission)
                    }
                } else invite
            }
        }
    }

    override fun setAllPermissions(permission: ShareRole) {
        invitesFlow.update { state ->
            state.map { invite ->
                when (invite) {
                    is UserTarget -> invite.copy(shareRole = permission)
                    is GroupTarget -> invite.copy(shareRole = permission)
                }
            }
        }
    }

    override fun removeInvite(inviteTarget: InviteTarget) {
        invitesFlow.update { state ->
            state.filter { existingInvite ->
                when {
                    existingInvite is UserTarget && inviteTarget is UserTarget ->
                        existingInvite.email != inviteTarget.email
                    existingInvite is GroupTarget && inviteTarget is GroupTarget ->
                        existingInvite.groupId != inviteTarget.groupId
                    else -> true
                }
            }
        }
    }

    override fun observeInvites(): Flow<List<InviteTarget>> = invitesFlow

    override fun clear() {
        invitesFlow.update { emptyList() }
    }

    override fun updateInvalidAddresses(addresses: List<String>) {
        invalidAddressesFlow.update { addresses.toSet() }
    }

    override fun observeInvalidAddresses(): Flow<Set<String>> = invalidAddressesFlow

    override fun clearInvalidAddresses() {
        invalidAddressesFlow.update { emptySet() }
    }

}
