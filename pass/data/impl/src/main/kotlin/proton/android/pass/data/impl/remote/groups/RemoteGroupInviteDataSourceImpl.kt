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

package proton.android.pass.data.impl.remote.groups

import me.proton.core.domain.entity.UserId
import me.proton.core.network.data.ApiProvider
import proton.android.pass.data.impl.api.PasswordManagerApi
import proton.android.pass.data.impl.requests.invites.AcceptInviteRequest
import proton.android.pass.data.impl.requests.invites.InviteKeyRotation
import proton.android.pass.domain.InviteToken
import javax.inject.Inject

class RemoteGroupInviteDataSourceImpl @Inject constructor(
    private val api: ApiProvider
) : RemoteGroupInviteDataSource {

    override suspend fun retrievePendingGroupInvites(userId: UserId, lastToken: String?) =
        api.get<PasswordManagerApi>(userId)
            .invoke { retrievePendingGroupInvites(lastToken) }
            .valueOrThrow
            .groupInvitesApiModel

    override suspend fun acceptGroupInvite(
        userId: UserId,
        inviteToken: InviteToken,
        keys: List<InviteKeyRotation>
    ) {
        api.get<PasswordManagerApi>(userId)
            .invoke { acceptGroupInvite(inviteToken.value, AcceptInviteRequest(keys)) }
            .valueOrThrow
    }

    override suspend fun rejectGroupInvite(userId: UserId, inviteToken: InviteToken) {
        api.get<PasswordManagerApi>(userId)
            .invoke { rejectGroupInvite(inviteToken.value) }
            .valueOrThrow
    }
}
