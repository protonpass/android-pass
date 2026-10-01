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

import androidx.room.Room
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import proton.android.pass.common.fakes.FakeAppDispatchers
import proton.android.pass.crypto.fakes.context.FakeEncryptionContextProvider
import proton.android.pass.data.api.repositories.ItemTypeCounts
import proton.android.pass.data.api.repositories.SearchSortBy
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.impl.fakes.FakeLocalFolderDataSource
import proton.android.pass.data.impl.fakes.FakeLocalItemDataSource
import proton.android.pass.data.impl.fakes.FakeLocalShareDataSource
import proton.android.pass.data.impl.local.search.SearchDao
import proton.android.pass.data.impl.local.search.SearchDatabase
import proton.android.pass.data.impl.local.search.SearchFtsCallback
import proton.android.pass.data.impl.local.search.SearchIdRow
import proton.android.pass.data.impl.local.search.SearchItemEntity
import proton.android.pass.domain.ItemState
import proton.android.pass.preferences.FakeInternalSettingsRepository
import javax.crypto.KeyGenerator

@RunWith(AndroidJUnit4::class)
class SearchIndexRepositoryMultiUserTest {

    private lateinit var database: SearchDatabase
    private lateinit var dao: SearchDao
    private lateinit var repository: SearchIndexRepositoryImpl

