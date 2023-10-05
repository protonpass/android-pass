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

package proton.android.pass.data.impl.fakes

import me.proton.core.domain.entity.UserId
import proton.android.pass.data.impl.remote.RemoteInviteDataSource
import proton.android.pass.data.impl.requests.AcceptInviteRequest
import proton.android.pass.data.impl.requests.CreateInviteRequest
import proton.android.pass.data.impl.responses.PendingInviteResponse
import proton.android.pass.data.impl.responses.ShareResponse
import proton.pass.domain.InviteToken
import proton.pass.domain.ShareId
import proton.pass.domain.ShareRole
import javax.inject.Inject

class TestRemoteInviteDataSource @Inject constructor() : RemoteInviteDataSource {

    private var sendInviteResult: Result<Unit> = Result.success(Unit)
    private var fetchInvitesResult: Result<List<PendingInviteResponse>> =
        Result.success(emptyList())
    private var acceptInviteResult: Result<ShareResponse> = Result.success(DEFAULT_RESPONSE)

    private var memory: MutableList<Payload> = mutableListOf()

    fun getMemory(): List<Payload> = memory

    fun setSendInviteResult(value: Result<Unit>) {
        sendInviteResult = value
    }

    fun setFetchInvitesResult(value: Result<List<PendingInviteResponse>>) {
        fetchInvitesResult = value
    }

    fun setAcceptInviteResult(value: Result<ShareResponse>) {
        acceptInviteResult = value
    }

    override suspend fun sendInvite(
        userId: UserId,
        shareId: ShareId,
        request: CreateInviteRequest
    ) {
        memory.add(Payload(userId, shareId, request))
        sendInviteResult.getOrThrow()
    }

    override suspend fun fetchInvites(userId: UserId): List<PendingInviteResponse> =
        fetchInvitesResult.getOrThrow()

    override suspend fun acceptInvite(
        userId: UserId,
        inviteToken: InviteToken,
        body: AcceptInviteRequest
    ): ShareResponse = acceptInviteResult.getOrThrow()

    override suspend fun rejectInvite(userId: UserId, token: InviteToken) {
    }

    data class Payload(
        val userId: UserId,
        val shareId: ShareId,
        val request: CreateInviteRequest
    )

    companion object {
        val DEFAULT_RESPONSE = ShareResponse(
            shareId = "TestRemoteInviteDataSource-ShareId",
            vaultId = "TestRemoteInviteDataSource-VaultId",
            addressId = "TestRemoteInviteDataSource-AddressId",
            primary = true,
            targetType = 1,
            targetId = "TestRemoteInviteDataSource-VaultId",
            permission = 0,
            content = null,
            contentKeyRotation = null,
            contentFormatVersion = null,
            shareRoleId = ShareRole.Admin.value,
            targetMembers = 1,
            owner = true,
            shared = false,
            expirationTime = null,
            createTime = 12_345_678,
            targetMaxMembers = 2
        )
    }
}
