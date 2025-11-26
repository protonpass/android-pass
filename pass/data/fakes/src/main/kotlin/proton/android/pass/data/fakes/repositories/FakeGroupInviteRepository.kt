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

package proton.android.pass.data.fakes.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.repositories.GroupInviteRepository
import proton.android.pass.domain.InviteId
import proton.android.pass.domain.InviteToken
import proton.android.pass.domain.PendingInvite
import javax.inject.Inject

class FakeGroupInviteRepository @Inject constructor() : GroupInviteRepository {
    override fun observePendingGroupInvites(userId: UserId, forceRefresh: Boolean): Flow<List<PendingInvite>> =
        flowOf(emptyList())

    override fun observePendingGroupInvite(userId: UserId, inviteId: InviteId): Flow<PendingInvite?> = flowOf(null)

    override suspend fun acceptGroupInvite(
        userId: UserId,
        inviteId: InviteId,
        inviteToken: InviteToken
    ) {
    }

    override suspend fun rejectGroupInvite(userId: UserId, inviteToken: InviteToken) {
    }
}
