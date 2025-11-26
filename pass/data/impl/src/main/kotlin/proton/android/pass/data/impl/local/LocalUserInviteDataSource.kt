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

package proton.android.pass.data.impl.local

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.Option
import proton.android.pass.data.impl.db.entities.UserInviteEntity
import proton.android.pass.data.impl.db.entities.UserInviteKeyEntity
import proton.android.pass.domain.InviteToken

data class UserInviteAndKeysEntity(
    val userInviteEntity: UserInviteEntity,
    val inviteKeys: List<UserInviteKeyEntity>
)

interface LocalUserInviteDataSource {

    suspend fun storeInvites(invites: List<UserInviteAndKeysEntity>)

    suspend fun removeInvites(invites: List<UserInviteEntity>)

    suspend fun removeInvite(userId: UserId, invite: InviteToken)

    suspend fun getInvite(userId: UserId, inviteToken: InviteToken): Option<UserInviteEntity>

    fun observeAllInvites(userId: UserId): Flow<List<UserInviteEntity>>

    suspend fun getInviteWithKeys(userId: UserId, inviteToken: InviteToken): Option<UserInviteAndKeysEntity>

}
