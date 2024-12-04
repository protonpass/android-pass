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

package proton.android.pass.data.impl.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.update
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.AddressId
import proton.android.pass.data.impl.db.PassDatabase
import proton.android.pass.data.impl.db.entities.ShareEntity
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareType
import proton.android.pass.domain.shares.ShareMember
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

class LocalShareDataSourceImpl @Inject constructor(
    private val database: PassDatabase
) : LocalShareDataSource {

    private val shareMembersFlow = MutableStateFlow<Map<UserId, Map<ShareId, List<ShareMember>>>>(
        value = emptyMap()
    )

    override suspend fun upsertShares(shares: List<ShareEntity>) {
        val (active, inactive) = shares.partition { it.isActive }
        database.sharesDao().insertOrUpdate(*active.toTypedArray())
        database.sharesDao().delete(*inactive.toTypedArray())
    }

    override suspend fun getById(userId: UserId, shareId: ShareId): ShareEntity? =
        database.sharesDao().observeById(userId.id, shareId.id).firstOrNull()

    override fun observeById(userId: UserId, shareId: ShareId): Flow<ShareEntity?> =
        database.sharesDao().observeById(userId.id, shareId.id)

    override fun getAllSharesForUser(userId: UserId): Flow<List<ShareEntity>> =
        database.sharesDao().observeAllForUser(userId.id)

    override fun observeAllActiveSharesForUser(userId: UserId): Flow<List<ShareEntity>> =
        database.sharesDao().observeAllActiveForUser(userId.id)

    override fun getAllSharesForAddress(addressId: AddressId): Flow<List<ShareEntity>> =
        database.sharesDao().observeAllForAddress(addressId.id)

    override suspend fun deleteShares(shareIds: Set<ShareId>): Boolean {
        PassLogger.i(TAG, "Deleting shares: ${shareIds.map { it.id }}")
        return database.sharesDao().delete(shareIds.map { it.id }.toTypedArray()) > 0
    }

    override suspend fun hasShares(userId: UserId): Boolean = database.sharesDao().countShares(userId.id) > 0

    override suspend fun deleteSharesForUser(userId: UserId) {
        PassLogger.i(TAG, "Deleting all shares for user")
        database.sharesDao().deleteSharesForUser(userId.id)
    }

    override fun observeActiveVaultCount(userId: UserId): Flow<Int> =
        database.sharesDao().observeActiveVaultCount(userId.id)

    override suspend fun updateOwnershipStatus(
        userId: UserId,
        shareId: ShareId,
        isOwner: Boolean
    ) = database.sharesDao().updateOwnership(
        userId = userId.id,
        shareId = shareId.id,
        isOwner = isOwner
    )

    override fun observeByType(
        userId: UserId,
        shareType: ShareType,
        isActive: Boolean?
    ): Flow<List<ShareEntity>> = database.sharesDao()
        .observeByType(
            userId = userId.id,
            shareType = shareType.value,
            isActive = isActive
        )

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

    private companion object {

        private const val TAG = "LocalShareDataSourceImpl"

    }

}
