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

import android.content.ContextWrapper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test
import proton.android.pass.common.fakes.FakeAppDispatchers

class LazyKeySupportSQLiteOpenHelperFactoryTest {

    private val keyProvider = FakeSearchDatabaseKeyProvider()
    private val delegateFactory = FakeDelegateFactory()
    private val factory = LazyKeySupportSQLiteOpenHelperFactory(keyProvider, FakeAppDispatchers()) { key ->
        delegateFactory.keys += key
        delegateFactory
    }

    @Test
    fun `creating the helper does not resolve the key`() {
        factory.create(configuration(name = DB_NAME))

        assertThat(keyProvider.calls).isEqualTo(0)
        assertThat(delegateFactory.keys).isEmpty()
    }

    @Test
    fun `first database access resolves the key and opens the delegate with it`() {
        val passphrase = byteArrayOf(1, 2, 3)
        keyProvider.result = SearchDatabaseKey.Persistent(passphrase)
        val helper = factory.create(configuration(name = DB_NAME))

        assertThrows(FakeDatabaseOpened::class.java) { helper.writableDatabase }

        assertThat(keyProvider.calls).isEqualTo(1)
        val key = delegateFactory.keys.single() as SearchDatabaseKey.Persistent
        assertThat(key.passphrase).isEqualTo(passphrase)
        assertThat(delegateFactory.configurations.single().name).isEqualTo(DB_NAME)
    }

    @Test
    fun `key is resolved once across repeated accesses`() {
        val helper = factory.create(configuration(name = DB_NAME))

        assertThrows(FakeDatabaseOpened::class.java) { helper.writableDatabase }
        assertThrows(FakeDatabaseOpened::class.java) { helper.readableDatabase }

        assertThat(keyProvider.calls).isEqualTo(1)
        assertThat(delegateFactory.helpers).hasSize(1)
    }

    @Test
    fun `unavailable key opens an in-memory delegate`() {
        keyProvider.result = SearchDatabaseKey.Unavailable
        val helper = factory.create(configuration(name = DB_NAME))

        assertThrows(FakeDatabaseOpened::class.java) { helper.writableDatabase }

        assertThat(delegateFactory.keys.single()).isEqualTo(SearchDatabaseKey.Unavailable)
        assertThat(delegateFactory.configurations.single().name).isNull()
    }

    @Test
    fun `key provider failure falls back to an in-memory delegate`() {
        keyProvider.failure = IllegalStateException("keystore down")
        val helper = factory.create(configuration(name = DB_NAME))

        assertThrows(FakeDatabaseOpened::class.java) { helper.readableDatabase }

        assertThat(delegateFactory.keys.single()).isEqualTo(SearchDatabaseKey.Unavailable)
        assertThat(delegateFactory.configurations.single().name).isNull()
    }

    @Test
    fun `write ahead logging requested before opening is applied to the delegate`() {
        val helper = factory.create(configuration(name = DB_NAME))

        helper.setWriteAheadLoggingEnabled(true)
        assertThrows(FakeDatabaseOpened::class.java) { helper.writableDatabase }

        assertThat(delegateFactory.helpers.single().writeAheadLoggingEnabled).isTrue()
    }

    @Test
    fun `database name is exposed without resolving the key`() {
        val helper = factory.create(configuration(name = DB_NAME))

        assertThat(helper.databaseName).isEqualTo(DB_NAME)
        assertThat(keyProvider.calls).isEqualTo(0)
    }

    @Test
    fun `closing before the first access does not resolve the key`() {
        val helper = factory.create(configuration(name = DB_NAME))

        helper.close()

        assertThat(keyProvider.calls).isEqualTo(0)
    }

    private fun configuration(name: String?) = SupportSQLiteOpenHelper.Configuration
        .builder(ContextWrapper(null))
        .name(name)
        .callback(NoOpCallback)
        .build()

    private class FakeSearchDatabaseKeyProvider : SearchDatabaseKeyProvider {
        var calls = 0
        var result: SearchDatabaseKey = SearchDatabaseKey.Persistent(ByteArray(KEY_SIZE))
        var failure: Throwable? = null

        override suspend fun getOrCreateKey(): SearchDatabaseKey {
            calls++
            failure?.let { throw it }
            return result
        }
    }

    private class FakeDelegateFactory : SupportSQLiteOpenHelper.Factory {
        val keys = mutableListOf<SearchDatabaseKey>()
        val configurations = mutableListOf<SupportSQLiteOpenHelper.Configuration>()
        val helpers = mutableListOf<FakeDelegateHelper>()

        override fun create(configuration: SupportSQLiteOpenHelper.Configuration): SupportSQLiteOpenHelper {
            configurations += configuration
            return FakeDelegateHelper(configuration.name).also { helpers += it }
        }
    }

    private class FakeDelegateHelper(override val databaseName: String?) : SupportSQLiteOpenHelper {
        var writeAheadLoggingEnabled: Boolean? = null

        override val writableDatabase: SupportSQLiteDatabase
            get() = throw FakeDatabaseOpened()

        override val readableDatabase: SupportSQLiteDatabase
            get() = throw FakeDatabaseOpened()

        override fun setWriteAheadLoggingEnabled(enabled: Boolean) {
            writeAheadLoggingEnabled = enabled
        }

        override fun close() = Unit
    }

    private class FakeDatabaseOpened : RuntimeException("delegate database opened")

    private object NoOpCallback : SupportSQLiteOpenHelper.Callback(SearchDatabase.VERSION) {
        override fun onCreate(db: SupportSQLiteDatabase) = Unit

        override fun onUpgrade(
            db: SupportSQLiteDatabase,
            oldVersion: Int,
            newVersion: Int
        ) = Unit
    }

    private companion object {
        private const val DB_NAME = "search.db"
        private const val KEY_SIZE = 32
    }
}
