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
import me.proton.core.network.data.ApiProvider
import me.proton.core.network.domain.ApiResult
import proton.android.pass.data.api.errors.CannotCreateMoreVaultsError
import proton.android.pass.data.impl.api.PasswordManagerApi
import proton.android.pass.data.impl.requests.AcceptInviteRequest
import proton.android.pass.data.impl.requests.CreateInviteRequest
import proton.android.pass.data.impl.requests.CreateNewUserInviteRequest
import proton.android.pass.data.impl.responses.PendingInviteResponse
import proton.android.pass.data.impl.responses.ShareResponse
import proton.android.pass.domain.InviteToken
import proton.android.pass.domain.ShareId
import javax.inject.Inject

class RemoteInviteDataSourceImpl @Inject constructor(
    private val apiProvider: ApiProvider
) : RemoteInviteDataSource {
    override suspend fun sendInvite(
        userId: UserId,
        shareId: ShareId,
        request: CreateInviteRequest
    ) {
        apiProvider.get<PasswordManagerApi>(userId)
            .invoke {
                inviteUser(
                    shareId = shareId.id,
                    request = request
                )
            }
            .valueOrThrow
    }

    override suspend fun sendNewUserInvite(
        userId: UserId,
        shareId: ShareId,
        request: CreateNewUserInviteRequest
    ) {
        apiProvider.get<PasswordManagerApi>(userId)
            .invoke {
                inviteNewUser(
                    shareId = shareId.id,
                    request = request
                )
            }
            .valueOrThrow
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

    @Suppress("UnderscoresInNumericLiterals")
    companion object {
        private const val CODE_CANNOT_CREATE_MORE_VAULTS = 300007
    }
}
