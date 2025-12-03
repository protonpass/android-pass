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

package proton.android.pass.data.api.repositories

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.Option
import proton.android.pass.domain.InviteRecommendations
import proton.android.pass.domain.InviteToken
import proton.android.pass.domain.PendingInvite
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareInvite
import proton.android.pass.domain.events.EventToken

interface UserInviteRepository {

    suspend fun getInvite(userId: UserId, inviteToken: InviteToken): Option<PendingInvite>

    fun observeInvites(userId: UserId): Flow<List<PendingInvite>>

    suspend fun refreshInvites(userId: UserId, eventToken: EventToken?): Boolean

    suspend fun acceptInvite(userId: UserId, inviteToken: InviteToken): ShareInvite

    suspend fun rejectInvite(userId: UserId, inviteToken: InviteToken)

    suspend fun sendInvitesToExistingUsers(
        userId: UserId,
        shareId: ShareId,
        inviteTargets: List<InviteTarget>
    )

    suspend fun sendInvitesToNewUsers(
        userId: UserId,
        shareId: ShareId,
        inviteTargets: List<InviteTarget>
    )

    fun observeInviteRecommendations(
        userId: UserId,
        shareId: ShareId,
        lastToken: String? = null,
        startsWith: String? = null
    ): Flow<InviteRecommendations>

}
