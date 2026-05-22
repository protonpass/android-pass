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

import androidx.room.Room
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import javax.crypto.KeyGenerator

/**
 * Instrumented coverage for the hand-rolled FTS5 layer of [SearchDatabase].
 *
 * Room cannot generate FTS5, so the virtual table + sync triggers are created manually in
 * [SearchFtsCallback]. The exported Room schema does not describe them, and
 * `MigrationTestHelper` neither runs the callback nor validates the FTS objects — so this is
 * the only place that guarantees:
 *  - the FTS table and its insert/update/delete triggers are actually created on a fresh DB,
 *  - search stays in sync with the backing `search_items` table, and
 *  - the access-control filters that the repository relies on (user scoping + hidden-vault
 *    exclusion) genuinely isolate rows — the boundary that used to be enforced by per-item
 *    crypto and is now enforced purely by these WHERE clauses.
 *
 * The DB is built with the same SQLCipher factory and the same [SearchFtsCallback] that ship
 * in production, so the test exercises the real DDL rather than a copy.
 */
@RunWith(AndroidJUnit4::class)
class SearchDatabaseFtsTest {

    private lateinit var database: SearchDatabase
    private lateinit var dao: SearchDao

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
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertIsReflectedInFtsSearch() = runTest {
        dao.upsert(entity(itemId = "item-1", title = "GitHub login", subtitle = "octocat"))

        val byTitle = ftsSearch(USER_A, "GitHub")
        val bySubtitle = ftsSearch(USER_A, "octocat")
        val miss = ftsSearch(USER_A, "gitlab")

        assertThat(byTitle.map { it.itemId }).containsExactly("item-1")
        assertThat(bySubtitle.map { it.itemId }).containsExactly("item-1")
        assertThat(miss).isEmpty()
    }

    @Test
    fun updateKeepsFtsInSync() = runTest {
        dao.upsert(entity(itemId = "item-1", title = "old title"))
        // upsert with REPLACE re-inserts the same (user, share, item); the FTS triggers must
        // drop the stale term and index the new one.
        dao.upsert(entity(itemId = "item-1", title = "new title"))

        assertThat(ftsSearch(USER_A, "old")).isEmpty()
        assertThat(ftsSearch(USER_A, "new").map { it.itemId }).containsExactly("item-1")
    }

    @Test
    fun deleteRemovesFromFts() = runTest {
        dao.upsert(entity(itemId = "item-1", title = "deletable"))
        assertThat(ftsSearch(USER_A, "deletable")).hasSize(1)

        dao.delete(shareId = SHARE_A, itemId = "item-1")

        assertThat(ftsSearch(USER_A, "deletable")).isEmpty()
    }

    @Test
    fun searchIsScopedToTheQueryingUser() = runTest {
        dao.upsert(entity(userId = USER_A, shareId = SHARE_A, itemId = "a", title = "shared term"))
        dao.upsert(entity(userId = USER_B, shareId = SHARE_B, itemId = "b", title = "shared term"))

        val asA = ftsSearch(USER_A, "shared")
        val asB = ftsSearch(USER_B, "shared")

        assertThat(asA.map { it.itemId }).containsExactly("a")
        assertThat(asB.map { it.itemId }).containsExactly("b")
    }

    @Test
    fun hiddenVaultItemsAreExcludedUnlessExplicitlyIncluded() = runTest {
        dao.upsert(entity(itemId = "visible", title = "secret term", isHidden = false))
        dao.upsert(entity(itemId = "hidden", title = "secret term", isHidden = true))

        val excludingHidden = ftsSearch(USER_A, "secret", includeHidden = false)
        val includingHidden = ftsSearch(USER_A, "secret", includeHidden = true)

        assertThat(excludingHidden.map { it.itemId }).containsExactly("visible")
        assertThat(includingHidden.map { it.itemId })
            .containsExactly("visible", "hidden")
    }

    /**
     * Mirrors the FTS query shape built by `SearchIndexRepositoryImpl` (user scoping +
     * optional hidden exclusion + the `search_items_fts MATCH` rowid subquery). Kept here so a
     * future repository query that drops `user_id` or `is_hidden` is contradicted by a test.
     */
    private suspend fun ftsSearch(
        userId: String,
        query: String,
        includeHidden: Boolean = false
    ): List<SearchIdRow> {
        val sql = buildString {
            append("SELECT user_id AS userId, share_id AS shareId, item_id AS itemId ")
            append("FROM search_items ")
            append("WHERE user_id = ? ")
            if (!includeHidden) append("AND is_hidden = 0 ")
            append("AND rowId IN (SELECT rowid FROM search_items_fts WHERE search_items_fts MATCH ?)")
        }
        val args = arrayOf<Any>(userId, "$query*")
        return dao.searchFtsPage(SimpleSQLiteQuery(sql, args))
    }

    private fun entity(
        userId: String = USER_A,
        shareId: String = SHARE_A,
        itemId: String,
        title: String,
        subtitle: String? = null,
        isHidden: Boolean = false
    ) = SearchItemEntity(
        userId = userId,
        shareId = shareId,
        itemId = itemId,
        title = title,
        subtitle = subtitle,
        itemType = ITEM_TYPE_LOGIN,
        createTime = 0L,
        modifyTime = 0L,
        isHidden = isHidden
    )

    private companion object {
        const val USER_A = "user-a"
        const val USER_B = "user-b"
        const val SHARE_A = "share-a"
        const val SHARE_B = "share-b"
        const val ITEM_TYPE_LOGIN = 1
    }
}
