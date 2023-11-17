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

package proton.android.pass.data.impl.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.SearchEntry
import proton.android.pass.data.api.repositories.SearchEntryRepository
import proton.android.pass.data.impl.db.entities.SearchEntryEntity
import proton.android.pass.data.impl.local.LocalSearchEntryDataSource
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import javax.inject.Inject

class SearchEntryRepositoryImpl @Inject constructor(
    private val localDataSource: LocalSearchEntryDataSource,
    private val clock: Clock
) : SearchEntryRepository {

    override suspend fun store(userId: UserId, shareId: ShareId, itemId: ItemId) {
        val searchEntryEntity = SearchEntryEntity(
            itemId = itemId.id,
            shareId = shareId.id,
            userId = userId.id,
            createTime = clock.now().epochSeconds
        )
        localDataSource.store(searchEntryEntity)
    }

    override suspend fun deleteAll(userId: UserId) {
        localDataSource.deleteAll(userId)
    }

    override suspend fun deleteAllByShare(shareId: ShareId) {
        localDataSource.deleteAllByShare(shareId)
    }

    override suspend fun deleteEntry(shareId: ShareId, itemId: ItemId) {
        localDataSource.deleteEntry(shareId, itemId)
    }

    override fun observeAll(userId: UserId): Flow<List<SearchEntry>> =
        localDataSource.observeAll(userId)
            .map { list ->
                list.map { it.toSearchEntry() }
            }

    override fun observeAllByShare(shareId: ShareId): Flow<List<SearchEntry>> =
        localDataSource.observeAllByShare(shareId)
            .map { list ->
                list.map { it.toSearchEntry() }
            }

    private fun SearchEntryEntity.toSearchEntry() = SearchEntry(
        itemId = ItemId(itemId),
        shareId = ShareId(shareId),
        userId = UserId(userId),
        createTime = createTime
    )
}
