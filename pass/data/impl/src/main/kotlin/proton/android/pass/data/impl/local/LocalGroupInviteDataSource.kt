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
import proton.android.pass.data.impl.db.entities.GroupInviteEntity
import proton.android.pass.data.impl.db.entities.GroupInviteKeyEntity
import proton.android.pass.domain.InviteId

data class GroupInviteAndKeysEntity(
    val groupInviteEntity: GroupInviteEntity,
    val inviteKeys: List<GroupInviteKeyEntity>
)

interface LocalGroupInviteDataSource {

    suspend fun storeInvites(invites: List<GroupInviteAndKeysEntity>)

    suspend fun removeInvites(invites: List<GroupInviteEntity>)

    fun observeAllInvites(userId: UserId): Flow<List<GroupInviteEntity>>

    fun observeInvite(userId: UserId, inviteId: InviteId): Flow<GroupInviteEntity?>

    suspend fun getInviteWithKeys(userId: UserId, inviteId: InviteId): GroupInviteAndKeysEntity?

    suspend fun removeInvite(userId: UserId, inviteId: InviteId)

}
