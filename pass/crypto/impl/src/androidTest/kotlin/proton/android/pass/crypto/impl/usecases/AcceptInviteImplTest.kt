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
import me.proton.core.key.domain.decryptAndVerifyData
import me.proton.core.key.domain.getArmored
import me.proton.core.key.domain.publicKey
import me.proton.core.key.domain.useKeys
import me.proton.core.user.domain.entity.AddressId
import me.proton.core.user.domain.entity.User
import me.proton.core.user.domain.entity.UserAddressKey
import org.junit.Test
import proton.android.pass.account.fakes.TestKeyStoreCrypto
import proton.android.pass.crypto.api.Base64
import proton.android.pass.crypto.api.usecases.EncryptedInviteKey
import proton.android.pass.crypto.fakes.context.TestEncryptionContext
import proton.android.pass.crypto.fakes.context.TestEncryptionContextProvider
import proton.android.pass.crypto.fakes.utils.TestUtils
import proton.pass.domain.key.ShareKey
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class AcceptInviteImplTest {
    private val cryptoContext: CryptoContext = AndroidCryptoContext(
        keyStoreCrypto = TestKeyStoreCrypto,
        pgpCrypto = GOpenPGPCrypto(),
    )

    @Test
    fun canAcceptInvite() {
        val instance = AcceptInviteImpl(cryptoContext)
        val inviterAddressKey = TestUtils.createUserAddressKey(cryptoContext, AddressId("Inviter"))
        val invited = TestUtils.createUser()
        val invitedUserAddressKey = TestUtils.createUserAddressKey(cryptoContext, AddressId("Invited"))
        val (shareKey, _) = TestUtils.createShareKey()

        val shareKeys = listOf(shareKey)

        val input = generateInput(
            inviterAddressKey = inviterAddressKey,
            invitedUserAddressKey = invitedUserAddressKey,
            shareKeys = shareKeys
        )

        val res = instance.invoke(
            invitedUser = invited,
            invitedUserAddressKeys = listOf(invitedUserAddressKey.privateKey),
            inviterAddressKeys = listOf(inviterAddressKey.privateKey.publicKey(cryptoContext)),
            keys = input
        )
        assertEquals(shareKeys.size, res.keys.size)

        res.keys.forEachIndexed { idx, reencryptedShareKey ->
            validateKey(
                original = shareKeys[idx],
                reencrypted = reencryptedShareKey,
                invitedUser = invited,
            )
        }
    }

    private fun validateKey(
        original: ShareKey,
        reencrypted: EncryptedInviteKey,
        invitedUser: User
    ) {
        val decryptedOriginal = TestEncryptionContext.decrypt(original.key)
        val decodedKey = Base64.decodeBase64(reencrypted.key)
        invitedUser.useKeys(cryptoContext) {
            val res = decryptAndVerifyData(message = getArmored(decodedKey))
            assertEquals(VerificationStatus.Success, res.status)
            assertContentEquals(decryptedOriginal, res.data)
        }
    }

    private fun generateInput(
        inviterAddressKey: UserAddressKey,
        invitedUserAddressKey: UserAddressKey,
        shareKeys: List<ShareKey>
    ) : List<EncryptedInviteKey> {
        val instance = EncryptInviteKeysImpl(cryptoContext, TestEncryptionContextProvider())
        val res = instance.invoke(
            inviterAddressKey = inviterAddressKey.privateKey,
            targetAddressKey = invitedUserAddressKey.privateKey.publicKey(cryptoContext),
            shareKeys = shareKeys
        )
        return res.keys
    }
}
