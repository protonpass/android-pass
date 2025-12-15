/*
 * Copyright (c) 2025 Proton AG
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
import me.proton.core.crypto.common.keystore.PlainByteArray
import me.proton.core.crypto.common.keystore.encrypt
import me.proton.core.crypto.common.pgp.PGPHeader
import me.proton.core.crypto.common.pgp.exception.CryptoException
import me.proton.core.key.domain.entity.key.PrivateAddressKey
import me.proton.core.key.domain.entity.key.PrivateKey
import me.proton.core.key.domain.publicKey
import me.proton.core.user.domain.entity.AddressId
import me.proton.core.user.domain.entity.UserAddressKey
import org.junit.Test
import proton.android.pass.account.fakes.FakeKeyStoreCrypto
import proton.android.pass.crypto.api.Base64
import proton.android.pass.crypto.api.usecases.invites.EncryptedGroupInviteAcceptKey
import proton.android.pass.crypto.api.usecases.invites.EncryptedInviteKey
import proton.android.pass.crypto.fakes.context.FakeEncryptionContext
import proton.android.pass.crypto.fakes.context.FakeEncryptionContextProvider
import proton.android.pass.domain.key.ShareKey
import proton.android.pass.test.UserAddressKeyTestFactory
import proton.android.pass.test.domain.ShareKeyTestFactory
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AcceptGroupInviteImplTest {
    private val cryptoContext: CryptoContext = AndroidCryptoContext(
        keyStoreCrypto = FakeKeyStoreCrypto,
        pgpCrypto = GOpenPGPCrypto(),
    )

    @Test
    fun canAcceptGroupInvite() {
        val inviterAddressKey = UserAddressKeyTestFactory.createUserAddressKey(
            cryptoContext,
            AddressId("Inviter")
        )
        val groupOpenerKey = createOrganizationKey()
        val (groupAddressKey, groupPassphrase) = createGroupAddressKey(groupOpenerKey)
        val (shareKey, _) = ShareKeyTestFactory.create()
        val shareKeys = listOf(shareKey)

        val encryptedInviteKeys = generateInput(
            inviterAddressKey = inviterAddressKey,
            targetGroupKey = groupAddressKey,
            shareKeys = shareKeys
        )

        val instance = AcceptGroupInviteImpl(cryptoContext, FakeEncryptionContextProvider())

        val res = instance.invoke(
            groupPrivateKeys = listOf(groupAddressKey),
            openerKey = groupOpenerKey,
            inviterAddressKeys = listOf(inviterAddressKey.privateKey.publicKey(cryptoContext)),
            keys = encryptedInviteKeys
        )

        assertEquals(shareKeys.size, res.size)
        res.forEachIndexed { idx, reencryptedShareKey ->
            validateKey(
                original = shareKeys[idx],
                reencrypted = reencryptedShareKey,
                groupPrivateKey = groupAddressKey,
                groupPassphrase = groupPassphrase
            )
        }
    }

    @Test
    fun invalidSignatureThrows() {
        val inviterAddressKey = UserAddressKeyTestFactory.createUserAddressKey(
            cryptoContext,
            AddressId("Inviter")
        )
        val wrongInviterKey = UserAddressKeyTestFactory.createUserAddressKey(
            cryptoContext,
            AddressId("WrongInviter")
        )
        val groupOpenerKey = createOrganizationKey()
        val (groupAddressKey, _) = createGroupAddressKey(groupOpenerKey)
        val (shareKey, _) = ShareKeyTestFactory.create()

        val encryptedInviteKeys = generateInput(
            inviterAddressKey = inviterAddressKey,
            targetGroupKey = groupAddressKey,
            shareKeys = listOf(shareKey)
        )

        val instance = AcceptGroupInviteImpl(cryptoContext, FakeEncryptionContextProvider())

        val error = assertFailsWith<IllegalStateException> {
            instance.invoke(
                groupPrivateKeys = listOf(groupAddressKey),
                openerKey = groupOpenerKey,
                inviterAddressKeys = listOf(wrongInviterKey.privateKey.publicKey(cryptoContext)),
                keys = encryptedInviteKeys
            )
        }
        assertEquals("Message cannot be decrypted with any group key", error.message)
    }

    @Test
    fun picksCorrectGroupKeyWhenMultipleProvided() {
        val inviterAddressKey = UserAddressKeyTestFactory.createUserAddressKey(
            cryptoContext,
            AddressId("Inviter")
        )
        val groupOpenerKey = createOrganizationKey()
        val (firstGroupKey, _) = createGroupAddressKey(groupOpenerKey)
        val (targetGroupKey, targetGroupPassphrase) = createGroupAddressKey(groupOpenerKey)
        val (shareKey, _) = ShareKeyTestFactory.create()

        val encryptedInviteKeys = generateInput(
            inviterAddressKey = inviterAddressKey,
            targetGroupKey = targetGroupKey,
            shareKeys = listOf(shareKey)
        )

        val instance = AcceptGroupInviteImpl(cryptoContext, FakeEncryptionContextProvider())

        val res = instance.invoke(
            groupPrivateKeys = listOf(firstGroupKey, targetGroupKey),
            openerKey = groupOpenerKey,
            inviterAddressKeys = listOf(inviterAddressKey.privateKey.publicKey(cryptoContext)),
            keys = encryptedInviteKeys
        )

        assertEquals(1, res.size)
        validateKey(
            original = shareKey,
            reencrypted = res.first(),
            groupPrivateKey = targetGroupKey,
            groupPassphrase = targetGroupPassphrase
        )
    }

    @Test
    fun missingTokenFailsFast() {
        val inviterAddressKey = UserAddressKeyTestFactory.createUserAddressKey(
            cryptoContext,
            AddressId("Inviter")
        )
        val groupOpenerKey = createOrganizationKey()
        val groupKeyWithNoToken = createGroupAddressKey(groupOpenerKey).first.copy(token = null)
        val (shareKey, _) = ShareKeyTestFactory.create()

        val encryptedInviteKeys = generateInput(
            inviterAddressKey = inviterAddressKey,
            targetGroupKey = groupKeyWithNoToken,
            shareKeys = listOf(shareKey)
        )

        val instance = AcceptGroupInviteImpl(cryptoContext, FakeEncryptionContextProvider())

        val error = assertFailsWith<IllegalStateException> {
            instance.invoke(
                groupPrivateKeys = listOf(groupKeyWithNoToken),
                openerKey = groupOpenerKey,
                inviterAddressKeys = listOf(inviterAddressKey.privateKey.publicKey(cryptoContext)),
                keys = encryptedInviteKeys
            )
        }
        assertEquals("Missing group address key token", error.message)
    }

    @Test
    fun wrongOpenerKeyCannotUnlockGroupKey() {
        val inviterAddressKey = UserAddressKeyTestFactory.createUserAddressKey(
            cryptoContext,
            AddressId("Inviter")
        )
        val correctOpener = createOrganizationKey()
        val wrongOpener = createOrganizationKey()
        val (groupAddressKey, _) = createGroupAddressKey(correctOpener)
        val (shareKey, _) = ShareKeyTestFactory.create()

        val encryptedInviteKeys = generateInput(
            inviterAddressKey = inviterAddressKey,
            targetGroupKey = groupAddressKey,
            shareKeys = listOf(shareKey)
        )

        val instance = AcceptGroupInviteImpl(cryptoContext, FakeEncryptionContextProvider())

        assertFailsWith<CryptoException> {
            instance.invoke(
                groupPrivateKeys = listOf(groupAddressKey),
                openerKey = wrongOpener,
                inviterAddressKeys = listOf(inviterAddressKey.privateKey.publicKey(cryptoContext)),
                keys = encryptedInviteKeys
            )
        }
    }

    private fun validateKey(
        original: ShareKey,
        reencrypted: EncryptedGroupInviteAcceptKey,
        groupPrivateKey: PrivateAddressKey,
        groupPassphrase: ByteArray
    ) {
        val decryptedOriginal = FakeEncryptionContext.decrypt(original.key)
        val decodedKey = Base64.decodeBase64(reencrypted.key)
        val armored = cryptoContext.pgpCrypto.getArmored(decodedKey)
        val unlockedGroupKey = cryptoContext.pgpCrypto.unlock(
            privateKey = groupPrivateKey.privateKey.key,
            passphrase = groupPassphrase
        )
        val decrypted = cryptoContext.pgpCrypto.decryptData(
            message = armored,
            unlockedKey = unlockedGroupKey.value
        )
        assertContentEquals(decryptedOriginal, decrypted)

        val localKey = FakeEncryptionContext.decrypt(reencrypted.localEncryptedKey)
        assertContentEquals(decryptedOriginal, localKey)
    }

    private fun generateInput(
        inviterAddressKey: UserAddressKey,
        targetGroupKey: PrivateAddressKey,
        shareKeys: List<ShareKey>
    ): List<EncryptedInviteKey> {
        val instance = EncryptInviteKeysImpl(cryptoContext, FakeEncryptionContextProvider())
        val res = instance.invoke(
            inviterAddressKey = inviterAddressKey.privateKey,
            inviteKeys = shareKeys,
            targetAddressKey = targetGroupKey.privateKey.publicKey(cryptoContext)
        )
        return res.keys
    }

    private fun createOrganizationKey(): PrivateKey {
        val passphrase = "organization-passphrase".encodeToByteArray()
        val lockedKey = cryptoContext.pgpCrypto.generateNewPrivateKey(
            username = "organization",
            domain = "group.test",
            passphrase = passphrase
        )
        val unlocked = cryptoContext.pgpCrypto.unlock(lockedKey, passphrase)
        val unencryptedArmored = cryptoContext.pgpCrypto.getArmored(
            unlocked.value,
            PGPHeader.PrivateKey
        )
        return PrivateKey(
            key = unencryptedArmored,
            isPrimary = true,
            isActive = true,
            canEncrypt = true,
            canVerify = true,
            passphrase = null
        )
    }

    private fun createGroupAddressKey(
        organizationKey: PrivateKey,
        passphrase: ByteArray = "group-key-passphrase".encodeToByteArray()
    ): Pair<PrivateAddressKey, ByteArray> {
        val groupPrivateKey = cryptoContext.pgpCrypto.generateNewPrivateKey(
            username = "group",
            domain = "group.test",
            passphrase = passphrase
        )

        val token = cryptoContext.pgpCrypto.encryptData(
            data = passphrase,
            publicKey = organizationKey.publicKey(cryptoContext).key
        )

        val encryptedPassphrase = PlainByteArray(passphrase).encrypt(cryptoContext.keyStoreCrypto)

        val privateKey = PrivateKey(
            key = groupPrivateKey,
            isPrimary = true,
            isActive = true,
            canEncrypt = true,
            canVerify = true,
            passphrase = encryptedPassphrase
        )

        return PrivateAddressKey(
            addressId = "group-address-id",
            privateKey = privateKey,
            token = token,
            signature = null
        ) to passphrase
    }
}
