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
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.shares.ShareMember
import javax.inject.Inject

class LocalShareMembersDataSourceImpl @Inject constructor() : LocalShareMembersDataSource {

    private val shareMembersFlow = MutableStateFlow<Map<UserId, Map<ShareId, List<ShareMember>>>>(
        value = emptyMap()
    )

    override fun getShareMember(
        userId: UserId,
        shareId: ShareId,
        memberShareId: ShareId
    ): ShareMember? = shareMembersFlow.value[userId]
        ?.get(shareId)
        ?.firstOrNull { it.shareId == memberShareId }

    override fun upsertShareMember(
        userId: UserId,
        shareId: ShareId,
        shareMember: ShareMember
    ) {
        shareMembersFlow.value[userId]
            ?.get(shareId)
            ?.map { currentShareMember ->
                if (currentShareMember.shareId == shareMember.shareId) {
                    shareMember
                } else {
                    currentShareMember
                }
            }
            ?.also { newShareMembers ->
                upsertShareMembers(userId, shareId, newShareMembers)
            }
    }

    override fun deleteShareMember(
        userId: UserId,
        shareId: ShareId,
        memberShareId: ShareId
    ) {
        shareMembersFlow.value[userId]
            ?.get(shareId)
            ?.filter { it.shareId != memberShareId }
            ?.also { newShareMembers ->
                upsertShareMembers(userId, shareId, newShareMembers)
            }
    }

    override fun observeShareMembers(userId: UserId, shareId: ShareId): Flow<List<ShareMember>> =
        shareMembersFlow.mapLatest { shareMembersMap ->
            shareMembersMap[userId]?.get(shareId).orEmpty()
        }

    override fun upsertShareMembers(
        userId: UserId,
        shareId: ShareId,
        shareMembers: List<ShareMember>
    ) {
        shareMembersFlow.update { shareMembersMap ->
            shareMembersMap.toMutableMap().apply {
                put(userId, mapOf(shareId to shareMembers))
            }
        }
    }

}
