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
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.toOption
import proton.android.pass.data.impl.db.PassDatabase
import proton.android.pass.data.impl.db.entities.UserInviteEntity
import proton.android.pass.domain.InviteToken
import javax.inject.Inject

class LocalUserInviteDataSourceImpl @Inject constructor(
    private val database: PassDatabase
) : LocalUserInviteDataSource {

    override suspend fun getInvite(userId: UserId, inviteToken: InviteToken): Option<UserInviteEntity> =
        database.userInviteDao()
            .getByToken(userId = userId.id, token = inviteToken.value)
            .toOption()

    override fun observeAllInvites(userId: UserId): Flow<List<UserInviteEntity>> =
        database.userInviteDao().observeAllForUser(userId.id)

    override suspend fun getInviteWithKeys(userId: UserId, inviteToken: InviteToken): Option<UserInviteAndKeysEntity> {
        val invite = database.userInviteDao().getByToken(userId = userId.id, token = inviteToken.value)
            ?: return None

        val keys = database.userInviteKeyDao().getAllByToken(invite.token)
        if (keys.isEmpty()) {
            throw IllegalStateException("Invite has no keys [token=${inviteToken.value}]")
        }

        return UserInviteAndKeysEntity(invite, keys).toOption()
    }

    override suspend fun storeInvites(invites: List<UserInviteAndKeysEntity>) {
        database.inTransaction("storeInvites") {
            invites.forEach {
                database.userInviteDao().insertOrUpdate(it.userInviteEntity)
                database.userInviteKeyDao().insertOrUpdate(*it.inviteKeys.toTypedArray())
            }
        }
    }

    override suspend fun removeInvites(invites: List<UserInviteEntity>) {
        database.userInviteDao().delete(*invites.toTypedArray())
    }

    override suspend fun removeInvite(userId: UserId, invite: InviteToken) {
        database.userInviteDao().removeByToken(userId.id, invite.value)
    }

}
