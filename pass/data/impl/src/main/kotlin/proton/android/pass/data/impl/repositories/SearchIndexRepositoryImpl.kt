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

package proton.android.pass.data.impl.repositories

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.room.InvalidationTracker
import androidx.sqlite.db.SimpleSQLiteQuery
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import me.proton.core.domain.entity.UserId
import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.repositories.IndexingStatus
import proton.android.pass.data.api.repositories.ItemTypeCounts
import proton.android.pass.data.api.repositories.SearchIndexRepository
import proton.android.pass.data.api.repositories.SearchSortBy
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.impl.db.entities.ItemEntity
import proton.android.pass.data.impl.local.LocalItemDataSource
import proton.android.pass.data.impl.local.LocalShareDataSource
import proton.android.pass.data.impl.local.search.SearchDao
import proton.android.pass.data.impl.local.search.SearchItemEntity
import proton.android.pass.data.impl.local.search.SearchItemsPagingSource
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ItemState
import proton.android.pass.domain.FolderId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.items.ItemSharedType
import proton.android.pass.log.api.PassLogger
import proton.android.pass.preferences.InternalSettingsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Suppress("LargeClass", "TooManyFunctions")
@Singleton
class SearchIndexRepositoryImpl @Inject constructor(
    private val searchInvalidationTracker: InvalidationTracker,
    private val searchDao: SearchDao,
    private val localItemDataSource: LocalItemDataSource,
    private val localShareDataSource: LocalShareDataSource,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val internalSettingsRepository: InternalSettingsRepository
) : SearchIndexRepository {

    private val _indexingStatus = MutableStateFlow<IndexingStatus>(IndexingStatus.Idle)

    override fun observeIndexingStatus(): Flow<IndexingStatus> = _indexingStatus.asStateFlow()

    override suspend fun indexItem(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ) {
        val item = localItemDataSource.getById(userId, shareId, itemId) ?: return

        val flags = shareIndexFlags(userId)
        val entity = encryptionContextProvider.withEncryptionContext {
            createSearchEntity(
                userId = userId,
                item = item,
                encryptionContext = this,
                isSharedByMe = item.shareId in flags.sharedByMeShareIds,
                isSharedWithMe = item.shareId in flags.sharedWithMeShareIds,
                isHidden = item.shareId !in flags.visibleShareIds
            )
        }
        searchDao.upsert(entity)
    }

    override suspend fun indexItems(userId: UserId, items: List<Pair<ShareId, ItemId>>) {
        if (items.isEmpty()) return

        val itemEntities = items.mapNotNull { (shareId, itemId) ->
            localItemDataSource.getById(userId, shareId, itemId)
        }
        val flags = shareIndexFlags(userId)
        val entities = encryptionContextProvider.withEncryptionContext {
            itemEntities.map { item ->
                createSearchEntity(
                    userId = userId,
                    item = item,
                    encryptionContext = this,
                    isSharedByMe = item.shareId in flags.sharedByMeShareIds,
                    isSharedWithMe = item.shareId in flags.sharedWithMeShareIds,
                    isHidden = item.shareId !in flags.visibleShareIds
                )
            }
        }

        if (entities.isNotEmpty()) {
            searchDao.upsertAll(entities)
            PassLogger.i(TAG, "Indexed ${entities.size} items")
        }
    }

    private suspend fun shareIndexFlags(userId: UserId): ShareIndexFlags {
        val visibleShareIds = localShareDataSource
            .observeAllActiveSharesForUser(userId, includeHidden = false)
            .first()
            .map { it.id }
            .toSet()
        val sharedByMeShareIds = localShareDataSource
            .observeSharedByMeIds(userId, includeHidden = true)
            .first()
            .map { it.id }
            .toSet()
        val sharedWithMeShareIds = localShareDataSource
            .observeSharedWithMeIds(userId, includeHidden = true)
            .first()
            .map { it.id }
            .toSet()
        return ShareIndexFlags(visibleShareIds, sharedByMeShareIds, sharedWithMeShareIds)
    }

    private data class ShareIndexFlags(
        val visibleShareIds: Set<String>,
        val sharedByMeShareIds: Set<String>,
        val sharedWithMeShareIds: Set<String>
    )

    override suspend fun updateShareHidden(shareId: ShareId, isHidden: Boolean) {
        searchDao.updateHiddenForShare(shareId.id, isHidden)
    }

    override suspend fun removeItemFromIndex(shareId: ShareId, itemId: ItemId) {
        searchDao.delete(shareId.id, itemId.id)
    }

    override suspend fun updateItemState(
        shareId: ShareId,
        itemId: ItemId,
        itemState: ItemState
    ) {
        val stateValue = when (itemState) {
            ItemState.Active -> 0
            ItemState.Trashed -> 1
        }
        searchDao.updateItemState(shareId.id, itemId.id, stateValue)
    }

    override suspend fun removeShareFromIndex(shareId: ShareId) {
        searchDao.clearForShare(shareId.id)
    }

    override suspend fun rebuildIndex(userId: UserId) {
        withContext(Dispatchers.IO) {
            PassLogger.i(TAG, "Starting index rebuild")
            _indexingStatus.value = IndexingStatus.Indexing

            runCatching {
                val result = performRebuildIndex(userId)
                val completionMessage = "Index rebuild complete. Indexed ${result.indexed} items"
                if (result.indexed == result.total) {
                    PassLogger.i(TAG, completionMessage)
                } else {
                    PassLogger.w(TAG, "$completionMessage, expected ${result.total}")
                }
                markRebuildComplete(userId)
                _indexingStatus.value = IndexingStatus.Ready
            }.onFailure { error ->
                _indexingStatus.value = IndexingStatus.Idle
                if (error !is CancellationException) {
                    PassLogger.e(
                        TAG,
                        "Index rebuild failed: ${error.javaClass.simpleName}"
                    )
                }
                throw error
            }
        }
    }

    @Suppress("LongMethod")
    private suspend fun performRebuildIndex(userId: UserId): IndexRebuildResult {
        // Clear existing index
        searchDao.clearForUser(userId.id)

        val shareIds = localShareDataSource
            .observeAllActiveSharesForUser(userId, includeHidden = true)
            .first()
            .map { ShareId(it.id) }

        if (shareIds.isEmpty()) {
            PassLogger.i(TAG, "No shares found for index rebuild")
            return IndexRebuildResult(total = 0, indexed = 0)
        }

        val flags = shareIndexFlags(userId)
        val activeItems = localItemDataSource.countItemsForIndex(userId, shareIds, ItemState.Active)
        val trashedItems = localItemDataSource.countItemsForIndex(userId, shareIds, ItemState.Trashed)
        val total = activeItems + trashedItems
        if (total == 0) {
            PassLogger.i(TAG, "No items found for index rebuild")
            return IndexRebuildResult(total = 0, indexed = 0)
        }

        val indexed = indexItemsForShares(userId, shareIds, flags) { current ->
            _indexingStatus.value = IndexingStatus.InProgress(current = current, total = total)
        }
        return IndexRebuildResult(total = total, indexed = indexed)
    }

    private suspend fun indexItemsForShares(
        userId: UserId,
        shareIds: List<ShareId>,
        flags: ShareIndexFlags,
        onPageIndexed: (Int) -> Unit = {}
    ): Int {
        var indexed = 0
        // Page through the items with a keyset cursor (rowid) so the full corpus is never
        // held in memory at once — only one page of decrypted entities at a time.
        for (state in ItemState.entries) {
            var afterRowId = 0L
            while (true) {
                val page = localItemDataSource.getItemsPageForIndex(
                    userId = userId,
                    shareIds = shareIds,
                    itemState = state,
                    afterRowId = afterRowId,
                    limit = REBUILD_PAGE_SIZE
                )
                if (page.isEmpty()) break

                val entities = encryptionContextProvider.withEncryptionContext {
                    page.map { row ->
                        createSearchEntity(
                            userId = userId,
                            item = row.item,
                            encryptionContext = this,
                            isSharedByMe = row.item.shareId in flags.sharedByMeShareIds,
                            isSharedWithMe = row.item.shareId in flags.sharedWithMeShareIds,
                            isHidden = row.item.shareId !in flags.visibleShareIds
                        )
                    }
                }
                searchDao.upsertAll(entities)

                indexed += page.size
                afterRowId = page.last().rowId
                onPageIndexed(indexed)
                kotlinx.coroutines.yield()
            }
        }
        return indexed
    }

    override suspend fun indexShare(userId: UserId, shareId: ShareId) {
        withContext(Dispatchers.IO) {
            searchDao.clearForShare(shareId.id)
            val flags = shareIndexFlags(userId)
            indexItemsForShares(userId, listOf(shareId), flags)
        }
    }

    override suspend fun needsRebuild(userId: UserId): Boolean {
        val hasIndex = searchDao.hasIndexForUser(userId.id)
        val lastRebuildTime = getLastRebuildTime(userId)

        // Needs rebuild if no index exists or if it was never built
        return !hasIndex || lastRebuildTime == 0L
    }

    override suspend fun checkAndRebuildIfNeeded(userId: UserId) {
        if (needsRebuild(userId)) {
            PassLogger.i(TAG, "Search index needs rebuild, starting automatic rebuild")
            rebuildIndex(userId)
        }
    }

    private data class IndexRebuildResult(
        val total: Int,
        val indexed: Int
    )

    override suspend fun clearIndex(userId: UserId) {
        searchDao.clearForUser(userId.id)
        clearRebuildTime(userId)
    }

    private fun createSearchEntity(
        userId: UserId,
        item: ItemEntity,
        encryptionContext: EncryptionContext,
        isSharedByMe: Boolean = false,
        isSharedWithMe: Boolean = false,
        isHidden: Boolean = false
    ): SearchItemEntity {
        val (title, subtitle) = with(encryptionContext) {
            val decryptedTitle = decrypt(item.encryptedTitle)
            val decryptedNote = decrypt(item.encryptedNote)
            val searchableContent = SearchableContentExtractor.extract(item, this)
            val fullSubtitle = listOfNotNull(
                decryptedNote.takeIf { it.isNotBlank() },
                searchableContent.takeIf { it.isNotBlank() }
            ).joinToString(" ").takeIf { it.isNotBlank() }
            Pair(decryptedTitle, fullSubtitle)
        }

        val itemStateValue = when (item.state) {
            ItemState.Active.value -> 0
            ItemState.Trashed.value -> 1
            else -> 0
        }

        return SearchItemEntity(
            userId = userId.id,
            shareId = item.shareId,
            folderId = item.folderId,
            itemId = item.id,
            title = title,
            subtitle = subtitle,
            itemType = item.itemType,
            createTime = item.createTime,
            modifyTime = item.modifyTime,
            itemState = itemStateValue,
            isSharedByMe = isSharedByMe,
            isSharedWithMe = isSharedWithMe,
            lastAutofillTime = item.lastUsedTime,
            hasTotp = item.hasTotp ?: false,
            isHidden = isHidden
        )
    }

    private fun getSortClause(sortBy: SearchSortBy): String = when (sortBy) {
        SearchSortBy.TITLE_ASC -> " ORDER BY title COLLATE NOCASE ASC"
        SearchSortBy.TITLE_DESC -> " ORDER BY title COLLATE NOCASE DESC"
        SearchSortBy.CREATION_DATE_DESC -> " ORDER BY create_time DESC"
        SearchSortBy.CREATION_DATE_ASC -> " ORDER BY create_time ASC"
        SearchSortBy.MODIFICATION_DATE_DESC -> " ORDER BY modify_time DESC"
        SearchSortBy.MOST_RECENT,
        // RELEVANCE is handled inline (bm25) in the FTS query; this is a defensive fallback.
        SearchSortBy.RELEVANCE -> " ORDER BY MAX(COALESCE(last_autofill_time, 0), modify_time) DESC"
    }

    private fun ItemState.toInt(): Int = when (this) {
        ItemState.Active -> 0
        ItemState.Trashed -> 1
    }

    private fun appendItemTypeFilter(
        sb: StringBuilder,
        args: MutableList<Any>,
        itemTypeFilter: ItemTypeFilter,
        tablePrefix: String
    ) {
        when (itemTypeFilter) {
            ItemTypeFilter.All -> {
                /* No filter */
            }

            ItemTypeFilter.Logins -> {
                sb.append(" AND ${tablePrefix}item_type = ?")
                args.add(ITEM_TYPE_LOGIN)
            }

            ItemTypeFilter.LoginWithTotp -> {
                sb.append(" AND ${tablePrefix}item_type = ? AND ${tablePrefix}has_totp = 1")
                args.add(ITEM_TYPE_LOGIN)
            }

            ItemTypeFilter.Aliases -> {
                sb.append(" AND ${tablePrefix}item_type = ?")
                args.add(ITEM_TYPE_ALIAS)
            }

            ItemTypeFilter.Notes -> {
                sb.append(" AND ${tablePrefix}item_type = ?")
                args.add(ITEM_TYPE_NOTE)
            }

            ItemTypeFilter.CreditCards -> {
                sb.append(" AND ${tablePrefix}item_type = ?")
                args.add(ITEM_TYPE_CREDIT_CARD)
            }

            ItemTypeFilter.Identity -> {
                sb.append(" AND ${tablePrefix}item_type = ?")
                args.add(ITEM_TYPE_IDENTITY)
            }

            ItemTypeFilter.Custom -> {
                // Custom includes Custom, WifiNetwork, and SSHKey
                sb.append(" AND ${tablePrefix}item_type IN (?, ?, ?)")
                args.add(ITEM_TYPE_CUSTOM)
                args.add(ITEM_TYPE_WIFI_NETWORK)
                args.add(ITEM_TYPE_SSH_KEY)
            }
        }
    }

    private suspend fun markRebuildComplete(userId: UserId) {
        val currentTime = System.currentTimeMillis()
        internalSettingsRepository.setSearchIndexRebuildTime(userId, currentTime)
    }

    private suspend fun getLastRebuildTime(userId: UserId): Long =
        internalSettingsRepository.getSearchIndexRebuildTime(userId)

    private suspend fun clearRebuildTime(userId: UserId) {
        internalSettingsRepository.setSearchIndexRebuildTime(userId, 0L)
    }

    @SuppressWarnings("LongMethod", "LongParameterList")
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
        val useFts = !query.isNullOrBlank()
        // bm25 relevance only makes sense with an FTS match; fall back otherwise.
        val effectiveSortBy = if (sortBy == SearchSortBy.RELEVANCE && !useFts) {
            SearchSortBy.MOST_RECENT
        } else {
            sortBy
        }
        val queryParts = if (useFts) {
            val ftsQuery = FtsQueryBuilder.build(query)
            buildPagingSearchQueryParts(
                userId = userId,
                ftsQuery = ftsQuery,
                sortBy = effectiveSortBy,
                shareIds = shareIds,
                folderId = folderId,
                itemState = itemState,
                itemSharedType = itemSharedType,
                itemTypeFilter = itemTypeFilter,
                includeHidden = includeHidden
            )
        } else {
            buildPagingGetAllQueryParts(
                userId = userId,
                sortBy = effectiveSortBy,
                shareIds = shareIds,
                folderId = folderId,
                itemState = itemState,
                itemSharedType = itemSharedType,
                itemTypeFilter = itemTypeFilter,
                includeHidden = includeHidden
            )
        }

        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                prefetchDistance = PREFETCH_DISTANCE,
                enablePlaceholders = false,
                initialLoadSize = INITIAL_LOAD_SIZE
            ),
            pagingSourceFactory = {
                SearchItemsPagingSource(
                    searchDbInvalidationTracker = searchInvalidationTracker,
                    searchDao = searchDao,
                    localItemDataSource = localItemDataSource,
                    encryptionContextProvider = encryptionContextProvider,
                    userId = userId,
                    baseQuery = queryParts.queryString,
                    queryArgs = queryParts.args,
                    useFts = useFts,
                    pageSize = PAGE_SIZE,
                    initialLoadSize = INITIAL_LOAD_SIZE
                )
            }
        ).flow
    }

    override fun observeItemTypeCounts(
        userId: UserId,
        shareIds: List<ShareId>?,
        folderId: FolderId?,
        itemState: ItemState?,
        itemSharedType: ItemSharedType?,
        query: String?,
        includeHidden: Boolean
    ): Flow<ItemTypeCounts> {
        val ftsQuery = if (!query.isNullOrBlank()) FtsQueryBuilder.build(query) else null
        val sqlQuery =
            buildItemTypeCountQuery(userId, shareIds, folderId, itemState, itemSharedType, ftsQuery, includeHidden)
        return searchDao.countByItemType(sqlQuery).map { rows ->
            val countMap = rows.associate { it.itemType to it.count }
            ItemTypeCounts(
                loginCount = countMap[ITEM_TYPE_LOGIN] ?: 0,
                aliasCount = countMap[ITEM_TYPE_ALIAS] ?: 0,
                noteCount = countMap[ITEM_TYPE_NOTE] ?: 0,
                creditCardCount = countMap[ITEM_TYPE_CREDIT_CARD] ?: 0,
                identityCount = countMap[ITEM_TYPE_IDENTITY] ?: 0,
                customCount = (countMap[ITEM_TYPE_CUSTOM] ?: 0) +
                    (countMap[ITEM_TYPE_WIFI_NETWORK] ?: 0) +
                    (countMap[ITEM_TYPE_SSH_KEY] ?: 0)
            )
        }
    }

    @SuppressWarnings("LongParameterList")
    private fun buildItemTypeCountQuery(
        userId: UserId,
        shareIds: List<ShareId>?,
        folderId: FolderId?,
        itemState: ItemState?,
        itemSharedType: ItemSharedType?,
        ftsQuery: String?,
        includeHidden: Boolean
    ): SimpleSQLiteQuery {
        val args = mutableListOf<Any>()

        val sb = StringBuilder()
        sb.append(
            """
                SELECT item_type AS itemType, COUNT(*) AS count
                FROM search_items
                WHERE user_id = ?
            """.trimIndent()
        )
        args.add(userId.id)

        if (!includeHidden) {
            sb.append(" AND is_hidden = 0")
        }

        // Filter by FTS query if provided
        if (!ftsQuery.isNullOrBlank()) {
            sb.append(" AND rowid IN (SELECT rowid FROM search_items_fts WHERE search_items_fts MATCH ?)")
            args.add(ftsQuery)
        }

        // Filter by itemState
        val stateValue = itemState?.toInt() ?: ItemState.Active.toInt()
        sb.append(" AND item_state = ?")
        args.add(stateValue)

        // Filter by shareIds
        if (shareIds != null && shareIds.isNotEmpty()) {
            val placeholders = shareIds.joinToString(",") { "?" }
            sb.append(" AND share_id IN ($placeholders)")
            args.addAll(shareIds.map { it.id })
        }

        // Filter by folderId
        if (folderId != null) {
            sb.append(" AND folder_id = ?")
            args.add(folderId.id)
        }

        // Filter by shared type
        when (itemSharedType) {
            ItemSharedType.SharedByMe -> sb.append(" AND is_shared_by_me = 1")
            ItemSharedType.SharedWithMe -> sb.append(" AND is_shared_with_me = 1")
            null -> {
                /* No filter */
            }
        }

        sb.append(" GROUP BY item_type")

        return SimpleSQLiteQuery(sb.toString(), args.toTypedArray())
    }

    @SuppressWarnings("LongMethod", "LongParameterList")
    private fun buildPagingSearchQueryParts(
        userId: UserId,
        ftsQuery: String,
        sortBy: SearchSortBy,
        shareIds: List<ShareId>?,
        folderId: FolderId?,
        itemState: ItemState?,
        itemSharedType: ItemSharedType?,
        itemTypeFilter: ItemTypeFilter,
        includeHidden: Boolean
    ): QueryParts {
        val args = mutableListOf<Any>()
        val useRelevance = sortBy == SearchSortBy.RELEVANCE

        val sb = StringBuilder()
        if (useRelevance) {
            // JOIN the FTS table so bm25() is available for ranking.
            sb.append(
                """
                    SELECT si.user_id AS userId, si.share_id AS shareId, si.item_id AS itemId
                    FROM search_items si
                    JOIN search_items_fts ON search_items_fts.rowid = si.rowId
                    WHERE search_items_fts MATCH ?
                    AND si.user_id = ?
                """.trimIndent()
            )
        } else {
            sb.append(
                """
                    SELECT si.user_id AS userId, si.share_id AS shareId, si.item_id AS itemId
                    FROM search_items si
                    WHERE si.rowId IN (
                        SELECT rowid FROM search_items_fts WHERE search_items_fts MATCH ?
                    )
                    AND si.user_id = ?
                """.trimIndent()
            )
        }
        args.add(ftsQuery)
        args.add(userId.id)

        if (!includeHidden) {
            sb.append(" AND si.is_hidden = 0")
        }

        // Filter by itemState
        val stateValue = itemState?.toInt() ?: ItemState.Active.toInt()
        sb.append(" AND si.item_state = ?")
        args.add(stateValue)

        // Filter by shareIds
        if (shareIds != null && shareIds.isNotEmpty()) {
            val placeholders = shareIds.joinToString(",") { "?" }
            sb.append(" AND si.share_id IN ($placeholders)")
            args.addAll(shareIds.map { it.id })
        }

        // Filter by folderId
        if (folderId != null) {
            sb.append(" AND si.folder_id = ?")
            args.add(folderId.id)
        }

        // Filter by shared type
        when (itemSharedType) {
            ItemSharedType.SharedByMe -> sb.append(" AND si.is_shared_by_me = 1")
            ItemSharedType.SharedWithMe -> sb.append(" AND si.is_shared_with_me = 1")
            null -> { /* No filter */ }
        }

        // Filter by item type
        appendItemTypeFilter(sb, args, itemTypeFilter, "si.")

        // Add sorting (bm25 ascending = most relevant first)
        if (useRelevance) {
            sb.append(" ORDER BY bm25(search_items_fts)")
        } else {
            sb.append(getSortClause(sortBy))
        }

        return QueryParts(sb.toString(), args.toTypedArray())
    }

    @SuppressWarnings("LongMethod", "LongParameterList")
    private fun buildPagingGetAllQueryParts(
        userId: UserId,
        sortBy: SearchSortBy,
        shareIds: List<ShareId>?,
        folderId: FolderId?,
        itemState: ItemState?,
        itemSharedType: ItemSharedType?,
        itemTypeFilter: ItemTypeFilter,
        includeHidden: Boolean
    ): QueryParts {
        val args = mutableListOf<Any>()

        val sb = StringBuilder()
        sb.append(
            """
                SELECT user_id AS userId, share_id AS shareId, item_id AS itemId
                FROM search_items
                WHERE user_id = ?
            """.trimIndent()
        )
        args.add(userId.id)

        if (!includeHidden) {
            sb.append(" AND is_hidden = 0")
        }

        // Filter by itemState
        val stateValue = itemState?.toInt() ?: ItemState.Active.toInt()
        sb.append(" AND item_state = ?")
        args.add(stateValue)

        // Filter by shareIds
        if (shareIds != null && shareIds.isNotEmpty()) {
            val placeholders = shareIds.joinToString(",") { "?" }
            sb.append(" AND share_id IN ($placeholders)")
            args.addAll(shareIds.map { it.id })
        }

        // Filter by folderId
        if (folderId != null) {
            sb.append(" AND folder_id = ?")
            args.add(folderId.id)
        }

        // Filter by shared type
        when (itemSharedType) {
            ItemSharedType.SharedByMe -> sb.append(" AND is_shared_by_me = 1")
            ItemSharedType.SharedWithMe -> sb.append(" AND is_shared_with_me = 1")
            null -> { /* No filter */ }
        }

        // Filter by item type
        appendItemTypeFilter(sb, args, itemTypeFilter, "")

        // Add sorting
        sb.append(getSortClause(sortBy))

        return QueryParts(sb.toString(), args.toTypedArray())
    }

    private data class QueryParts(
        val queryString: String,
        val args: Array<Any>
    )

    companion object {
        private const val TAG = "SearchIndexRepositoryImpl"
        private const val REBUILD_PAGE_SIZE = 50

        // Item type constants matching ItemCategory values
        private const val ITEM_TYPE_LOGIN = 0
        private const val ITEM_TYPE_ALIAS = 1
        private const val ITEM_TYPE_NOTE = 2
        private const val ITEM_TYPE_CREDIT_CARD = 4
        private const val ITEM_TYPE_IDENTITY = 5
        private const val ITEM_TYPE_CUSTOM = 6
        private const val ITEM_TYPE_WIFI_NETWORK = 7
        private const val ITEM_TYPE_SSH_KEY = 8
        private const val PAGE_SIZE = 30
        private const val PREFETCH_DISTANCE = 10
        private const val INITIAL_LOAD_SIZE = 60
    }
}
