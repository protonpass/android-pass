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
import android.content.ContextWrapper
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import proton.android.pass.crypto.fakes.context.FakeEncryptionContextProvider
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.io.path.createTempDirectory

/**
 * Test suite for [SearchDatabaseKeyProvider] covering:
 * 1. First-time key generation and persistence
 * 2. Key retrieval from existing valid encrypted file
 * 3. Key regeneration and database cleanup on decryption failure (regression test for bug fix)
 */
class SearchDatabaseKeyProviderTest {

    private lateinit var tempDir: File
    private lateinit var testContext: TestContextWrapper
    private lateinit var encryptionContextProvider: FakeEncryptionContextProvider
    private lateinit var provider: SearchDatabaseKeyProviderImpl

    @Before
    fun setup() {
        tempDir = createTempDirectory("search-db-key-test").toFile()
        testContext = TestContextWrapper(tempDir)
        encryptionContextProvider = FakeEncryptionContextProvider()
        provider = SearchDatabaseKeyProviderImpl(testContext, encryptionContextProvider)
    }

    @After
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `first call with no key file creates and persists a new key`() = runTest {
        val keyFile = File(tempDir, "search_db_key.enc")
        assertThat(keyFile.exists()).isFalse()

        val key = provider.getOrCreateKey()

        assertThat(key).isNotEmpty()
        assertThat(keyFile.exists()).isTrue()
        assertThat(keyFile.length()).isGreaterThan(0L)
    }

    @Test
    fun `subsequent calls return the same key without modifying files`() = runTest {
        val keyFile = File(tempDir, "search_db_key.enc")

        // First call creates the key
        val firstKey = provider.getOrCreateKey()
        val firstModTime = keyFile.lastModified()

        // Wait a tiny bit to ensure time difference would be visible
        Thread.sleep(10)

        // Second call should return the same key
        val secondKey = provider.getOrCreateKey()

        assertThat(firstKey).isEqualTo(secondKey)
        // Verify the file wasn't modified (same timestamp)
        assertThat(keyFile.lastModified()).isEqualTo(firstModTime)
    }

    @Test
    fun `corrupted key file triggers regeneration and deletes stale database`() = runTest {
        val keyFile = File(tempDir, "search_db_key.enc")
        val dbFile = testContext.getDatabasePath(SearchDatabase.DB_NAME)
        val walFile = File(dbFile.path + "-wal")
        val shmFile = File(dbFile.path + "-shm")
        val journalFile = File(dbFile.path + "-journal")

        // Create a valid key first
        val originalKey = provider.getOrCreateKey()
        assertThat(keyFile.exists()).isTrue()

        // Simulate a Keystore-backed decryption failure (e.g. key invalidation)
        encryptionContextProvider.shouldFailDecryption = true

        // Create stale database files that should be deleted
        dbFile.parentFile?.mkdirs()
        dbFile.writeText("stale db content")
        walFile.writeText("stale wal content")
        shmFile.writeText("stale shm content")
        journalFile.writeText("stale journal content")

        assertThat(dbFile.exists()).isTrue()
        assertThat(walFile.exists()).isTrue()
        assertThat(shmFile.exists()).isTrue()
        assertThat(journalFile.exists()).isTrue()

        // Call getOrCreateKey - should detect corrupt key, regenerate, and clean up DB files
        val newKey = provider.getOrCreateKey()

        // New key should be different from the original
        assertThat(newKey).isNotEqualTo(originalKey)
        // The key file should now contain valid encrypted data
        assertThat(keyFile.exists()).isTrue()
        assertThat(keyFile.length()).isGreaterThan(0L)
        // Stale database files should be deleted
        assertThat(dbFile.exists()).isFalse()
        assertThat(walFile.exists()).isFalse()
        assertThat(shmFile.exists()).isFalse()
        assertThat(journalFile.exists()).isFalse()
    }

    @Test
    fun `missing database files do not prevent key generation on corruption`() = runTest {
        val keyFile = File(tempDir, "search_db_key.enc")
        val dbFile = testContext.getDatabasePath(SearchDatabase.DB_NAME)

        // Create a valid key first
        val originalKey = provider.getOrCreateKey()

        // Simulate a Keystore-backed decryption failure (e.g. key invalidation)
        encryptionContextProvider.shouldFailDecryption = true

        // Don't create database files, ensuring deleteStaleDatabase handles missing files gracefully
        assertThat(dbFile.exists()).isFalse()

        // Should still succeed
        val newKey = provider.getOrCreateKey()

        assertThat(newKey).isNotEqualTo(originalKey)
        assertThat(keyFile.exists()).isTrue()
    }

    /**
     * Custom Context wrapper that provides the file system operations needed by
     * SearchDatabaseKeyProvider without requiring the full Android framework.
     */
    private class TestContextWrapper(private val dataDirectory: File) : ContextWrapper(null) {
        override fun getApplicationContext(): Context = this

        // Override getDataDir() which is the backing method for dataDir property
        override fun getDataDir(): File = dataDirectory

        override fun getDatabasePath(name: String): File = File(getDataDir(), name)
    }
}
