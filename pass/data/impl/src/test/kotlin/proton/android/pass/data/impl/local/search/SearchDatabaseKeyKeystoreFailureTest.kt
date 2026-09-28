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
import org.junit.After
import org.junit.Before
import org.junit.Test
import proton.android.pass.common.fakes.FakeAppDispatchers
import proton.android.pass.crypto.api.EncryptionKey
import proton.android.pass.crypto.fakes.context.FakeAndroidKeyStoreCrypto
import proton.android.pass.crypto.impl.context.EncryptionContextProviderImpl
import java.io.File
import kotlin.io.path.createTempDirectory

class SearchDatabaseKeyKeystoreFailureTest {

    private lateinit var tempDir: File
    private lateinit var testContext: TestContextWrapper
    private lateinit var keyStoreCrypto: FakeAndroidKeyStoreCrypto
    private lateinit var encryptionContextProvider: EncryptionContextProviderImpl
    private lateinit var provider: SearchDatabaseKeyProviderImpl

    @Before
    fun setup() {
        tempDir = createTempDirectory("search-db-keystore-failure-test").toFile()
        testContext = TestContextWrapper(tempDir)
        keyStoreCrypto = FakeAndroidKeyStoreCrypto()
        encryptionContextProvider = EncryptionContextProviderImpl(
            context = testContext,
            keyStoreCrypto = keyStoreCrypto,
            appDispatchers = FakeAppDispatchers()
        )
        provider = SearchDatabaseKeyProviderImpl(testContext, encryptionContextProvider)
    }

    @After
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `healthy launch creates both key files then returns unavailable when keystore fails`() = runTest {
        val passKeyFile = File(tempDir, "pass.key")
        val searchKeyFile = File(tempDir, "search_db_key.enc")

        assertThat(passKeyFile.exists()).isFalse()
        assertThat(searchKeyFile.exists()).isFalse()

        val firstKey = provider.getOrCreateKey()
        assertThat(firstKey).isInstanceOf(SearchDatabaseKey.Persistent::class.java)
        val firstPassphrase = (firstKey as SearchDatabaseKey.Persistent).passphrase
        assertThat(firstPassphrase).hasLength(32)

        assertThat(passKeyFile.exists()).isTrue()
        assertThat(searchKeyFile.exists()).isTrue()
        val originalPassKeyContent = passKeyFile.readBytes()
        val originalSearchKeyContent = searchKeyFile.readBytes()

        keyStoreCrypto.isKeyStoreAvailable = false

        val newProvider = SearchDatabaseKeyProviderImpl(
            testContext,
            EncryptionContextProviderImpl(
                context = testContext,
                keyStoreCrypto = keyStoreCrypto,
                appDispatchers = FakeAppDispatchers()
            )
        )
        val degradedKey = newProvider.getOrCreateKey()

        assertThat(degradedKey).isInstanceOf(SearchDatabaseKey.Unavailable::class.java)
        assertThat(passKeyFile.readBytes()).isEqualTo(originalPassKeyContent)
        assertThat(searchKeyFile.readBytes()).isEqualTo(originalSearchKeyContent)
    }

    @Test
    fun `recovery when keystore becomes available again returns same passphrase`() = runTest {
        val firstKey = provider.getOrCreateKey()
        val originalPassphrase = (firstKey as SearchDatabaseKey.Persistent).passphrase

        keyStoreCrypto.isKeyStoreAvailable = false
        val degradedProvider = SearchDatabaseKeyProviderImpl(
            testContext,
            EncryptionContextProviderImpl(
                context = testContext,
                keyStoreCrypto = keyStoreCrypto,
                appDispatchers = FakeAppDispatchers()
            )
        )
        val degradedKey = degradedProvider.getOrCreateKey()
        assertThat(degradedKey).isInstanceOf(SearchDatabaseKey.Unavailable::class.java)

        keyStoreCrypto.isKeyStoreAvailable = true
        val recoveryProvider = SearchDatabaseKeyProviderImpl(
            testContext,
            EncryptionContextProviderImpl(
                context = testContext,
                keyStoreCrypto = keyStoreCrypto,
                appDispatchers = FakeAppDispatchers()
            )
        )
        val recoveredKey = recoveryProvider.getOrCreateKey()

        assertThat(recoveredKey).isInstanceOf(SearchDatabaseKey.Persistent::class.java)
        assertThat((recoveredKey as SearchDatabaseKey.Persistent).passphrase).isEqualTo(originalPassphrase)
    }

    @Test
    fun `fresh install with keystore down from start keeps the same key across launches`() = runTest {
        keyStoreCrypto.isKeyStoreAvailable = false

        val passKeyFile = File(tempDir, "pass.key")
        val searchKeyFile = File(tempDir, "search_db_key.enc")

        val firstKey = provider.getOrCreateKey()

        assertThat(firstKey).isInstanceOf(SearchDatabaseKey.Persistent::class.java)
        assertThat(passKeyFile.length()).isEqualTo(EncryptionKey.KEY_SIZE.toLong())
        assertThat(searchKeyFile.exists()).isTrue()

        val nextLaunchProvider = SearchDatabaseKeyProviderImpl(
            testContext,
            EncryptionContextProviderImpl(
                context = testContext,
                keyStoreCrypto = keyStoreCrypto,
                appDispatchers = FakeAppDispatchers()
            )
        )
        val nextLaunchKey = nextLaunchProvider.getOrCreateKey()

        assertThat(nextLaunchKey).isInstanceOf(SearchDatabaseKey.Persistent::class.java)
        assertThat((nextLaunchKey as SearchDatabaseKey.Persistent).passphrase)
            .isEqualTo((firstKey as SearchDatabaseKey.Persistent).passphrase)
    }

    private class TestContextWrapper(private val dataDirectory: File) : ContextWrapper(null) {
        override fun getApplicationContext(): Context = this

        override fun getDataDir(): File = dataDirectory

        override fun getDatabasePath(name: String): File = File(getDataDir(), name)
    }
}
