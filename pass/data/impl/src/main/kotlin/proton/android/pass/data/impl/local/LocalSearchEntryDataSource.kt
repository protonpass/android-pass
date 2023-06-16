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
import proton.android.pass.data.impl.db.entities.SearchEntryEntity
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId
import javax.inject.Inject

interface LocalSearchEntryDataSource {
    suspend fun store(entity: SearchEntryEntity)
    suspend fun deleteAll(userId: UserId)
    suspend fun deleteAllByShare(shareId: ShareId)
    suspend fun deleteEntry(shareId: ShareId, itemId: ItemId)
    fun observeAll(userId: UserId): Flow<List<SearchEntryEntity>>
    fun observeAllByShare(shareId: ShareId): Flow<List<SearchEntryEntity>>
}

class LocalSearchEntryDataSourceImpl @Inject constructor(
    private val db: PassDatabase
) : LocalSearchEntryDataSource {
    override suspend fun store(entity: SearchEntryEntity) {
        db.searchEntryDao().insertOrUpdate(entity)
    }

    override suspend fun deleteAll(userId: UserId) {
        db.searchEntryDao().deleteAll(userId.id)
    }

    override suspend fun deleteAllByShare(shareId: ShareId) {
        db.searchEntryDao().deleteAllByShare(shareId.id)
    }

    override suspend fun deleteEntry(shareId: ShareId, itemId: ItemId) {
        db.searchEntryDao().deleteEntry(shareId.id, itemId.id)
    }

    override fun observeAll(userId: UserId): Flow<List<SearchEntryEntity>> =
        db.searchEntryDao().observeAll(userId.id)

    override fun observeAllByShare(shareId: ShareId): Flow<List<SearchEntryEntity>> =
        db.searchEntryDao().observeAllByShare(shareId.id)
}
