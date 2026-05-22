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

package proton.android.pass.data.api.repositories

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.domain.FolderId
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ItemState
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.items.ItemSharedType

interface SearchIndexRepository {

    /**
     * Observe the indexing status
     */
    fun observeIndexingStatus(): Flow<IndexingStatus>

    /**
     * Index a single item
     */
    suspend fun indexItem(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    )

    /**
     * Index multiple items at once
     */
    suspend fun indexItems(userId: UserId, items: List<Pair<ShareId, ItemId>>)

    /**
     * Remove an item from the index
     */
    suspend fun removeItemFromIndex(shareId: ShareId, itemId: ItemId)

    /**
     * Update the item state in the index (Active or Trashed)
     */
    suspend fun updateItemState(
        shareId: ShareId,
        itemId: ItemId,
        itemState: ItemState
    )

    /**
     * Remove all items for a share from the index
     */
    suspend fun removeShareFromIndex(shareId: ShareId)

    /**
     * Update the hidden flag for all items of a share in the index.
     * Used when a vault's visibility changes, so search can keep excluding hidden vaults
     * without re-decrypting and re-indexing their items.
     */
    suspend fun updateShareHidden(shareId: ShareId, isHidden: Boolean)

    /**
     * Index (clear then re-add) all items of a single share.
     * Used after fetching a freshly accepted shared vault, whose items are inserted
     * via setShareItems without a full index rebuild.
     */
    suspend fun indexShare(userId: UserId, shareId: ShareId)

    /**
     * Rebuild the entire index for a user
     */
    suspend fun rebuildIndex(userId: UserId)

    /**
     * Check if the index needs to be rebuilt
     */
    suspend fun needsRebuild(userId: UserId): Boolean

    /**
     * Check if the index needs to be rebuilt and rebuild if necessary.
     * This is useful for automatic recovery after database migrations.
     */
    suspend fun checkAndRebuildIfNeeded(userId: UserId)

    /**
     * Clear the index for a user
     */
    suspend fun clearIndex(userId: UserId)

    /**
     * Get a PagingSource that auto-invalidates when data changes in Room.
     * Use this for proper pagination with automatic refresh on data changes.
     * @param scope CoroutineScope for observing data changes
     * @param query Search query, or null/blank to get all items
     * @param shareIds Optional list of share IDs to filter by. If null, searches all shares.
     * @param itemState Optional item state to filter by (Active or Trashed). If null, returns Active items.
     * @param itemSharedType Optional shared type to filter by (SharedByMe or SharedWithMe). If null, no filter.
     * @param itemTypeFilter Optional item type to filter by. If All, no type filter is applied.
     * @param includeHidden If false (default), items belonging to hidden vaults are excluded.
     */
    fun getItems(
        userId: UserId,
        query: String?,
        sortBy: SearchSortBy,
        shareIds: List<ShareId>? = null,
        folderId: FolderId? = null,
        itemState: ItemState? = null,
        itemSharedType: ItemSharedType? = null,
        itemTypeFilter: ItemTypeFilter = ItemTypeFilter.All,
        includeHidden: Boolean = false
    ): Flow<PagingData<Item>>

    /**
     * Observe item counts by type for filter chips display.
     * Returns counts for each item category (Login, Alias, Note, etc.)
     */
    fun observeItemTypeCounts(
        userId: UserId,
        shareIds: List<ShareId>? = null,
        folderId: FolderId? = null,
        itemState: ItemState? = null,
        itemSharedType: ItemSharedType? = null,
        query: String? = null,
        includeHidden: Boolean = false
    ): Flow<ItemTypeCounts>
}

data class ItemTypeCounts(
    val loginCount: Int = 0,
    val aliasCount: Int = 0,
    val noteCount: Int = 0,
    val creditCardCount: Int = 0,
    val identityCount: Int = 0,
    val customCount: Int = 0 // Includes Custom, WifiNetwork, SSHKey
) {
    companion object {
        val EMPTY = ItemTypeCounts()
    }
}

enum class SearchSortBy {
    TITLE_ASC,
    TITLE_DESC,
    CREATION_DATE_DESC,
    CREATION_DATE_ASC,
    MODIFICATION_DATE_DESC,
    MOST_RECENT,

    /**
     * Sort by FTS5 bm25 relevance. Only meaningful for an actual text search;
     * callers fall back to another sort when there is no query.
     */
    RELEVANCE
}

data class SearchResultItem(
    val userId: UserId,
    val shareId: ShareId,
    val itemId: ItemId
)

sealed interface IndexingStatus {
    data object Idle : IndexingStatus
    data object Indexing : IndexingStatus
    data class InProgress(val current: Int, val total: Int) : IndexingStatus
    data object Ready : IndexingStatus
}
