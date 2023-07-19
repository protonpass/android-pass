/*
 * Copyright (c) 2023 Proton AG
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
import me.proton.core.crypto.common.pgp.VerificationStatus
import me.proton.core.key.domain.entity.key.PrivateKeyRing
import me.proton.core.key.domain.entity.key.PublicKey
import me.proton.core.key.domain.publicKey
import me.proton.core.user.domain.entity.AddressId
import org.junit.Test
import proton.android.pass.account.fakes.TestKeyStoreCrypto
import proton.android.pass.crypto.api.Base64
import proton.android.pass.crypto.fakes.context.TestEncryptionContext
import proton.android.pass.crypto.fakes.context.TestEncryptionContextProvider
import proton.android.pass.crypto.fakes.utils.TestUtils
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class EncryptInviteKeysImplTest {

    private val cryptoContext: CryptoContext = AndroidCryptoContext(
        keyStoreCrypto = TestKeyStoreCrypto,
        pgpCrypto = GOpenPGPCrypto(),
    )

    @Test
    fun createShareVaultRequest() {
        val instance = EncryptInviteKeysImpl(cryptoContext, TestEncryptionContextProvider())
        val inviterAddressKey = TestUtils.createUserAddressKey(cryptoContext, AddressId("inviter"))
        val (shareKey, _) = TestUtils.createShareKey()
        val targetAddressKey = TestUtils.createUserAddressKey(cryptoContext, AddressId("invited"))
        val targetAddressPublicKey = cryptoContext.pgpCrypto.getPublicKey(targetAddressKey.privateKey.key)
        val shareKeyList = listOf(shareKey)
        val res = instance.invoke(
            inviterAddressKey = inviterAddressKey.privateKey,
            shareKeys = shareKeyList,
            targetAddressKey = PublicKey(
                key = targetAddressPublicKey,
                isPrimary = true,
                isActive = true,
                canEncrypt = true,
                canVerify = true,
            )
        )
        // Verify same number of share keys is returned
        assertEquals(shareKeyList.size, res.keys.size)

        // Verify keys match
        res.keys.forEachIndexed { index, shareKeyElement ->
            // Verify key rotation
            val originalShareKey = shareKeyList[index]
            assertEquals(originalShareKey.rotation, shareKeyElement.keyRotation)

            // Decode key and decrypt it verifying the signature
            val decodedKey = Base64.decodeBase64(shareKeyElement.key)
            val armoredDecodedKey = cryptoContext.pgpCrypto.getArmored(decodedKey)
            val inviterPublicKey = inviterAddressKey.privateKey.publicKey(cryptoContext)

            val targetAddressKeyKeyRing = PrivateKeyRing(cryptoContext, listOf(targetAddressKey.privateKey))
            val decryptedData = cryptoContext.pgpCrypto.decryptAndVerifyData(
                message = armoredDecodedKey,
                publicKeys = listOf(inviterPublicKey.key),
                unlockedKeys = targetAddressKeyKeyRing.unlockedKeys.map { it.unlockedKey.value },
            )
            assertEquals(decryptedData.status, VerificationStatus.Success)

            val decryptedOriginalShareKey = TestEncryptionContext.decrypt(originalShareKey.key)
            assertContentEquals(decryptedOriginalShareKey, decryptedData.data)
        }
    }
}
