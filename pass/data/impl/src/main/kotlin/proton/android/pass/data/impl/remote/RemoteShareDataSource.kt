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

package proton.android.pass.data.impl.remote

import me.proton.core.domain.entity.UserId
import proton.android.pass.data.impl.requests.CreateVaultRequest
import proton.android.pass.data.impl.requests.UpdateVaultRequest
import proton.android.pass.data.impl.responses.CodeOnlyResponse
import proton.android.pass.data.impl.responses.GetSharePendingInvitesResponse
import proton.android.pass.data.impl.responses.ShareMemberResponse
import proton.android.pass.data.impl.responses.ShareResponse
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareRole

interface RemoteShareDataSource {

    suspend fun createVault(userId: UserId, body: CreateVaultRequest): ShareResponse

    suspend fun updateVault(
        userId: UserId,
        shareId: ShareId,
        body: UpdateVaultRequest
    ): ShareResponse

    suspend fun deleteVault(userId: UserId, shareId: ShareId)

    suspend fun getShares(userId: UserId): List<ShareResponse>

    suspend fun fetchShareById(userId: UserId, shareId: ShareId): ShareResponse?

    suspend fun markAsPrimary(userId: UserId, shareId: ShareId)

    suspend fun leaveVault(userId: UserId, shareId: ShareId)

    suspend fun getShareMembers(userId: UserId, shareId: ShareId): List<ShareMemberResponse>

    suspend fun getShareItemMembers(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): List<ShareMemberResponse>

    suspend fun getSharePendingInvites(userId: UserId, shareId: ShareId): GetSharePendingInvitesResponse

    suspend fun removeShareMember(
        userId: UserId,
        shareId: ShareId,
        memberShareId: ShareId
    ): CodeOnlyResponse

    suspend fun updateShareMember(
        userId: UserId,
        shareId: ShareId,
        memberShareId: ShareId,
        memberShareRole: ShareRole
    ): CodeOnlyResponse

}
