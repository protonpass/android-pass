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
import proton.android.pass.data.impl.db.entities.InviteEntity
import proton.android.pass.data.impl.db.entities.InviteKeyEntity
import proton.android.pass.domain.InviteToken

data class InviteAndKeysEntity(
    val inviteEntity: InviteEntity,
    val inviteKeys: List<InviteKeyEntity>
)

interface LocalInviteDataSource {

    suspend fun storeInvites(invites: List<InviteAndKeysEntity>)

    suspend fun removeInvites(invites: List<InviteEntity>)

    suspend fun removeInvite(userId: UserId, invite: InviteToken)

    suspend fun getInvite(userId: UserId, inviteToken: InviteToken): Option<InviteEntity>

    fun observeAllInvites(userId: UserId): Flow<List<InviteEntity>>

    suspend fun getInviteWithKeys(userId: UserId, inviteToken: InviteToken): Option<InviteAndKeysEntity>

}
