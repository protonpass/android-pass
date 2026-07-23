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
import androidx.paging.PagingState
import androidx.room.InvalidationTracker
import androidx.sqlite.db.SimpleSQLiteQuery
import me.proton.core.domain.entity.UserId
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.impl.extensions.toDomain
import proton.android.pass.data.impl.local.LocalItemDataSource
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.log.api.PassLogger

class SearchItemsPagingSource(
    private val searchDbInvalidationTracker: InvalidationTracker,
    private val searchDao: SearchDao,
    private val localItemDataSource: LocalItemDataSource,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val userId: UserId,
    private val baseQuery: String,
    private val queryArgs: Array<Any>,
    private val useFts: Boolean,
    private val pageSize: Int,
    private val initialLoadSize: Int
) : PagingSource<Int, Item>() {

    private val observer = object : InvalidationTracker.Observer(OBSERVED_TABLES) {
        override fun onInvalidated(tables: Set<String>) {
            PassLogger.d(TAG, "Tables invalidated: $tables, refreshing PagingSource")
            invalidate()
        }
    }

    init {
        searchDbInvalidationTracker.addObserver(observer)

        registerInvalidatedCallback {
            searchDbInvalidationTracker.removeObserver(observer)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Item> {
        val page = params.key ?: 0
        // Calculate offset based on page number and consistent page sizes
        val offset = if (page == 0) 0 else initialLoadSize + (page - 1) * pageSize

        return runCatching {
            // Build query with LIMIT and OFFSET
            val paginatedQuery = "$baseQuery LIMIT ${params.loadSize} OFFSET $offset"
            val sqlQuery = SimpleSQLiteQuery(paginatedQuery, queryArgs)

            PassLogger.d(TAG, "Loading page $page with offset $offset, limit ${params.loadSize}")

            // 1. Get ordered IDs from search index
            val searchResults = if (useFts) {
                searchDao.searchFtsPage(sqlQuery)
            } else {
                searchDao.searchPage(sqlQuery)
            }

            if (searchResults.isEmpty()) {
                return@runCatching LoadResult.Page<Int, Item>(
                    data = emptyList(),
                    prevKey = if (page == 0) null else page - 1,
                    nextKey = null
                )
            }

            // 2. Batch fetch items (order NOT guaranteed)
            val pairs = searchResults.map { ShareId(it.shareId) to ItemId(it.itemId) }
            val itemsMap = localItemDataSource.getByShareItemPairs(userId, pairs)
                .associateBy { it.shareId to it.id }

            // 3. Order guaranteed
            val domainItems = encryptionContextProvider.withEncryptionContext {
                searchResults.mapNotNull { row ->
                    itemsMap[row.shareId to row.itemId]?.toDomain(this)
                }
            }

            PassLogger.d(TAG, "Loaded ${domainItems.size} items for page $page")
            LoadResult.Page(
                data = domainItems,
                prevKey = if (page == 0) null else page - 1,
                nextKey = if (searchResults.size < params.loadSize) null else page + 1
            )
        }.getOrElse { e ->
            PassLogger.w(TAG, "Error loading page: ${e.message}")
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Item>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    companion object {
        private const val TAG = "SearchItemsPagingSource"
        private val OBSERVED_TABLES = arrayOf("search_items")
    }
}
