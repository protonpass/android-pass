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
import proton.android.pass.data.api.repositories.ShareMembersRepository
import proton.android.pass.data.impl.local.shares.LocalShareMembersDataSource
import proton.android.pass.data.impl.remote.shares.RemoteShareMembersDataSource
import proton.android.pass.data.impl.responses.ShareMemberResponse
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareRole
import proton.android.pass.domain.ShareType
import proton.android.pass.domain.shares.ShareMember
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

class ShareMembersRepositoryImpl @Inject constructor(
    private val remoteDataSource: RemoteShareMembersDataSource,
    private val localDataSource: LocalShareMembersDataSource
) : ShareMembersRepository {

    override suspend fun getShareMembers(
        userId: UserId,
        shareId: ShareId,
        userEmail: String?
    ): List<ShareMember> = remoteDataSource.getShareMembers(userId, shareId)
        .map { shareMemberResponse ->
            shareMemberResponse.toDomain(userEmail)
        }

    override fun observeShareItemMembers(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        userEmail: String?
    ): Flow<List<ShareMember>> = flow {
        remoteDataSource.getShareItemMembers(userId, shareId, itemId)
            .map { shareMemberResponse ->
                shareMemberResponse.toDomain(userEmail)
            }
            .also { shareMembers ->
                localDataSource.upsertShareMembers(userId, shareId, shareMembers)
            }

        emitAll(localDataSource.observeShareMembers(userId, shareId))
    }

    override suspend fun updateShareMember(
        userId: UserId,
        shareId: ShareId,
        memberShareId: ShareId,
        memberShareRole: ShareRole
    ) {
        runCatching {
            remoteDataSource.updateShareMember(userId, shareId, memberShareId, memberShareRole)
        }.onFailure { error ->
            PassLogger.w(TAG, "There was an error removing a share member")
            PassLogger.w(TAG, error)
            throw error
        }.onSuccess {
            localDataSource.getShareMember(userId, shareId, memberShareId)
                ?.also { currentShareMember ->
                    localDataSource.upsertShareMember(
                        userId = userId,
                        shareId = shareId,
                        shareMember = currentShareMember.copy(role = memberShareRole)
                    )
                }
        }
    }

    override suspend fun deleteShareMember(
        userId: UserId,
        shareId: ShareId,
        memberShareId: ShareId
    ) {
        runCatching {
            remoteDataSource.deleteShareMember(userId, shareId, memberShareId)
        }.onFailure { error ->
            PassLogger.w(TAG, "There was an error removing a share member")
            PassLogger.w(TAG, error)
            throw error
        }.onSuccess {
            localDataSource.deleteShareMember(userId, shareId, memberShareId)
        }
    }

    private companion object {

        private const val TAG = "ShareMembersRepository"

    }

}

private fun ShareMemberResponse.toDomain(currentUserEmail: String?): ShareMember = ShareMember(
    email = userEmail,
    shareId = ShareId(shareId),
    username = userName,
    isCurrentUser = userEmail == currentUserEmail,
    isOwner = owner ?: false,
    role = shareRoleId
        ?.let(ShareRole::fromValue)
        ?: ShareRole.fromValue(ShareRole.SHARE_ROLE_READ),
    shareType = ShareType.from(targetType)
)
