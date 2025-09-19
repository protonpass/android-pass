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

package proton.android.pass.data.api.repositories

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.UserAddress
import proton.android.pass.domain.Share
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareType
import proton.android.pass.domain.entity.NewVault

@Suppress("ComplexInterface", "TooManyFunctions")
interface ShareRepository {

    suspend fun createVault(userId: UserId, vault: NewVault): Share

    suspend fun deleteVault(userId: UserId, shareId: ShareId)

    suspend fun refreshShares(userId: UserId): RefreshSharesResult

    suspend fun refreshShare(userId: UserId, shareId: ShareId)

    fun observeAllShares(userId: UserId): Flow<List<Share>>

    fun observeAllUsableShareIds(userId: UserId): Flow<List<ShareId>>

    fun observeVaultCount(userId: UserId): Flow<Int>

    suspend fun getById(userId: UserId, shareId: ShareId): Share

    fun observeById(userId: UserId, shareId: ShareId): Flow<Share>

    fun observeSharesByType(userId: UserId, shareType: ShareType): Flow<List<Share>>

    suspend fun updateVault(
        userId: UserId,
        shareId: ShareId,
        vault: NewVault
    ): Share

    suspend fun deleteSharesForUser(userId: UserId): Boolean

    suspend fun leaveVault(userId: UserId, shareId: ShareId)

    suspend fun applyUpdateShareEvent(
        userId: UserId,
        shareId: ShareId,
        event: UpdateShareEvent
    )

    suspend fun applyPendingShareEvent(userId: UserId, event: UpdateShareEvent)

    suspend fun applyPendingShareEventKeys(userId: UserId, event: UpdateShareEvent)

    suspend fun getAddressForShareId(userId: UserId, shareId: ShareId): UserAddress

    fun observeSharedWithMeIds(userId: UserId): Flow<List<ShareId>>

    fun observeSharedByMeIds(userId: UserId): Flow<List<ShareId>>

    suspend fun batchChangeShareVisibility(userId: UserId, shareVisibilityChanges: Map<ShareId, Boolean>)
}

data class UpdateShareEvent(
    val shareId: String,
    val vaultId: String,
    val addressId: String,
    val targetType: Int,
    val targetId: String,
    val permission: Int,
    val content: String?,
    val contentKeyRotation: Long?,
    val contentFormatVersion: Int?,
    val shareRoleId: String,
    val targetMembers: Int,
    val owner: Boolean,
    val shared: Boolean,
    val expirationTime: Long?,
    val createTime: Long,
    val targetMaxMembers: Int,
    val newUserInvitesReady: Int,
    val pendingInvites: Int,
    val canAutofill: Boolean,
    val flags: Int
)

data class RefreshSharesResult(
    val allShareIds: Set<ShareId>,
    val newShareIds: Set<ShareId>,
    val wasFirstSync: Boolean
)
