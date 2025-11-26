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

package proton.android.pass.data.impl.repositories

import kotlinx.coroutines.flow.MutableStateFlow
import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.entity.key.PrivateAddressKey
import me.proton.core.key.domain.entity.key.PrivateKey
import me.proton.core.key.domain.entity.key.canEncrypt
import me.proton.core.key.domain.entity.key.canVerify
import me.proton.core.util.kotlin.toBoolean
import proton.android.pass.data.api.repositories.GroupRepository
import proton.android.pass.data.impl.remote.groups.RemoteGroupsDataSource
import proton.android.pass.data.impl.responses.invites.AddressApiModel
import proton.android.pass.data.impl.responses.invites.GroupApiModel
import proton.android.pass.data.impl.responses.invites.GroupMemberApiModel
import proton.android.pass.domain.Group
import proton.android.pass.domain.GroupAddress
import proton.android.pass.domain.GroupId
import proton.android.pass.domain.GroupMember
import javax.inject.Inject

class GroupRepositoryImpl @Inject constructor(
    private val remoteGroupsDataSource: RemoteGroupsDataSource
) : GroupRepository {

    private val groupsCache = MutableStateFlow<Map<UserId, List<Group>>>(emptyMap())
    private val groupMembersCache =
        MutableStateFlow<Map<UserId, Map<GroupId, List<GroupMember>>>>(emptyMap())

    override suspend fun retrieveGroups(userId: UserId, forceRefresh: Boolean): List<Group> =
        fetchAndCacheGroups(userId, forceRefresh)

    override suspend fun retrieveGroup(
        userId: UserId,
        groupId: GroupId,
        forceRefresh: Boolean
    ): Group? {
        if (!forceRefresh) {
            val cachedGroup = getCachedGroup(userId, groupId.id)
            if (cachedGroup != null) {
                return cachedGroup
            }
        }

        val groups = fetchAndCacheGroups(userId, forceRefresh)
        return groups.find { it.id.id == groupId.id }
    }

    override suspend fun retrieveGroupMembers(
        userId: UserId,
        groupId: GroupId,
        forceRefresh: Boolean
    ): List<GroupMember> = fetchAndCacheGroupMembers(userId, groupId, forceRefresh)

    private fun getCachedGroups(userId: UserId): List<Group>? = groupsCache.value[userId]

    private fun getCachedGroup(userId: UserId, groupId: String): Group? =
        getCachedGroups(userId)?.find { it.id.id == groupId }

    private fun getCachedGroupMembers(userId: UserId, groupId: GroupId): List<GroupMember>? =
        groupMembersCache.value[userId]?.get(groupId)

    private suspend fun fetchAndCacheGroups(userId: UserId, forceRefresh: Boolean = false): List<Group> {
        if (!forceRefresh) {
            val cachedGroups = getCachedGroups(userId)
            if (cachedGroups != null) {
                return cachedGroups
            }
        }

        val apiModels = remoteGroupsDataSource.retrieveGroups(userId)
        val groups = apiModels.map(GroupApiModel::toDomain)
        groupsCache.value += userId to groups

        return groups
    }

    private suspend fun fetchAndCacheGroupMembers(
        userId: UserId,
        groupId: GroupId,
        forceRefresh: Boolean
    ): List<GroupMember> {
        if (!forceRefresh) {
            val cachedMembers = getCachedGroupMembers(userId, groupId)
            if (cachedMembers != null) {
                return cachedMembers
            }
        }

        val members = mutableListOf<GroupMember>()
        var page = 0

        while (true) {
            val apiModels = remoteGroupsDataSource.retrieveGroupMembers(
                userId = userId,
                groupId = groupId,
                pageSize = GROUP_MEMBERS_DEFAULT_PAGE_SIZE,
                page = page
            )
            if (apiModels.isEmpty()) {
                break
            }

            members += apiModels.map(GroupMemberApiModel::toDomain)

            if (apiModels.size < GROUP_MEMBERS_DEFAULT_PAGE_SIZE) {
                break
            }

            page++
        }

        val membersList = members.toList()
        val groupMembersByUser = groupMembersCache.value[userId].orEmpty() + (groupId to membersList)
        groupMembersCache.value += userId to groupMembersByUser

        return membersList
    }

    private companion object {
        const val GROUP_MEMBERS_DEFAULT_PAGE_SIZE = 1000
    }
}

private fun GroupApiModel.toDomain(): Group = Group(
    id = GroupId(id),
    name = name,
    address = address?.toDomain(),
    permissions = permissions,
    createTime = createTime,
    flags = flags,
    groupVisibility = groupVisibility,
    memberVisibility = memberVisibility,
    description = description
)

private fun AddressApiModel.toDomain(): GroupAddress = GroupAddress(
    id = id,
    email = email,
    keys = keys?.map {
        PrivateAddressKey(
            addressId = it.id,
            privateKey = PrivateKey(
                key = it.privateKey,
                isPrimary = it.primary.toBoolean(),
                isActive = it.active.toBoolean(),
                canEncrypt = it.flags.canEncrypt(),
                canVerify = it.flags.canVerify(),
                passphrase = null
            ),
            token = it.token,
            signature = it.signature
        )
    }
)

private fun GroupMemberApiModel.toDomain(): GroupMember = GroupMember(
    id = id,
    type = type,
    state = state,
    createTime = createTime,
    groupId = groupId,
    addressId = addressId,
    email = email
)
