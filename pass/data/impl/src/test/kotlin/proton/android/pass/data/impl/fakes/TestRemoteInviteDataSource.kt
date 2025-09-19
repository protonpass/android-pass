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
import proton.android.pass.data.impl.requests.CreateInvitesRequest
import proton.android.pass.data.impl.requests.CreateNewUserInvitesRequest
import proton.android.pass.data.impl.responses.InviteRecommendationResponse
import proton.android.pass.data.impl.responses.PendingInviteResponse
import proton.android.pass.data.impl.responses.ShareResponse
import proton.android.pass.domain.InviteToken
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareRole
import javax.inject.Inject

class TestRemoteInviteDataSource @Inject constructor() : RemoteInviteDataSource {

    private var sendInviteResult: Result<Unit> = Result.success(Unit)
    private var fetchInvitesResult: Result<List<PendingInviteResponse>> =
        Result.success(emptyList())
    private var acceptInviteResult: Result<ShareResponse> = Result.success(DEFAULT_RESPONSE)
    private var inviteRecommendationResponseResult: Result<InviteRecommendationResponse> =
        Result.success(
            InviteRecommendationResponse(
                recommendedEmails = emptyList(),
                planInternalName = "",
                groupDisplayName = "",
                planRecommendedEmails = emptyList(),
                planRecommendedEmailsNextToken = ""
            )
        )

    private var memory: MutableList<InvitePayload> = mutableListOf()

    fun getInviteMemory(): List<InvitePayload> = memory

    fun setSendInviteResult(value: Result<Unit>) {
        sendInviteResult = value
    }

    fun setFetchInvitesResult(value: Result<List<PendingInviteResponse>>) {
        fetchInvitesResult = value
    }

    fun setAcceptInviteResult(value: Result<ShareResponse>) {
        acceptInviteResult = value
    }

    fun setInviteRecommendationResponseResult(value: Result<InviteRecommendationResponse>) {
        inviteRecommendationResponseResult = value
    }

    override suspend fun sendInvites(
        userId: UserId,
        shareId: ShareId,
        existingUserRequests: CreateInvitesRequest,
        newUserRequests: CreateNewUserInvitesRequest
    ) {
        memory.add(InvitePayload(userId, shareId, existingUserRequests, newUserRequests))
        sendInviteResult.getOrThrow()
    }

    override suspend fun fetchInvites(userId: UserId): List<PendingInviteResponse> = fetchInvitesResult.getOrThrow()

    override suspend fun acceptInvite(
        userId: UserId,
        inviteToken: InviteToken,
        body: AcceptInviteRequest
    ): ShareResponse = acceptInviteResult.getOrThrow()

    override suspend fun rejectInvite(userId: UserId, token: InviteToken) {
    }

    override suspend fun fetchInviteRecommendations(
        userId: UserId,
        shareId: ShareId,
        lastToken: String?,
        startsWith: String?
    ): InviteRecommendationResponse = inviteRecommendationResponseResult.getOrThrow()

    data class InvitePayload(
        val userId: UserId,
        val shareId: ShareId,
        val existingRequests: CreateInvitesRequest,
        val newUserRequests: CreateNewUserInvitesRequest
    )

    companion object {
        val DEFAULT_RESPONSE = ShareResponse(
            shareId = "TestRemoteInviteDataSource-ShareId",
            vaultId = "TestRemoteInviteDataSource-VaultId",
            addressId = "TestRemoteInviteDataSource-AddressId",
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
            targetMaxMembers = 2,
            newUserInvitesReady = 0,
            pendingInvites = 0,
            canAutofill = true,
            flags = 0
        )
    }
}
