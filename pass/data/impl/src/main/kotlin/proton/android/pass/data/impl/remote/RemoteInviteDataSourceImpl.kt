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

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import me.proton.core.domain.entity.UserId
import me.proton.core.network.data.ApiProvider
import me.proton.core.network.domain.ApiResult
import proton.android.pass.data.api.errors.CannotCreateMoreVaultsError
import proton.android.pass.data.api.errors.ErrorCodes
import proton.android.pass.data.api.errors.FreeUserInviteError
import proton.android.pass.data.api.errors.UserAlreadyInviteError
import proton.android.pass.data.api.errors.getProtonErrorCode
import proton.android.pass.data.impl.api.PasswordManagerApi
import proton.android.pass.data.impl.requests.AcceptInviteRequest
import proton.android.pass.data.impl.requests.CreateInvitesRequest
import proton.android.pass.data.impl.requests.CreateNewUserInvitesRequest
import proton.android.pass.data.impl.responses.InviteRecommendationResponse
import proton.android.pass.data.impl.responses.PendingInviteResponse
import proton.android.pass.data.impl.responses.ShareResponse
import proton.android.pass.domain.InviteToken
import proton.android.pass.domain.ShareId
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

class RemoteInviteDataSourceImpl @Inject constructor(
    private val apiProvider: ApiProvider
) : RemoteInviteDataSource {

    @Suppress("TooGenericExceptionCaught")
    override suspend fun sendInvites(
        userId: UserId,
        shareId: ShareId,
        existingUserRequests: CreateInvitesRequest,
        newUserRequests: CreateNewUserInvitesRequest
    ) = withContext(Dispatchers.IO) {
        val api = apiProvider.get<PasswordManagerApi>(userId)

        val existingUsers = async {
            if (existingUserRequests.invites.isNotEmpty()) {
                try {
                    api.invoke {
                        inviteUsers(shareId.id, existingUserRequests)
                    }.valueOrThrow

                    Result.success(Unit)
                } catch (error: Throwable) {
                    val reason = when (error.getProtonErrorCode()) {
                        ErrorCodes.FREE_USER_INVITED -> FreeUserInviteError(error.message)
                        ErrorCodes.USER_ALREADY_INVITED -> UserAlreadyInviteError(error.message)
                        else -> error
                    }

                    Result.failure(reason)
                }
            } else {
                Result.success(Unit)
            }
        }
        existingUsers.invokeOnCompletion {
            PassLogger.d(TAG, "Existing users invites completed")
        }

        val newUsers = async {
            if (newUserRequests.invites.isNotEmpty()) {
                val res = api.invoke { inviteNewUsers(shareId.id, newUserRequests) }
                res.exceptionOrNull?.let { Result.failure(it) } ?: Result.success(Unit)
            } else {
                Result.success(Unit)
            }
        }
        newUsers.invokeOnCompletion {
            PassLogger.d(TAG, "New users invites completed")
        }

        val results: List<Result<Unit>> = awaitAll(existingUsers, newUsers)
        results.forEach { it.getOrThrow() }
    }

    override suspend fun fetchInvites(userId: UserId): List<PendingInviteResponse> =
        apiProvider.get<PasswordManagerApi>(userId)
            .invoke {
                fetchInvites()
            }
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

    override suspend fun fetchInviteRecommendations(
        userId: UserId,
        shareId: ShareId,
        lastToken: String?,
        startsWith: String?
    ): InviteRecommendationResponse = apiProvider.get<PasswordManagerApi>(userId)
        .invoke {
            inviteRecommendations(shareId.id, lastToken, startsWith).recommendation
        }
        .valueOrThrow

    @Suppress("UnderscoresInNumericLiterals")
    companion object {
        private const val TAG = "RemoteInviteDataSourceImpl"
        private const val CODE_CANNOT_CREATE_MORE_VAULTS = 300007
    }
}
