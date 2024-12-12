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

package proton.android.pass.data.impl.local.shares

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.update
import me.proton.core.domain.entity.UserId
import proton.android.pass.domain.InviteId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.shares.SharePendingInvite
import javax.inject.Inject

class LocalShareInvitesDataSourceImpl @Inject constructor() : LocalShareInvitesDataSource {

    private val sharePendingInvitesFlow =
        MutableStateFlow<Map<UserId, Map<ShareId, List<SharePendingInvite>>>>(
            value = emptyMap()
        )

    override fun getSharePendingInvite(
        userId: UserId,
        shareId: ShareId,
        inviteId: InviteId
    ): SharePendingInvite? = sharePendingInvitesFlow.value[userId]
        ?.get(shareId)
        ?.first { it.inviteId == inviteId }

    override fun deleteSharePendingInvite(
        userId: UserId,
        shareId: ShareId,
        inviteId: InviteId
    ) {
        sharePendingInvitesFlow.value[userId]
            ?.get(shareId)
            ?.filter { it.inviteId != inviteId }
            ?.also { sharePendingInvites ->
                upsertSharePendingInvites(userId, shareId, sharePendingInvites)
            }
    }

    override fun observeSharePendingInvites(userId: UserId, shareId: ShareId): Flow<List<SharePendingInvite>> =
        sharePendingInvitesFlow
            .mapLatest { sharePendingInvitesMap ->
                sharePendingInvitesMap[userId]?.get(shareId).orEmpty()
            }

    override fun upsertSharePendingInvites(
        userId: UserId,
        shareId: ShareId,
        pendingInvites: List<SharePendingInvite>
    ) {
        sharePendingInvitesFlow.update { sharePendingInvitesMap ->
            sharePendingInvitesMap.toMutableMap().apply {
                put(userId, mapOf(shareId to pendingInvites))
            }
        }
    }

}
