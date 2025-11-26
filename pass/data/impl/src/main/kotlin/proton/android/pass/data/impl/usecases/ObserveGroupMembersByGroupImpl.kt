/*
 * Copyright (c) 2025 Proton AG
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

package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.errors.UserIdNotAvailableError
import proton.android.pass.data.api.repositories.GroupRepository
import proton.android.pass.data.api.usecases.GroupMembers
import proton.android.pass.data.api.usecases.ObserveGroupMembersByGroup
import proton.android.pass.preferences.FeatureFlag
import proton.android.pass.preferences.FeatureFlagsPreferencesRepository
import javax.inject.Inject

class ObserveGroupMembersByGroupImpl @Inject constructor(
    private val accountManager: AccountManager,
    private val groupRepository: GroupRepository,
    private val featureFlagsPreferencesRepository: FeatureFlagsPreferencesRepository
) : ObserveGroupMembersByGroup {

    override fun invoke(userId: UserId?, forceRefresh: Boolean): Flow<List<GroupMembers>> = flow {
        val isGroupSharingEnabled =
            featureFlagsPreferencesRepository.get<Boolean>(FeatureFlag.PASS_GROUP_SHARE)
                .firstOrNull()
                ?: false
        if (isGroupSharingEnabled) {
            val currentUserId = userId ?: accountManager.getPrimaryUserId().firstOrNull()
                ?: throw UserIdNotAvailableError()
            emit(retrieveGroupMembersByGroup(currentUserId, forceRefresh))
        } else {
            emit(emptyList())
        }
    }

    private suspend fun retrieveGroupMembersByGroup(userId: UserId, forceRefresh: Boolean): List<GroupMembers> {
        val groups = groupRepository.retrieveGroups(userId, forceRefresh)
        return groups.map { group ->
            val members = groupRepository.retrieveGroupMembers(
                userId = userId,
                groupId = group.id,
                forceRefresh = forceRefresh
            )
            GroupMembers(group = group, members = members)
        }
    }
}
