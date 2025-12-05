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
import proton.android.pass.account.fakes.FakeKeyStoreCrypto
import proton.android.pass.crypto.api.Base64
import proton.android.pass.crypto.api.usecases.invites.EncryptedInviteAcceptKey
import proton.android.pass.crypto.api.usecases.invites.EncryptedInviteKey
import proton.android.pass.crypto.fakes.context.FakeEncryptionContext
import proton.android.pass.crypto.fakes.context.FakeEncryptionContextProvider
import proton.android.pass.domain.key.ShareKey
import proton.android.pass.test.UserAddressKeyTestFactory
import proton.android.pass.test.domain.ShareKeyTestFactory
import proton.android.pass.test.domain.UserTestFactory
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class AcceptInviteImplTest {
    private val cryptoContext: CryptoContext = AndroidCryptoContext(
        keyStoreCrypto = FakeKeyStoreCrypto,
        pgpCrypto = GOpenPGPCrypto(),
    )

    @Test
    fun canAcceptInvite() {
        val instance = AcceptUserInviteImpl(cryptoContext, FakeEncryptionContextProvider())
        val inviterAddressKey = UserAddressKeyTestFactory.createUserAddressKey(cryptoContext, AddressId("Inviter"))
        val invited = UserTestFactory.createWithKeys()
        val invitedUserAddressKey = UserAddressKeyTestFactory.createUserAddressKey(cryptoContext, AddressId("Invited"))
        val (shareKey, _) = ShareKeyTestFactory.create()

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
        reencrypted: EncryptedInviteAcceptKey,
        invitedUser: User
    ) {
        val decryptedOriginal = FakeEncryptionContext.decrypt(original.key)
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
        val instance = EncryptInviteKeysImpl(cryptoContext, FakeEncryptionContextProvider())
        val res = instance.invoke(
            inviterAddressKey = inviterAddressKey.privateKey,
            targetAddressKey = invitedUserAddressKey.privateKey.publicKey(cryptoContext),
            inviteKeys = shareKeys
        )
        return res.keys
    }
}
