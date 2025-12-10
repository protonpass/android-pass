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

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import me.proton.core.domain.entity.UserId
import me.proton.core.network.data.ApiProvider
import me.proton.core.network.domain.ApiResult
import proton.android.pass.data.api.errors.CannotCreateMoreVaultsError
import proton.android.pass.data.api.errors.ErrorCodes
import proton.android.pass.data.api.errors.FreeUserInviteError
import proton.android.pass.data.api.errors.UserAlreadyInviteError
import proton.android.pass.data.api.errors.getProtonErrorCode
import proton.android.pass.data.impl.api.PasswordManagerApi
import proton.android.pass.data.impl.requests.CreateInvitesRequest
import proton.android.pass.data.impl.requests.CreateNewUserInvitesRequest
import proton.android.pass.data.impl.requests.invites.AcceptInviteRequest
import proton.android.pass.data.impl.responses.InviteRecommendationsOrganizationResponse
import proton.android.pass.data.impl.responses.InviteRecommendationsSuggestedResponse
import proton.android.pass.data.impl.responses.PendingUserInviteResponse
import proton.android.pass.data.impl.responses.ShareResponse
import proton.android.pass.domain.InviteToken
import proton.android.pass.domain.ShareId
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

class RemoteUserInviteDataSourceImpl @Inject constructor(
    private val apiProvider: ApiProvider
) : RemoteUserInviteDataSource {

    @Suppress("TooGenericExceptionCaught")
    override suspend fun sendInvites(
        userId: UserId,
        shareId: ShareId,
        existingUserRequests: CreateInvitesRequest,
        newUserRequests: CreateNewUserInvitesRequest
    ) {
        coroutineScope {
            val existingUsers = async {
                runCatching { sendInvitesToExistingUsers(userId, shareId, existingUserRequests) }
            }
            existingUsers.invokeOnCompletion {
                PassLogger.d(TAG, "Existing users invites completed")
            }

            val newUsers = async {
                runCatching { sendInvitesToNewUsers(userId, shareId, newUserRequests) }
            }
            newUsers.invokeOnCompletion {
                PassLogger.d(TAG, "New users invites completed")
            }

            val results: List<Result<Unit>> = awaitAll(existingUsers, newUsers)
            results.forEach { it.getOrThrow() }
        }
    }

    @Suppress("TooGenericExceptionCaught")
    override suspend fun sendInvitesToExistingUsers(
        userId: UserId,
        shareId: ShareId,
        existingUserRequests: CreateInvitesRequest
    ) {
        if (existingUserRequests.invites.isNotEmpty()) {
            val api = apiProvider.get<PasswordManagerApi>(userId)
            runCatching {
                api.invoke {
                    inviteUsers(shareId.id, existingUserRequests)
                }.valueOrThrow
            }.onFailure { error ->
                val reason = when (error.getProtonErrorCode()) {
                    ErrorCodes.FREE_USER_INVITED -> FreeUserInviteError(error.message)
                    ErrorCodes.USER_ALREADY_INVITED -> UserAlreadyInviteError(error.message)
                    else -> error
                }
                throw reason
            }.getOrThrow()
        }
    }

    override suspend fun sendInvitesToNewUsers(
        userId: UserId,
        shareId: ShareId,
        newUserRequests: CreateNewUserInvitesRequest
    ) {
        if (newUserRequests.invites.isNotEmpty()) {
            val api = apiProvider.get<PasswordManagerApi>(userId)
            val res = api.invoke { inviteNewUsers(shareId.id, newUserRequests) }
            res.exceptionOrNull?.let { throw it }
        }
    }

    override suspend fun fetchInvites(userId: UserId): List<PendingUserInviteResponse> =
        apiProvider.get<PasswordManagerApi>(userId)
            .invoke { fetchUserInvites() }
            .valueOrThrow
            .invites

    override suspend fun acceptInvite(
        userId: UserId,
        inviteToken: InviteToken,
        body: AcceptInviteRequest
    ): ShareResponse {
        val res = apiProvider.get<PasswordManagerApi>(userId)
            .invoke {
                acceptInvite(
                    inviteId = inviteToken.value,
                    request = body
                )
            }

        when (res) {
            is ApiResult.Success -> return res.value.share
            is ApiResult.Error -> {
                if (res is ApiResult.Error.Http) {
                    if (res.proton?.code == CODE_CANNOT_CREATE_MORE_VAULTS) {
                        throw CannotCreateMoreVaultsError()
                    }
                }
                throw res.cause ?: Exception("Create vault failed")
            }
        }

    }


    override suspend fun rejectInvite(userId: UserId, token: InviteToken) {
        apiProvider.get<PasswordManagerApi>(userId)
            .invoke {
                rejectInvite(inviteId = token.value)
            }
            .valueOrThrow
    }

    override suspend fun fetchInviteRecommendationsSuggested(
        userId: UserId,
        shareId: ShareId,
        startsWith: String?
    ): InviteRecommendationsSuggestedResponse = apiProvider.get<PasswordManagerApi>(userId)
        .invoke {
            inviteRecommendationsSuggested(shareId.id, startsWith)
        }
        .valueOrThrow

    override suspend fun fetchInviteRecommendationsOrganization(
        userId: UserId,
        shareId: ShareId,
        since: String?,
        pageSize: Int?,
        startsWith: String?
    ): InviteRecommendationsOrganizationResponse = apiProvider.get<PasswordManagerApi>(userId)
        .invoke {
            inviteRecommendationsOrganization(shareId.id, since, pageSize, startsWith)
        }
        .valueOrThrow

    @Suppress("UnderscoresInNumericLiterals")
    companion object {
        private const val TAG = "RemoteInviteDataSourceImpl"
        private const val CODE_CANNOT_CREATE_MORE_VAULTS = 300007
    }
}
