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
import proton.android.pass.data.impl.responses.invites.GroupApiModel
import proton.android.pass.data.impl.responses.invites.GroupMemberApiModel
import proton.android.pass.domain.GroupId
import javax.inject.Inject

class RemoteGroupsDataSourceImpl @Inject constructor(
    private val api: ApiProvider
) : RemoteGroupsDataSource {

    override suspend fun retrieveGroups(userId: UserId): List<GroupApiModel> = api.get<PasswordManagerApi>(userId)
        .invoke { retrieveGroups() }
        .valueOrThrow
        .groups

    override suspend fun retrieveGroupMembers(
        userId: UserId,
        groupId: GroupId,
        pageSize: Int,
        page: Int
    ): List<GroupMemberApiModel> = api.get<PasswordManagerApi>(userId)
        .invoke { retrieveGroupMembers(groupId.id, pageSize, page) }
        .valueOrThrow
        .members

}
