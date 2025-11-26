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

import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.repositories.GroupRepository
import proton.android.pass.domain.Group
import proton.android.pass.domain.GroupId
import proton.android.pass.domain.GroupMember
import javax.inject.Inject

class FakeGroupRepository @Inject constructor() : GroupRepository {
    override suspend fun retrieveGroups(userId: UserId, forceRefresh: Boolean): List<Group> = emptyList()

    override suspend fun retrieveGroup(
        userId: UserId,
        groupId: GroupId,
        forceRefresh: Boolean
    ): Group? = null

    override suspend fun retrieveGroupMembers(
        userId: UserId,
        groupId: GroupId,
        forceRefresh: Boolean
    ): List<GroupMember> = emptyList()
}