    @Before
    fun setup() {
        System.loadLibrary("sqlcipher")
        val key = KeyGenerator.getInstance("AES").apply { init(256) }.generateKey().encoded
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            SearchDatabase::class.java
        )
            .openHelperFactory(SupportOpenHelperFactory(key))
            .fallbackToDestructiveMigration()
            .addCallback(SearchFtsCallback)
            .build()
        dao = database.searchDao()
        repository = SearchIndexRepositoryImpl(
            searchInvalidationTracker = database.invalidationTracker,
            searchDao = dao,
            localItemDataSource = FakeLocalItemDataSource(),
            localShareDataSource = FakeLocalShareDataSource(),
            encryptionContextProvider = FakeEncryptionContextProvider(),
            internalSettingsRepository = FakeInternalSettingsRepository(),
            appDispatchers = FakeAppDispatchers(),
            localFolderDataSource = FakeLocalFolderDataSource()
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun getAllReturnsItemsOfEveryQueriedUser() = runTest {
        insertTwoAccounts()

        val bothUsers = getAll(listOf(USER_A, USER_B))
        val onlyUserA = getAll(listOf(USER_A))

        assertThat(bothUsers.map { it.itemId }).containsExactly(ITEM_A, ITEM_B, ITEM_SHARED)
        assertThat(bothUsers.map { it.userId }.toSet()).containsExactly(USER_A.id, USER_B.id)
        assertThat(onlyUserA.map { it.userId }.toSet()).containsExactly(USER_A.id)
        assertThat(onlyUserA.map { it.itemId }).containsExactly(ITEM_A, ITEM_SHARED)
    }

    @Test
    fun getAllReturnsCrossAccountDuplicateOnceAsOwnerCopy() = runTest {
        insertTwoAccounts()

        val duplicates = getAll(listOf(USER_A, USER_B)).filter { it.itemId == ITEM_SHARED }

        assertThat(duplicates).containsExactly(SearchIdRow(USER_A.id, SHARE_A, ITEM_SHARED))
    }

    @Test
    fun ftsSearchReturnsCrossAccountDuplicateOnceAsOwnerCopy() = runTest {
        insertTwoAccounts()

        val relevance = ftsSearch(listOf(USER_A, USER_B), SearchSortBy.RELEVANCE)
        val byTitle = ftsSearch(listOf(USER_A, USER_B), SearchSortBy.TITLE_ASC)

        listOf(relevance, byTitle).forEach { rows ->
            assertThat(rows.map { it.itemId }).containsExactly(ITEM_A, ITEM_B, ITEM_SHARED)
            assertThat(rows.filter { it.itemId == ITEM_SHARED })
                .containsExactly(SearchIdRow(USER_A.id, SHARE_A, ITEM_SHARED))
        }
    }

    @Test
    fun itemTypeCountsMatchListSizeForMultipleUsers() = runTest {
        insertTwoAccounts()
        val userIds = listOf(USER_A, USER_B)

        val counts = repository.observeItemTypeCounts(userIds = userIds).first()
        val rows = getAll(userIds)

        assertThat(counts).isEqualTo(ItemTypeCounts(loginCount = 2, aliasCount = 1))
        assertThat(counts.total()).isEqualTo(rows.size)
    }

    @Test
    fun emptyUserIdsReturnEmptyCounts() = runTest {
        insertTwoAccounts()

        val counts = repository.observeItemTypeCounts(userIds = emptyList()).first()

        assertThat(counts).isEqualTo(ItemTypeCounts.EMPTY)
    }

    private suspend fun insertTwoAccounts() {
        dao.upsert(
            entity(USER_B, SHARE_B, ITEM_SHARED, ITEM_TYPE_LOGIN, isSharedWithMe = true)
        )
        dao.upsert(
            entity(USER_A, SHARE_A, ITEM_SHARED, ITEM_TYPE_LOGIN, isSharedByMe = true)
        )
        dao.upsert(entity(USER_A, SHARE_A, ITEM_A, ITEM_TYPE_LOGIN))
        dao.upsert(entity(USER_B, SHARE_B, ITEM_B, ITEM_TYPE_ALIAS))
    }

    private suspend fun getAll(userIds: List<UserId>): List<SearchIdRow> {
        val parts = repository.buildPagingGetAllQueryParts(
            userIds = userIds,
            sortBy = SearchSortBy.TITLE_ASC,
            shareIds = null,
            folderId = null,
            itemState = ItemState.Active,
            itemSharedType = null,
            itemTypeFilter = ItemTypeFilter.All,
            includeHidden = false
        )
        return dao.searchPage(SimpleSQLiteQuery(parts.queryString, parts.args))
    }

    private suspend fun ftsSearch(userIds: List<UserId>, sortBy: SearchSortBy): List<SearchIdRow> {
        val parts = repository.buildPagingSearchQueryParts(
            userIds = userIds,
            ftsQuery = FtsQueryBuilder.build(COMMON_TERM),
            sortBy = sortBy,
            shareIds = null,
            folderIds = null,
            itemState = ItemState.Active,
            itemSharedType = null,
            itemTypeFilter = ItemTypeFilter.All,
            includeHidden = false
        )
        return dao.searchFtsPage(SimpleSQLiteQuery(parts.queryString, parts.args))
    }

    private fun ItemTypeCounts.total(): Int =
        loginCount + aliasCount + noteCount + creditCardCount + identityCount + customCount

    private fun entity(
        userId: UserId,
        shareId: String,
        itemId: String,
        itemType: Int,
        isSharedByMe: Boolean = false,
        isSharedWithMe: Boolean = false
    ) = SearchItemEntity(
        userId = userId.id,
        shareId = shareId,
        itemId = itemId,
        title = "$COMMON_TERM $itemId",
        subtitle = null,
        itemType = itemType,
        createTime = 0L,
        modifyTime = 0L,
        isSharedByMe = isSharedByMe,
        isSharedWithMe = isSharedWithMe
    )

    private companion object {
        val USER_A = UserId("user-a")
        val USER_B = UserId("user-b")
        const val SHARE_A = "share-a"
        const val SHARE_B = "share-b"
        const val ITEM_A = "item-a"
        const val ITEM_B = "item-b"
        const val ITEM_SHARED = "item-shared"
        const val COMMON_TERM = "vaultitem"
        const val ITEM_TYPE_LOGIN = 0
        const val ITEM_TYPE_ALIAS = 1
    }
}
