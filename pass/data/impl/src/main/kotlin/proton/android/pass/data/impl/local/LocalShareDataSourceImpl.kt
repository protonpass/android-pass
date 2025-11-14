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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import me.proton.core.domain.entity.UserId
import proton.android.pass.commonrust.api.UsableShareFilter
import proton.android.pass.commonrust.api.UsableShareKey
import proton.android.pass.data.impl.db.PassDatabase
import proton.android.pass.data.impl.db.entities.ShareEntity
import proton.android.pass.data.impl.db.entities.ShareKeyView
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareRole
import proton.android.pass.domain.ShareType
import javax.inject.Inject
import kotlin.collections.map

class LocalShareDataSourceImpl @Inject constructor(
    private val database: PassDatabase,
    private val usableShareFilter: UsableShareFilter
) : LocalShareDataSource {

    override suspend fun upsertShares(shares: List<ShareEntity>) {
        val (active, inactive) = shares.partition { it.isActive }
        database.sharesDao().insertOrUpdate(*active.toTypedArray())
        database.sharesDao().delete(*inactive.toTypedArray())
    }

    override suspend fun getById(userId: UserId, shareId: ShareId): ShareEntity? =
        database.sharesDao().observeById(userId.id, shareId.id).firstOrNull()

    override fun observeById(userId: UserId, shareId: ShareId): Flow<ShareEntity?> =
        database.sharesDao().observeById(userId.id, shareId.id)

    override fun observeAllIncludingInactive(userId: UserId): Flow<List<ShareEntity>> =
        database.sharesDao().observeAllIncludingInactive(userId = userId.id)

    override fun observeAllActiveSharesForUser(userId: UserId, includeHidden: Boolean): Flow<List<ShareEntity>> =
        observeUsableShareIds(userId, includeHidden).flatMapLatest { shareIds ->
            database.sharesDao().observeActive(
                userId = userId.id,
                shareIds = shareIds.map(ShareId::id),
                shareType = null,
                shareRole = null
            )
        }

    override fun observeUsableShareIds(userId: UserId, includeHidden: Boolean): Flow<List<ShareId>> =
        database.sharesDao()
            .observeShareKeyView(userId.id)
            .map { mapToUsableShareKeys(it) }
            .map { usableShareFilter.filter(it, includeHidden) }
            .distinctUntilChanged()

    override suspend fun deleteShares(userId: UserId, shareIds: Set<ShareId>): Boolean =
        database.sharesDao().deleteShares(
            userId = userId.id,
            shareIds = shareIds.map { it.id },
            applyShareIds = true
        ) > 0

    override suspend fun deleteSharesForUser(userId: UserId): Boolean = database.sharesDao().deleteShares(
        userId = userId.id,
        shareIds = null,
        applyShareIds = false
    ) > 0

    override fun observeActiveVaultCount(userId: UserId, includeHidden: Boolean): Flow<Int> =
        observeUsableShareIds(userId, includeHidden).flatMapLatest { shareIds ->
            database.sharesDao().observeCount(
                userId = userId.id,
                shareIds = shareIds.map(ShareId::id),
                shareType = ShareType.Vault.value
            )
        }

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
        includeHidden: Boolean
    ): Flow<List<ShareEntity>> = observeUsableShareIds(userId, includeHidden)
        .flatMapLatest { shareIds ->
            database.sharesDao()
                .observeActive(
                    userId = userId.id,
                    shareIds = shareIds.map(ShareId::id),
                    shareType = shareType.value,
                    shareRole = null
                )
        }

    override fun observeSharedWithMeIds(userId: UserId, includeHidden: Boolean): Flow<List<ShareId>> =
        observeUsableShareIds(userId, includeHidden)
            .flatMapLatest { shareIds ->
                database.sharesDao()
                    .observeIds(
                        userId = userId.id,
                        shareIds = shareIds.map(ShareId::id),
                        shareType = ShareType.Item.value,
                        shareRole = null
                    )
                    .map { list -> list.map(::ShareId) }
            }

    override fun observeSharedByMeIds(userId: UserId, includeHidden: Boolean): Flow<List<ShareId>> =
        observeUsableShareIds(userId, includeHidden).flatMapLatest { shareIds ->
            database.sharesDao()
                .observeIds(
                    userId = userId.id,
                    shareIds = shareIds.map(ShareId::id),
                    shareType = null,
                    shareRole = ShareRole.SHARE_ROLE_ADMIN
                )
                .map { list -> list.map(::ShareId) }
        }

    private fun mapToUsableShareKeys(entities: List<ShareKeyView>): List<UsableShareKey> = entities.map {
        UsableShareKey(
            shareId = it.shareId,
            vaultId = it.vaultId,
            targetType = ShareType.from(it.targetType),
            targetId = it.targetId,
            roleId = it.roleId,
            permissions = it.permissions,
            flags = it.flags
        )
    }

}
