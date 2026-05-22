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

package proton.android.pass.data.impl.local.search

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SupportSQLiteQuery
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchDao {

    // CRUD operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: SearchItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<SearchItemEntity>)

    @Query(
        """
        DELETE FROM search_items
        WHERE share_id = :shareId AND item_id = :itemId
        """
    )
    suspend fun delete(shareId: String, itemId: String)

    @Query(
        """
        DELETE FROM search_items
        WHERE user_id = :userId
        """
    )
    suspend fun clearForUser(userId: String)

    @Query(
        """
        DELETE FROM search_items
        WHERE share_id = :shareId
        """
    )
    suspend fun clearForShare(shareId: String)

    @Query(
        """
        UPDATE search_items
        SET item_state = :itemState
        WHERE share_id = :shareId AND item_id = :itemId
        """
    )
    suspend fun updateItemState(
        shareId: String,
        itemId: String,
        itemState: Int
    )

    @Query(
        """
        UPDATE search_items
        SET is_hidden = :isHidden
        WHERE share_id = :shareId
        """
    )
    suspend fun updateHiddenForShare(shareId: String, isHidden: Boolean)

    // Count operations
    @Query(
        """
        SELECT COUNT(*)
        FROM search_items
        WHERE user_id = :userId
        """
    )
    suspend fun countForUser(userId: String): Int

    // Check if index exists for user
    @Query(
        """
        SELECT EXISTS(
            SELECT 1 FROM search_items
            WHERE user_id = :userId
            LIMIT 1
        )
        """
    )
    suspend fun hasIndexForUser(userId: String): Boolean

    // PagingSource for Room auto-invalidation
    @RawQuery(observedEntities = [SearchItemEntity::class])
    fun searchPaging(query: SupportSQLiteQuery): PagingSource<Int, SearchIdRow>

    @RawQuery(observedEntities = [SearchItemEntity::class])
    fun searchFtsPaging(query: SupportSQLiteQuery): PagingSource<Int, SearchIdRow>

    // Manual pagination for batch item fetching
    @RawQuery(observedEntities = [SearchItemEntity::class])
    suspend fun searchPage(query: SupportSQLiteQuery): List<SearchIdRow>

    @RawQuery(observedEntities = [SearchItemEntity::class])
    suspend fun searchFtsPage(query: SupportSQLiteQuery): List<SearchIdRow>

    // Count items grouped by type (for filter chips)
    @RawQuery(observedEntities = [SearchItemEntity::class])
    fun countByItemType(query: SupportSQLiteQuery): Flow<List<ItemTypeCountRow>>
}

data class SearchIdRow(
    val userId: String,
    val shareId: String,
    val itemId: String
)

data class ItemTypeCountRow(
    val itemType: Int,
    val count: Int
)
