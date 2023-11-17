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
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.impl.db.PassDatabase
import proton.android.pass.data.impl.db.entities.ShareKeyEntity
import proton.android.pass.domain.ShareId
import javax.inject.Inject

class LocalShareKeyDataSourceImpl @Inject constructor(
    private val passDatabase: PassDatabase
) : LocalShareKeyDataSource {
    override fun getAllShareKeysForShare(
        userId: UserId,
        shareId: ShareId
    ): Flow<List<ShareKeyEntity>> =
        passDatabase.shareKeysDao().getAllForShare(userId.id, shareId.id)

    override fun getForShareAndRotation(
        userId: UserId,
        shareId: ShareId,
        rotation: Long
    ): Flow<ShareKeyEntity?> =
        passDatabase.shareKeysDao().getByShareAndRotation(userId.id, shareId.id, rotation)

    override fun getLatestKeyForShare(shareId: ShareId): Flow<ShareKeyEntity> =
        passDatabase.shareKeysDao().getLatestKeyForShare(shareId.id)

    override suspend fun storeShareKeys(entities: List<ShareKeyEntity>) {
        passDatabase.shareKeysDao().insertOrUpdate(*entities.toTypedArray())
    }
}
