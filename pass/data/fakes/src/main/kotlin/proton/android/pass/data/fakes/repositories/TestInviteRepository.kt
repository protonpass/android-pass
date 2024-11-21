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

package proton.android.pass.data.fakes.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.data.api.repositories.InviteRepository
import proton.android.pass.domain.InviteRecommendations
import proton.android.pass.domain.InviteToken
import proton.android.pass.domain.PendingInvite
import proton.android.pass.domain.ShareId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestInviteRepository @Inject constructor() : InviteRepository {

    private val invitesFlow: MutableStateFlow<List<PendingInvite>> = MutableStateFlow(emptyList())
    private var refreshResult: Result<Boolean> = Result.success(false)
    private var acceptResult: Result<ShareId> = Result.success(DEFAULT_SHARE_ID)
    private var rejectResult: Result<Unit> = Result.success(Unit)
    private var inviteRecommendationsResult: Result<InviteRecommendations> = Result.success(
        InviteRecommendations(
            recommendedEmails = emptyList(),
            planInternalName = "",
            groupDisplayName = "",
            planRecommendedEmails = emptyList()
        )
    )

    fun emitInvites(invites: List<PendingInvite>) {
        invitesFlow.tryEmit(invites)
    }

    fun setRefreshResult(value: Result<Boolean>) {
        refreshResult = value
    }

    fun setAcceptResult(value: Result<ShareId>) {
        acceptResult = value
    }

    fun setRejectResult(value: Result<Unit>) {
        rejectResult = value
    }

    override suspend fun getInvite(userId: UserId, inviteToken: InviteToken): Option<PendingInvite> = None

    override fun observeInvites(userId: UserId): Flow<List<PendingInvite>> = invitesFlow

    override suspend fun refreshInvites(userId: UserId) = refreshResult.getOrThrow()

    override suspend fun acceptInvite(userId: UserId, inviteToken: InviteToken): ShareId = acceptResult.getOrThrow()

    override suspend fun rejectInvite(userId: UserId, inviteToken: InviteToken) {
        rejectResult.getOrThrow()
    }

    override fun observeInviteRecommendations(
        userId: UserId,
        shareId: ShareId,
        lastToken: String?,
        startsWith: String?
    ): Flow<InviteRecommendations> = flowOf(inviteRecommendationsResult.getOrThrow())

    companion object {
        val DEFAULT_SHARE_ID = ShareId("TestInviteRepository-ShareId")
    }
}
