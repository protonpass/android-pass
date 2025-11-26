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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.AppDispatchers
import proton.android.pass.data.impl.db.PassDatabase
import proton.android.pass.data.impl.db.entities.GroupInviteEntity
import proton.android.pass.domain.InviteId
import javax.inject.Inject

class LocalGroupInviteDataSourceImpl @Inject constructor(
    private val database: PassDatabase,
    private val appDispatchers: AppDispatchers
) : LocalGroupInviteDataSource {

    override fun observeAllInvites(userId: UserId): Flow<List<GroupInviteEntity>> =
        database.groupInviteDao().observeAllForUser(userId.id)

    override fun observeInvite(userId: UserId, inviteId: InviteId): Flow<GroupInviteEntity?> =
        database.groupInviteDao().observeInviteForUser(userId.id, inviteId.value)

    override suspend fun getInviteWithKeys(userId: UserId, inviteId: InviteId): GroupInviteAndKeysEntity? =
        withContext(appDispatchers.io) {
            val invite = database.groupInviteDao()
                .observeInviteForUser(userId = userId.id, inviteId = inviteId.value)
                .first()
                ?: return@withContext null

            val keys = database.groupInviteKeyDao().getAllById(inviteId.value)
            if (keys.isEmpty()) {
                throw IllegalStateException("Invite has no keys [id=${inviteId.value}]")
            }

            GroupInviteAndKeysEntity(invite, keys)
        }

    override suspend fun removeInvite(userId: UserId, inviteId: InviteId) {
        database.groupInviteDao().removeInvite(userId.id, inviteId.value)
    }

    override suspend fun storeInvites(invites: List<GroupInviteAndKeysEntity>) {
        database.inTransaction("storeGroupInvites") {
            invites.forEach {
                database.groupInviteDao().insertOrUpdate(it.groupInviteEntity)
                database.groupInviteKeyDao().insertOrUpdate(*it.inviteKeys.toTypedArray())
            }
        }
    }

    override suspend fun removeInvites(invites: List<GroupInviteEntity>) {
        database.groupInviteDao().delete(*invites.toTypedArray())
    }
}
