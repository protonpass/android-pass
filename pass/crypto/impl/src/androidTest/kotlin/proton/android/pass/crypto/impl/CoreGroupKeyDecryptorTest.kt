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

package proton.android.pass.crypto.impl

import com.google.common.truth.Truth.assertThat
import me.proton.core.crypto.android.context.AndroidCryptoContext
import me.proton.core.crypto.android.pgp.GOpenPGPCrypto
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.key.domain.entity.key.PublicKey
import me.proton.core.key.domain.entity.key.PublicKeyRing
import org.junit.Test
import proton.android.pass.account.fakes.FakeKeyStoreCrypto
import proton.android.pass.crypto.api.error.InvalidSignature
import proton.android.pass.test.StringTestFactory
import proton.android.pass.test.UserAddressTestFactory

class CoreGroupKeyDecryptorTest {

    private val cryptoContext: CryptoContext = AndroidCryptoContext(
        keyStoreCrypto = FakeKeyStoreCrypto,
        pgpCrypto = GOpenPGPCrypto()
    )

    @Test
    fun decryptsTheShareKeyWhenTheGroupSignatureIsValid() {
        val invited = GeneratedKey()
        val group = GeneratedKey()
        val message = encryptAndSign(SHARE_KEY, recipient = invited, signer = group)

        val result = decryptor(invited, group).decryptAndVerify(message)

        assertThat(result).isEqualTo(SHARE_KEY)
    }

    @Test
    fun throwsInvalidSignatureWhenTheKeyWasSignedByAnotherKey() {
        val invited = GeneratedKey()
        val group = GeneratedKey()
        val otherSigner = GeneratedKey()
        val message = encryptAndSign(SHARE_KEY, recipient = invited, signer = otherSigner)

        val result = runCatching { decryptor(invited, group).decryptAndVerify(message) }

        assertThat(result.exceptionOrNull()).isInstanceOf(InvalidSignature::class.java)
    }

    private fun decryptor(invited: GeneratedKey, group: GeneratedKey) = CoreGroupKeyDecryptor(
        invitedAddress = UserAddressTestFactory.createUserAddress(
            cryptoContext,
            invited.privateKey,
            invited.passphrase
        ),
        groupPublicKeys = PublicKeyRing(listOf(group.publicKey)),
        cryptoContext = cryptoContext
    )

    private fun encryptAndSign(
        data: ByteArray,
        recipient: GeneratedKey,
        signer: GeneratedKey
    ): ByteArray {
        val pgp = cryptoContext.pgpCrypto
        val encrypted = pgp.unlock(signer.privateKey, signer.passphrase).use { unlocked ->
            pgp.encryptAndSignData(data, recipient.armoredPublicKey, unlocked.value)
        }
        return pgp.getUnarmored(encrypted)
    }

    private inner class GeneratedKey {
        val passphrase: ByteArray = StringTestFactory.generatePassphrase().encodeToByteArray()
        val privateKey: String = cryptoContext.pgpCrypto.generateNewPrivateKey(
            "androidTest",
            "androidTest@androidTest",
            passphrase
        )
        val armoredPublicKey: String = cryptoContext.pgpCrypto.getPublicKey(privateKey)
        val publicKey = PublicKey(
            key = armoredPublicKey,
            isPrimary = true,
            isActive = true,
            canEncrypt = true,
            canVerify = true
        )
    }

    private companion object {
        val SHARE_KEY = byteArrayOf(1, 2, 3, 4)
    }
}
