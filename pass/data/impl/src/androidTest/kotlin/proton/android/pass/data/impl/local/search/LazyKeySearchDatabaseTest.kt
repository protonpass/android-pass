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

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.paging.PagingSource
import androidx.room.InvalidationTracker
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import proton.android.pass.common.api.AppDispatchers
import proton.android.pass.commonrust.fakes.FakeItemContentSerializer
import proton.android.pass.crypto.fakes.context.FakeEncryptionContextProvider
import proton.android.pass.data.impl.fakes.FakeLocalItemDataSource
import java.io.File
import java.util.UUID
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class LazyKeySearchDatabaseTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val databaseName = "lazy-key-${UUID.randomUUID()}.db"
    private val databases = mutableListOf<SearchDatabase>()

    @Before
    fun setup() {
        System.loadLibrary("sqlcipher")
    }

    @After
    fun tearDown() {
        databases.forEach { it.close() }
        context.deleteDatabase(databaseName)
    }

    @Test
    fun persistentKeyCreatesAnEncryptedDatabaseFile() = runTest {
        val database = buildDatabase(SearchDatabaseKey.Persistent(KEY_A))

        database.searchDao().upsert(entity(itemId = "item-1", title = "GitHub login"))

        val file = context.getDatabasePath(databaseName)
        assertThat(file.exists()).isTrue()
        assertThat(file.header()).isNotEqualTo(SQLITE_PLAIN_HEADER)
    }

    @Test
    fun persistentKeyKeepsDataAcrossReopenWithTheSameKey() = runTest {
        buildDatabase(SearchDatabaseKey.Persistent(KEY_A)).run {
            searchDao().upsert(entity(itemId = "item-1", title = "GitHub login"))
            close()
        }

        val reopened = buildDatabase(SearchDatabaseKey.Persistent(KEY_A))

        assertThat(reopened.rowCount()).isEqualTo(1)
        assertThat(reopened.ftsMatchCount("GitHub")).isEqualTo(1)
    }

    @Test
    fun persistentKeyCannotBeReopenedWithADifferentKey() = runTest {
        buildDatabase(SearchDatabaseKey.Persistent(KEY_A)).run {
            searchDao().upsert(entity(itemId = "item-1", title = "GitHub login"))
            close()
        }

        val reopened = buildDatabase(SearchDatabaseKey.Persistent(KEY_B))

        val result = runCatching { reopened.rowCount() }
        assertThat(result.isFailure).isTrue()
    }

    @Test
    fun persistentKeyCreatesFtsObjects() {
        val database = buildDatabase(SearchDatabaseKey.Persistent(KEY_A))

        assertThat(database.ftsObjectCount()).isEqualTo(FTS_OBJECT_COUNT)
    }

    @Test
    fun unavailableKeyWorksInMemoryWithoutCreatingAFile() = runTest {
        val database = buildDatabase(SearchDatabaseKey.Unavailable)

        database.searchDao().upsert(entity(itemId = "item-1", title = "GitHub login"))

        assertThat(database.rowCount()).isEqualTo(1)
        assertThat(database.ftsMatchCount("GitHub")).isEqualTo(1)
        assertThat(context.getDatabasePath(databaseName).exists()).isFalse()
    }

    @Test
    fun unavailableKeyCreatesFtsObjects() {
        val database = buildDatabase(SearchDatabaseKey.Unavailable)

        assertThat(database.ftsObjectCount()).isEqualTo(FTS_OBJECT_COUNT)
    }

    @Test
    fun unavailableKeyDoesNotKeepDataAcrossReopen() = runTest {
        buildDatabase(SearchDatabaseKey.Unavailable).run {
            searchDao().upsert(entity(itemId = "item-1", title = "GitHub login"))
            close()
        }

        val reopened = buildDatabase(SearchDatabaseKey.Unavailable)

        assertThat(reopened.rowCount()).isEqualTo(0)
    }

    @Test
    fun unavailableKeyAfterPersistentKeyDoesNotTouchTheEncryptedFile() = runTest {
        buildDatabase(SearchDatabaseKey.Persistent(KEY_A)).run {
            searchDao().upsert(entity(itemId = "item-1", title = "GitHub login"))
            close()
        }
        val file = context.getDatabasePath(databaseName)
        val sizeBefore = file.length()

        buildDatabase(SearchDatabaseKey.Unavailable).run {
            searchDao().upsert(entity(itemId = "item-2", title = "GitLab login"))
            close()
        }

        assertThat(file.length()).isEqualTo(sizeBefore)
        val reopened = buildDatabase(SearchDatabaseKey.Persistent(KEY_A))
        assertThat(reopened.rowCount()).isEqualTo(1)
    }

    @Test
    fun buildingTheDatabaseAndItsProvidedDependenciesDoesNotResolveTheKey() {
        val provider = GatedKeyProvider(SearchDatabaseKey.Persistent(KEY_A))
        val database = buildDatabase(provider)

        database.searchDao()
        database.invalidationTracker
        pagingSource(database)

        assertThat(provider.calls).isEqualTo(0)
        assertThat(context.getDatabasePath(databaseName).exists()).isFalse()
    }

    @Test
    fun creatingThePagingSourceOnAnOpenDatabaseDoesNotRegisterAnInvalidationObserver() = runTest {
        val database = buildDatabase(SearchDatabaseKey.Persistent(KEY_A))
        database.rowCount()
        val source = pagingSource(database)
        val invalidated = CountDownLatch(1)
        source.registerInvalidatedCallback { invalidated.countDown() }
        val controlNotified = CountDownLatch(1)
        database.invalidationTracker.addObserver(object : InvalidationTracker.Observer("search_items") {
            override fun onInvalidated(tables: Set<String>) = controlNotified.countDown()
        })

        database.searchDao().upsert(entity(itemId = "item-1", title = "GitHub login"))

        assertThat(controlNotified.await(WAIT_SECONDS, TimeUnit.SECONDS)).isTrue()
        assertThat(invalidated.count).isEqualTo(1)
    }

    @Test
    fun cancellingLoadBeforeTheObserverIsRegisteredDoesNotSkipRegistrationOnTheNextLoad() = runBlocking {
        val database = buildDatabase(SearchDatabaseKey.Persistent(KEY_A))
        database.rowCount()
        val ioDispatcher = PausableDispatcher()
        val source = pagingSource(database, PausableAppDispatchers(ioDispatcher))
        val invalidated = CountDownLatch(1)
        source.registerInvalidatedCallback { invalidated.countDown() }
        val params = PagingSource.LoadParams.Refresh<Int>(key = null, loadSize = PAGE_SIZE, placeholdersEnabled = false)

        val firstLoad = launch(Dispatchers.Default) { source.load(params) }
        assertThat(ioDispatcher.queued.await(WAIT_SECONDS, TimeUnit.SECONDS)).isTrue()
        firstLoad.cancel()
        ioDispatcher.resume()
        firstLoad.join()
        source.load(params)
        database.searchDao().upsert(entity(itemId = "item-1", title = "GitHub login"))

        assertThat(invalidated.await(WAIT_SECONDS, TimeUnit.SECONDS)).isTrue()
    }

    @Test
    fun pagingSourceCreatedAndLoadedOnTheMainThreadDoesNotBlockIt() {
        val provider = GatedKeyProvider(SearchDatabaseKey.Persistent(KEY_A))
        val database = buildDatabase(provider)
        val scope = CoroutineScope(Dispatchers.Main)
        val loaded = CountDownLatch(1)
        var result: PagingSource.LoadResult<Int, *>? = null

        scope.launch {
            result = pagingSource(database).load(
                PagingSource.LoadParams.Refresh(key = null, loadSize = PAGE_SIZE, placeholdersEnabled = false)
            )
            loaded.countDown()
        }

        assertThat(provider.entered.await(WAIT_SECONDS, TimeUnit.SECONDS)).isTrue()
        val mainResponded = CountDownLatch(1)
        Handler(Looper.getMainLooper()).post { mainResponded.countDown() }
        val mainWasResponsive = mainResponded.await(WAIT_SECONDS, TimeUnit.SECONDS)
        provider.gate.countDown()
        val finished = loaded.await(WAIT_SECONDS, TimeUnit.SECONDS)
        scope.cancel()

        assertThat(mainWasResponsive).isTrue()
        assertThat(finished).isTrue()
        assertThat(result).isInstanceOf(PagingSource.LoadResult.Page::class.java)
    }

    private fun buildDatabase(key: SearchDatabaseKey): SearchDatabase = buildDatabase(FixedKeyProvider(key))

    private fun buildDatabase(keyProvider: SearchDatabaseKeyProvider): SearchDatabase =
        buildSearchDatabase(context, keyProvider, RealAppDispatchers, databaseName).also { databases += it }

    private fun pagingSource(
        database: SearchDatabase,
        appDispatchers: AppDispatchers = RealAppDispatchers
    ): SearchItemsPagingSource {
        return SearchItemsPagingSource(
            itemContentSerializer = FakeItemContentSerializer(),
            searchDbInvalidationTracker = database.invalidationTracker,
            searchDao = database.searchDao(),
            localItemDataSource = FakeLocalItemDataSource(),
            encryptionContextProvider = FakeEncryptionContextProvider(),
            baseQuery = "SELECT user_id AS userId, share_id AS shareId, item_id AS itemId FROM search_items",
            queryArgs = emptyArray(),
            useFts = false,
            pageSize = PAGE_SIZE,
            initialLoadSize = PAGE_SIZE,
            appDispatchers = appDispatchers
        )
    }

    private fun SearchDatabase.rowCount(): Int = scalarInt("SELECT COUNT(*) FROM search_items")

    private fun SearchDatabase.ftsMatchCount(term: String): Int =
        scalarInt("SELECT COUNT(*) FROM search_items_fts WHERE search_items_fts MATCH '$term'")

    private fun SearchDatabase.ftsObjectCount(): Int = scalarInt(
        "SELECT COUNT(*) FROM sqlite_master WHERE " +
            "(type = 'table' AND name = 'search_items_fts') OR " +
            "(type = 'trigger' AND name IN ('search_items_fts_ai', 'search_items_fts_ad', 'search_items_fts_au'))"
    )

    private fun SearchDatabase.scalarInt(sql: String): Int = openHelper.writableDatabase.query(sql).use { cursor ->
        cursor.moveToFirst()
        cursor.getInt(0)
    }

    private fun File.header(): String = inputStream().use { stream ->
        val buffer = ByteArray(SQLITE_PLAIN_HEADER.length)
        val read = stream.read(buffer)
        String(buffer, 0, maxOf(read, 0), Charsets.ISO_8859_1)
    }

    private fun entity(itemId: String, title: String) = SearchItemEntity(
        userId = USER_ID,
        shareId = SHARE_ID,
        itemId = itemId,
        title = title,
        subtitle = null,
        itemType = 0,
        createTime = 0L,
        modifyTime = 0L
    )

    private class GatedKeyProvider(private val key: SearchDatabaseKey) : SearchDatabaseKeyProvider {
        val entered = CountDownLatch(1)
        val gate = CountDownLatch(1)
        var calls = 0

        override suspend fun getOrCreateKey(): SearchDatabaseKey {
            calls++
            entered.countDown()
            gate.await()
            return key
        }
    }

    private class FixedKeyProvider(private val key: SearchDatabaseKey) : SearchDatabaseKeyProvider {
        override suspend fun getOrCreateKey(): SearchDatabaseKey = key
    }

    private class PausableDispatcher : CoroutineDispatcher() {
        val queued = CountDownLatch(1)

        @Volatile private var paused = true
        private val pending = ConcurrentLinkedQueue<Runnable>()

        override fun dispatch(context: CoroutineContext, block: Runnable) {
            if (paused) {
                pending += block
                queued.countDown()
            } else {
                Dispatchers.IO.dispatch(context, block)
            }
        }

        fun resume() {
            paused = false
            generateSequence { pending.poll() }.forEach { Dispatchers.IO.dispatch(EmptyCoroutineContext, it) }
        }
    }

    private class PausableAppDispatchers(override val io: CoroutineDispatcher) : AppDispatchers {
        override val main: CoroutineDispatcher = Dispatchers.Main
        override val default: CoroutineDispatcher = Dispatchers.Default
    }

    private object RealAppDispatchers : AppDispatchers {
        override val main: CoroutineDispatcher = Dispatchers.Main
        override val default: CoroutineDispatcher = Dispatchers.Default
        override val io: CoroutineDispatcher = Dispatchers.IO
    }

    private companion object {
        private const val USER_ID = "user-1"
        private const val SHARE_ID = "share-1"
        private const val SQLITE_PLAIN_HEADER = "SQLite format 3"
        private const val FTS_OBJECT_COUNT = 4
        private const val KEY_SIZE = 32
        private const val PAGE_SIZE = 10
        private const val WAIT_SECONDS = 5L
        private val KEY_A = ByteArray(KEY_SIZE) { 1 }
        private val KEY_B = ByteArray(KEY_SIZE) { 2 }
    }
}
