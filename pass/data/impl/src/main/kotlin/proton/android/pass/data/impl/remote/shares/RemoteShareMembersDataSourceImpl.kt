/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.data.impl.remote.shares

import me.proton.core.domain.entity.UserId
import me.proton.core.network.data.ApiProvider
import proton.android.pass.data.impl.api.PasswordManagerApi
import proton.android.pass.data.impl.requests.UpdateMemberShareRequest
import proton.android.pass.data.impl.responses.CodeOnlyResponse
import proton.android.pass.data.impl.responses.ShareMemberResponse
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareRole
import javax.inject.Inject

class RemoteShareMembersDataSourceImpl @Inject constructor(
    private val apiProvider: ApiProvider
) : RemoteShareMembersDataSource {

    override suspend fun getShareMembers(userId: UserId, shareId: ShareId): List<ShareMemberResponse> =
        apiProvider.get<PasswordManagerApi>(userId)
            .invoke { getShareMembers(shareId.id) }
            .valueOrThrow
            .members

    override suspend fun getShareItemMembers(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): List<ShareMemberResponse> = apiProvider.get<PasswordManagerApi>(userId)
        .invoke { getShareItemMembers(shareId.id, itemId.id) }
        .valueOrThrow
        .members

    override suspend fun updateShareMember(
        userId: UserId,
        shareId: ShareId,
        memberShareId: ShareId,
        memberShareRole: ShareRole
    ): CodeOnlyResponse = apiProvider.get<PasswordManagerApi>(userId)
        .invoke {
            updateShareMember(
                shareId = shareId.id,
                memberShareId = memberShareId.id,
                request = UpdateMemberShareRequest(
                    shareRoleId = memberShareRole.value
                )
            )
        }
        .valueOrThrow

    override suspend fun deleteShareMember(
        userId: UserId,
        shareId: ShareId,
        memberShareId: ShareId
    ): CodeOnlyResponse = apiProvider.get<PasswordManagerApi>(userId)
        .invoke {
            deleteShareMember(
                shareId = shareId.id,
                memberShareId = memberShareId.id
            )
        }
        .valueOrThrow

}
