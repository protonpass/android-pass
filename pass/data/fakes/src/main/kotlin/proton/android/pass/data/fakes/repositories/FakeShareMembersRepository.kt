/*
 * Copyright (c) 2026 Proton AG
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
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.FlowUtils.testFlow
import proton.android.pass.data.api.repositories.ShareMembersRepository
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareRole
import proton.android.pass.domain.shares.ShareMember
import javax.inject.Inject

class FakeShareMembersRepository @Inject constructor() : ShareMembersRepository {

    private var members: List<ShareMember> = emptyList()
    private var getMembersTotalError: Throwable? = null
    private val observeShareItemMembersFlow = testFlow<List<ShareMember>>()

    fun setMembers(value: List<ShareMember>) {
        members = value
    }

    fun setGetMembersTotalError(error: Throwable) {
        getMembersTotalError = error
    }

    fun emitShareItemMembers(value: List<ShareMember>) {
        observeShareItemMembersFlow.tryEmit(value)
    }

    override suspend fun getShareMembers(
        userId: UserId,
        shareId: ShareId,
        userEmail: String?
    ): List<ShareMember> = members

    override fun observeShareItemMembers(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        userEmail: String?
    ): Flow<List<ShareMember>> = observeShareItemMembersFlow

    override suspend fun updateShareMember(
        userId: UserId,
        shareId: ShareId,
        memberShareId: ShareId,
        memberShareRole: ShareRole
    ) = Unit

    override suspend fun deleteShareMember(
        userId: UserId,
        shareId: ShareId,
        memberShareId: ShareId
    ) = Unit

    override suspend fun getShareMembersTotal(userId: UserId, shareId: ShareId): Int {
        getMembersTotalError?.let { throw it }
        return members.size
    }
}
