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

package proton.android.pass.data.impl.crypto

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import proton.android.pass.crypto.api.Base64
import proton.android.pass.crypto.api.error.InvalidSignature
import proton.android.pass.crypto.fakes.context.FakeEncryptionContext
import proton.android.pass.crypto.fakes.usecases.FakeGroupKeyDecryptor
import proton.android.pass.crypto.fakes.usecases.FakeUserKeys
import proton.android.pass.data.impl.exception.UserKeyNotActive

class ReencryptShareKeyImplTest {

    private lateinit var instance: ReencryptShareKeyImpl

    @Before
    fun setup() {
        instance = ReencryptShareKeyImpl()
    }

    @Test
    fun `throws UserKeyNotActive when the user has no active keys`() {
        val userKeys = FakeUserKeys(activeKeyIds = emptySet())

        val result = runCatching { reencrypt(userKeys) }

        assertThat(result.exceptionOrNull()).isInstanceOf(UserKeyNotActive::class.java)
        assertThat(userKeys.decryptedMessages).isEmpty()
    }

    @Test
    fun `throws UserKeyNotActive when no active key matches the key id`() {
        val userKeys = FakeUserKeys(activeKeyIds = setOf("other-key-id"))

        val result = runCatching { reencrypt(userKeys) }

        assertThat(result.exceptionOrNull()).isInstanceOf(UserKeyNotActive::class.java)
        assertThat(userKeys.decryptedMessages).isEmpty()
    }

    @Test
    fun `decrypts the decoded key and reencrypts it when the user key is active`() {
        val userKeys = FakeUserKeys(activeKeyIds = setOf(USER_KEY_ID))

        val result = reencrypt(userKeys)

        assertThat(userKeys.decryptedMessages.single()).isEqualTo(Base64.decodeBase64(ENCODED_KEY))
        assertThat(userKeys.labels).containsExactly("reencrypt share key")
        assertThat(result.array).isEqualTo(FakeEncryptionContext.encrypt(FakeUserKeys.DEFAULT_DECRYPTED).array)
    }

    @Test
    fun `propagates InvalidSignature from the user keys`() {
        val userKeys = FakeUserKeys(activeKeyIds = setOf(USER_KEY_ID))
        userKeys.decryptAndVerifyBehavior = { throw InvalidSignature("bad signature") }

        val result = runCatching { reencrypt(userKeys) }

        assertThat(result.exceptionOrNull()).isInstanceOf(InvalidSignature::class.java)
    }

    @Test
    fun `group key decrypts the decoded key with the decryptor and reencrypts it`() {
        val decryptor = FakeGroupKeyDecryptor()

        val result = instance(
            encryptionContext = FakeEncryptionContext,
            input = ReencryptGroupKeyInput(key = ENCODED_KEY, decryptor = decryptor)
        )

        assertThat(decryptor.decryptedMessages.single()).isEqualTo(Base64.decodeBase64(ENCODED_KEY))
        assertThat(result.array).isEqualTo(FakeEncryptionContext.encrypt(FakeGroupKeyDecryptor.DEFAULT_DECRYPTED).array)
    }

    @Test
    fun `group key propagates InvalidSignature from the decryptor`() {
        val decryptor = FakeGroupKeyDecryptor()
        decryptor.decryptAndVerifyBehavior = { throw InvalidSignature("bad signature") }

        val result = runCatching {
            instance(
                encryptionContext = FakeEncryptionContext,
                input = ReencryptGroupKeyInput(key = ENCODED_KEY, decryptor = decryptor)
            )
        }

        assertThat(result.exceptionOrNull()).isInstanceOf(InvalidSignature::class.java)
    }

    private fun reencrypt(userKeys: FakeUserKeys) = instance(
        encryptionContext = FakeEncryptionContext,
        input = ReencryptShareKeyInput(
            key = ENCODED_KEY,
            userKeyId = USER_KEY_ID,
            addressId = "address-id",
            userKeys = userKeys
        )
    )

    private companion object {
        const val USER_KEY_ID = "user-key-id"
        val ENCODED_KEY: String = Base64.encodeBase64String(byteArrayOf(1, 2, 3))
    }
}
