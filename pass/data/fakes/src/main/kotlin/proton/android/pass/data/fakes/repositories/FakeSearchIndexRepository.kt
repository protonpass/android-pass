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

package proton.android.pass.data.fakes.repositories

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.repositories.IndexingStatus
import proton.android.pass.data.api.repositories.ItemTypeCounts
import proton.android.pass.data.api.repositories.SearchIndexRepository
import proton.android.pass.data.api.repositories.SearchSortBy
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.domain.FolderId
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ItemState
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.items.ItemSharedType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeSearchIndexRepository @Inject constructor() : SearchIndexRepository {

    private val indexedItems = mutableMapOf<String, IndexedItem>()
    private val items = mutableMapOf<String, Item>()
    private var needsRebuild = false
    private var rebuildCalled = false
    private val _indexingStatus = MutableStateFlow<IndexingStatus>(IndexingStatus.Ready)

    override fun observeIndexingStatus(): Flow<IndexingStatus> = _indexingStatus.asStateFlow()

    fun setIndexingStatus(status: IndexingStatus) {
        _indexingStatus.value = status
    }

    data class IndexedItem(
        val userId: UserId,
        val shareId: ShareId,
        val itemId: ItemId,
        val title: String = "",
        val subtitle: String? = null,
        val createTime: Long = 0,
        val modifyTime: Long = 0
    )

    override suspend fun indexItem(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ) {
        val key = "${shareId.id}:${itemId.id}"
        indexedItems[key] = IndexedItem(userId, shareId, itemId)
    }

    override suspend fun indexItems(userId: UserId, items: List<Pair<ShareId, ItemId>>) {
        items.forEach { (shareId, itemId) ->
            indexItem(userId, shareId, itemId)
        }
    }

    fun isItemIndexed(shareId: ShareId, itemId: ItemId): Boolean =
        indexedItems.containsKey("${shareId.id}:${itemId.id}")

    override suspend fun removeItemFromIndex(shareId: ShareId, itemId: ItemId) {
        val key = "${shareId.id}:${itemId.id}"
        indexedItems.remove(key)
    }

    override suspend fun updateItemState(
        shareId: ShareId,
        itemId: ItemId,
        itemState: ItemState
    ) {
        // No-op for fake
    }

    override suspend fun removeShareFromIndex(shareId: ShareId) {
        val keysToRemove = indexedItems.keys.filter { it.startsWith("${shareId.id}:") }
        keysToRemove.forEach { indexedItems.remove(it) }
    }

    override suspend fun updateShareHidden(shareId: ShareId, isHidden: Boolean) {
        // No-op for fake
    }

    override suspend fun indexShare(userId: UserId, shareId: ShareId) {
        items.values
            .filter { it.userId == userId && it.shareId == shareId }
            .forEach { indexItem(userId, shareId, it.id) }
    }

    override suspend fun rebuildIndex(userId: UserId) {
        rebuildCalled = true
        needsRebuild = false
    }

    override suspend fun needsRebuild(userId: UserId): Boolean = needsRebuild

    override suspend fun checkAndRebuildIfNeeded(userId: UserId) {
        if (needsRebuild(userId)) {
            rebuildIndex(userId)
        }
    }

    override suspend fun clearIndex(userId: UserId) {
        val keysToRemove = indexedItems.filter { it.value.userId == userId }.keys
        keysToRemove.forEach { indexedItems.remove(it) }
    }

    override fun getItems(
        userId: UserId,
        query: String?,
        sortBy: SearchSortBy,
        shareIds: List<ShareId>?,
        folderId: FolderId?,
        itemState: ItemState?,
        itemSharedType: ItemSharedType?,
        itemTypeFilter: ItemTypeFilter,
        includeHidden: Boolean
    ): Flow<PagingData<Item>> {
        val filtered = items.values
            .filter { it.userId == userId }
            .filter { shareIds == null || shareIds.isEmpty() || it.shareId in shareIds }
            .filter { item ->
                val expectedState = itemState ?: ItemState.Active
                item.state == expectedState.value
            }

        val sorted = when (sortBy) {
            SearchSortBy.TITLE_ASC -> filtered.sortedBy { it.title.lowercase() }
            SearchSortBy.TITLE_DESC -> filtered.sortedByDescending { it.title.lowercase() }
            SearchSortBy.CREATION_DATE_DESC -> filtered.sortedByDescending { it.createTime.epochSeconds }
            SearchSortBy.CREATION_DATE_ASC -> filtered.sortedBy { it.createTime.epochSeconds }
            SearchSortBy.MODIFICATION_DATE_DESC -> filtered.sortedByDescending { it.modificationTime.epochSeconds }
            SearchSortBy.MOST_RECENT,
            SearchSortBy.RELEVANCE -> filtered.sortedByDescending { item ->
                val lastAutofill = item.lastAutofillTime.value()?.epochSeconds ?: 0L
                maxOf(lastAutofill, item.modificationTime.epochSeconds)
            }
        }

        return flowOf(PagingData.from(sorted))
    }

    override fun observeItemTypeCounts(
        userId: UserId,
        shareIds: List<ShareId>?,
        folderId: FolderId?,
        itemState: ItemState?,
        itemSharedType: ItemSharedType?,
        query: String?,
        includeHidden: Boolean
    ): Flow<ItemTypeCounts> = flowOf(ItemTypeCounts.EMPTY)
}


