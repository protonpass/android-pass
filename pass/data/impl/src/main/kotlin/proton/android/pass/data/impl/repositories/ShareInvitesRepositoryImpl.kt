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

package proton.android.pass.data.impl.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.errors.CannotSendMoreInvitesError
import proton.android.pass.data.api.errors.ErrorCodes
import proton.android.pass.data.api.errors.getProtonErrorCode
import proton.android.pass.data.api.repositories.ShareInvitesRepository
import proton.android.pass.data.impl.local.shares.LocalShareInvitesDataSource
import proton.android.pass.data.impl.remote.shares.RemoteShareInvitesDataSource
import proton.android.pass.domain.InviteId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareRole
import proton.android.pass.domain.shares.SharePendingInvite
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

class ShareInvitesRepositoryImpl @Inject constructor(
    private val remoteDataSource: RemoteShareInvitesDataSource,
    private val localDataSource: LocalShareInvitesDataSource
) : ShareInvitesRepository {

    override fun observeSharePendingInvites(userId: UserId, shareId: ShareId): Flow<List<SharePendingInvite>> = flow {
        remoteDataSource.getSharePendingInvites(userId, shareId)
            .let { sharePendingInviteResponse ->
                buildList {
                    sharePendingInviteResponse.invites
                        .map { actualUserPendingInvite ->
                            SharePendingInvite.ExistingUser(
                                email = actualUserPendingInvite.invitedEmail,
                                inviteId = InviteId(actualUserPendingInvite.inviteId)
                            )
                        }
                        .also(::addAll)

                    sharePendingInviteResponse.newUserInvites
                        .map { newUserPendingInvite ->
                            SharePendingInvite.NewUser(
                                email = newUserPendingInvite.invitedEmail,
                                inviteId = InviteId(newUserPendingInvite.newUserInviteId),
                                role = ShareRole.fromValue(newUserPendingInvite.shareRoleId),
                                inviteState = SharePendingInvite.NewUser.InviteState.fromValue(
                                    value = newUserPendingInvite.state
                                )
                            )
                        }
                        .also(::addAll)
                }.also { sharePendingInvites ->
                    localDataSource.upsertSharePendingInvites(userId, shareId, sharePendingInvites)
                }
            }

        emitAll(localDataSource.observeSharePendingInvites(userId, shareId))
    }

    override suspend fun deleteSharePendingInvite(
        userId: UserId,
        shareId: ShareId,
        inviteId: InviteId
    ) {
        localDataSource.getSharePendingInvite(userId, shareId, inviteId)
            ?.also { sharePendingInvite ->
                runCatching {
                    remoteDataSource.deleteSharePendingInvite(
                        userId = userId,
                        shareId = shareId,
                        inviteId = inviteId,
                        isForNewUser = sharePendingInvite.isForNewUser
                    )
                }.onFailure { error ->
                    PassLogger.w(TAG, "There was an error deleting share pending invite")
                    PassLogger.w(TAG, error)

                    throw error
                }.onSuccess {
                    localDataSource.deleteSharePendingInvite(userId, shareId, inviteId)
                }
            }
    }

    override suspend fun resendShareInvite(
        userId: UserId,
        shareId: ShareId,
        inviteId: InviteId
    ) {
        runCatching {
            remoteDataSource.resendShareInvite(userId, shareId, inviteId)
        }.onFailure { error ->
            PassLogger.w(TAG, "There was an error re-sending share pending invite")
            PassLogger.w(TAG, error)

            if (error.getProtonErrorCode() == ErrorCodes.RESEND_SHARE_INVITE_LIMIT) {
                throw CannotSendMoreInvitesError()
            }
            throw error
        }
    }

    private companion object {

        private const val TAG = "ShareInvitesRepositoryImpl"

    }

}
