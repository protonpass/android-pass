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
import proton.android.pass.data.impl.remote.RemoteUserInviteDataSource
import proton.android.pass.data.impl.requests.CreateInvitesRequest
import proton.android.pass.data.impl.requests.CreateNewUserInvitesRequest
import proton.android.pass.data.impl.requests.invites.AcceptInviteRequest
import proton.android.pass.data.impl.responses.InviteRecommendationsOrganizationResponse
import proton.android.pass.data.impl.responses.InviteRecommendationsSuggestedResponse
import proton.android.pass.data.impl.responses.OrganizationRecommendation
import proton.android.pass.data.impl.responses.PendingUserInviteResponse
import proton.android.pass.data.impl.responses.ShareResponse
import proton.android.pass.domain.InviteToken
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareRole
import proton.android.pass.domain.events.EventToken
import javax.inject.Inject

class TestRemoteUserInviteDataSource @Inject constructor() : RemoteUserInviteDataSource {

    private var sendInviteResult: Result<Unit> = Result.success(Unit)
    private var fetchInvitesResult: Result<List<PendingUserInviteResponse>> =
        Result.success(emptyList())
    private var acceptInviteResult: Result<ShareResponse> = Result.success(DEFAULT_RESPONSE)
    private var inviteRecommendationsSuggestedResult: Result<InviteRecommendationsSuggestedResponse> =
        Result.success(
            InviteRecommendationsSuggestedResponse(
                suggested = emptyList(),
                code = 1000
            )
        )
    private var inviteRecommendationsOrganizationResult: Result<InviteRecommendationsOrganizationResponse> =
        Result.success(
            InviteRecommendationsOrganizationResponse(
                code = 1000,
                recommendation = OrganizationRecommendation(
                    groupDisplayName = null,
                    nextToken = null,
                    entries = emptyList()
                )
            )
        )

    private var memory: MutableList<InvitePayload> = mutableListOf()
    private var existingUsersMemory: MutableList<ExistingUsersInvitePayload> = mutableListOf()
    private var newUsersMemory: MutableList<NewUsersInvitePayload> = mutableListOf()

    fun getInviteMemory(): List<InvitePayload> = memory

    fun getExistingUsersInviteMemory(): List<ExistingUsersInvitePayload> = existingUsersMemory

    fun getNewUsersInviteMemory(): List<NewUsersInvitePayload> = newUsersMemory

    fun setSendInviteResult(value: Result<Unit>) {
        sendInviteResult = value
    }

    fun setFetchInvitesResult(value: Result<List<PendingUserInviteResponse>>) {
        fetchInvitesResult = value
    }

    fun setAcceptInviteResult(value: Result<ShareResponse>) {
        acceptInviteResult = value
    }

    fun setInviteRecommendationsSuggestedResult(value: Result<InviteRecommendationsSuggestedResponse>) {
        inviteRecommendationsSuggestedResult = value
    }

    fun setInviteRecommendationsOrganizationResult(value: Result<InviteRecommendationsOrganizationResponse>) {
        inviteRecommendationsOrganizationResult = value
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

    override suspend fun sendInvitesToExistingUsers(
        userId: UserId,
        shareId: ShareId,
        existingUserRequests: CreateInvitesRequest
    ) {
        existingUsersMemory.add(ExistingUsersInvitePayload(userId, shareId, existingUserRequests))
        sendInviteResult.getOrThrow()
    }

    override suspend fun sendInvitesToNewUsers(
        userId: UserId,
        shareId: ShareId,
        newUserRequests: CreateNewUserInvitesRequest
    ) {
        newUsersMemory.add(NewUsersInvitePayload(userId, shareId, newUserRequests))
        sendInviteResult.getOrThrow()
    }

    override suspend fun fetchInvites(userId: UserId, eventToken: EventToken?): List<PendingUserInviteResponse> =
        fetchInvitesResult.getOrThrow()

    override suspend fun acceptInvite(
        userId: UserId,
        inviteToken: InviteToken,
        body: AcceptInviteRequest
    ): ShareResponse = acceptInviteResult.getOrThrow()

    override suspend fun rejectInvite(userId: UserId, token: InviteToken) {
    }

    override suspend fun fetchInviteRecommendationsSuggested(
        userId: UserId,
        shareId: ShareId,
        startsWith: String?
    ): InviteRecommendationsSuggestedResponse = inviteRecommendationsSuggestedResult.getOrThrow()

    override suspend fun fetchInviteRecommendationsOrganization(
        userId: UserId,
        shareId: ShareId,
        since: String?,
        pageSize: Int?,
        startsWith: String?
    ): InviteRecommendationsOrganizationResponse = inviteRecommendationsOrganizationResult.getOrThrow()

    data class InvitePayload(
        val userId: UserId,
        val shareId: ShareId,
        val existingRequests: CreateInvitesRequest,
        val newUserRequests: CreateNewUserInvitesRequest
    )

    data class ExistingUsersInvitePayload(
        val userId: UserId,
        val shareId: ShareId,
        val existingUserRequests: CreateInvitesRequest
    )

    data class NewUsersInvitePayload(
        val userId: UserId,
        val shareId: ShareId,
        val newUserRequests: CreateNewUserInvitesRequest
    )

    companion object {
        val DEFAULT_RESPONSE = ShareResponse(
            shareId = "TestRemoteInviteDataSource-ShareId",
            vaultId = "TestRemoteInviteDataSource-VaultId",
            groupId = "TestRemoteInviteDataSource-GroupId",
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
