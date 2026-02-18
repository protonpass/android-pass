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

package proton.android.pass.crypto.impl.usecases

import me.proton.core.crypto.android.context.AndroidCryptoContext
import me.proton.core.crypto.android.pgp.GOpenPGPCrypto
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.pgp.SignatureContext
import me.proton.core.key.domain.publicKey
import me.proton.core.key.domain.signData
import org.junit.Test
import proton.android.pass.account.fakes.FakeKeyStoreCrypto
import proton.android.pass.crypto.api.Constants
import proton.android.pass.crypto.impl.extensions.tryUseKeys
import proton.android.pass.domain.OrganizationKey
import proton.android.pass.test.domain.UserTestFactory
import kotlin.test.assertTrue

class OpenOrganizationKeyImplTest {
    private val cryptoContext: CryptoContext = AndroidCryptoContext(
        keyStoreCrypto = FakeKeyStoreCrypto,
        pgpCrypto = GOpenPGPCrypto()
    )

    @Test
    fun returnedOrganizationPrivateKeyCanBeUnlocked() {
        val user = UserTestFactory.createWithKeys()
        val organizationPassphrase = "organization-passphrase".encodeToByteArray()
        val lockedOrganizationKey = cryptoContext.pgpCrypto.generateNewPrivateKey(
            username = "organization",
            domain = "group.test",
            passphrase = organizationPassphrase
        )

        val token = cryptoContext.pgpCrypto.encryptData(
            data = organizationPassphrase,
            publicKey = user.keys.first().privateKey.publicKey(cryptoContext).key
        )
        val signature = user.tryUseKeys("sign organization key token", cryptoContext) {
            signData(
                data = organizationPassphrase,
                signatureContext = SignatureContext(
                    value = Constants.SIGNATURE_CONTEXT_ORG_KEY_TOKEN,
                    isCritical = true
                )
            )
        }

        val instance = OpenOrganizationKeyImpl(cryptoContext)
        val (privateKey, _) = instance.invoke(
            user = user,
            organizationKey = OrganizationKey(
                privateKey = lockedOrganizationKey,
                token = token,
                signature = signature,
                passwordless = true
            )
        ).getOrThrow()

        assertTrue(privateKey.key.contains("BEGIN PGP PRIVATE KEY BLOCK"))
        val unlocked = cryptoContext.pgpCrypto.unlock(
            privateKey = privateKey.key,
            passphrase = ByteArray(0)
        )
        unlocked.close()
    }
}
