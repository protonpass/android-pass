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

package proton.android.pass.crypto.impl.context

import android.content.Context
import android.content.ContextWrapper
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import proton.android.pass.common.fakes.FakeAppDispatchers
import proton.android.pass.crypto.api.EncryptionKey
import proton.android.pass.crypto.api.error.LocalEncryptionKeyUnavailableException
import proton.android.pass.crypto.fakes.context.FakeAndroidKeyStoreCrypto
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.assertFailsWith

class EncryptionContextProviderImplTest {

    private lateinit var tempDir: File
    private lateinit var keyFile: File
    private lateinit var keyStoreCrypto: FakeAndroidKeyStoreCrypto
    private lateinit var instance: EncryptionContextProviderImpl

    @Before
    fun setup() {
        tempDir = createTempDirectory("encryption-context-provider-test").toFile()
        keyFile = File(tempDir, "pass.key")
        keyStoreCrypto = FakeAndroidKeyStoreCrypto()
        instance = createInstance()
    }

    @After
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `generates and persists an encrypted key when none exists`() {
        val encrypted = instance.withEncryptionContext { encrypt("content") }

        assertThat(keyFile.exists()).isTrue()
        assertThat(keyFile.length()).isNotEqualTo(EncryptionKey.KEY_SIZE.toLong())
        assertThat(createInstance().withEncryptionContext { decrypt(encrypted) }).isEqualTo("content")
    }

    @Test
    fun `generates a plaintext key when keystore is unavailable`() {
        keyStoreCrypto.isKeyStoreAvailable = false

        val encrypted = instance.withEncryptionContext { encrypt("content") }

        assertThat(keyFile.length()).isEqualTo(EncryptionKey.KEY_SIZE.toLong())
        assertThat(createInstance().withEncryptionContext { decrypt(encrypted) }).isEqualTo("content")
    }

    @Test
    fun `rejects encrypted key file when keystore is unavailable and keeps the file`() {
        val encrypted = instance.withEncryptionContext { encrypt("content") }
        val originalKeyFileContent = keyFile.readBytes()
        keyStoreCrypto.isKeyStoreAvailable = false
        val degradedInstance = createInstance()

        assertFailsWith<LocalEncryptionKeyUnavailableException> {
            degradedInstance.withEncryptionContext { decrypt(encrypted) }
        }
        assertThat(keyFile.readBytes()).isEqualTo(originalKeyFileContent)
    }

    @Test
    fun `does not cache a rejected key and recovers when keystore becomes available`() {
        val encrypted = instance.withEncryptionContext { encrypt("content") }
        keyStoreCrypto.isKeyStoreAvailable = false
        val degradedInstance = createInstance()
        assertFailsWith<LocalEncryptionKeyUnavailableException> {
            degradedInstance.withEncryptionContext { decrypt(encrypted) }
        }

        keyStoreCrypto.isKeyStoreAvailable = true

        assertThat(degradedInstance.withEncryptionContext { decrypt(encrypted) }).isEqualTo("content")
    }

    @Test
    fun `accepts a legacy plaintext key file when keystore is unavailable`() {
        val legacyKey = EncryptionKey.generate()
        keyFile.writeBytes(legacyKey.value())
        val encrypted = EncryptionContextImpl(legacyKey.clone()).encrypt("content")
        keyStoreCrypto.isKeyStoreAvailable = false

        assertThat(instance.withEncryptionContext { decrypt(encrypted) }).isEqualTo("content")
    }

    @Test
    fun `rejects decrypted key with unexpected size`() {
        keyFile.writeBytes(ByteArray(EncryptionKey.KEY_SIZE + 1) { 1 })
        keyStoreCrypto.isKeyStoreAvailable = false

        assertFailsWith<LocalEncryptionKeyUnavailableException> {
            instance.withEncryptionContext { encrypt("content") }
        }
    }

    @Test
    fun `blocks without crypto operations still run when key is unavailable`() {
        instance.withEncryptionContext { encrypt("content") }
        keyStoreCrypto.isKeyStoreAvailable = false

        val result = createInstance().withEncryptionContext { emptyList<String>().map { decrypt(it) } }

        assertThat(result).isEmpty()
    }

    @Test
    fun `suspendable context fails on crypto operations when key is unavailable`() = runTest {
        instance.withEncryptionContext { encrypt("content") }
        val originalKeyFileContent = keyFile.readBytes()
        keyStoreCrypto.isKeyStoreAvailable = false

        assertFailsWith<LocalEncryptionKeyUnavailableException> {
            createInstance().withEncryptionContextSuspendable { encrypt("content") }
        }
        assertThat(keyFile.readBytes()).isEqualTo(originalKeyFileContent)
    }

    private fun createInstance() = EncryptionContextProviderImpl(
        context = TestContextWrapper(tempDir),
        keyStoreCrypto = keyStoreCrypto,
        appDispatchers = FakeAppDispatchers()
    )

    private class TestContextWrapper(private val dataDirectory: File) : ContextWrapper(null) {
        override fun getApplicationContext(): Context = this

        override fun getDataDir(): File = dataDirectory
    }
}
