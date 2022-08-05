package me.proton.core.pass.data.crypto

import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import me.proton.core.crypto.android.context.AndroidCryptoContext
import me.proton.core.crypto.android.pgp.GOpenPGPCrypto
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.common.keystore.PlainByteArray
import me.proton.core.crypto.common.pgp.PGPHeader
import me.proton.core.crypto.common.pgp.SessionKey
import me.proton.core.key.domain.*
import me.proton.core.pass.domain.ItemContents
import me.proton.core.pass.domain.KeyPacket
import me.proton.core.pass.domain.key.ItemKey
import me.proton.core.pass.domain.key.VaultKey
import me.proton.core.pass.domain.key.publicKey
import me.proton.core.pass.domain.key.usePrivateKey
import me.proton.core.test.android.instrumented.utils.StringUtils
import org.junit.Test
import proton_pass_item_v1.ItemV1

class UpdateItemTest {

    private val cryptoContext: CryptoContext = AndroidCryptoContext(
        keyStoreCrypto = object : KeyStoreCrypto {
            override fun isUsingKeyStore(): Boolean = false
            override fun encrypt(value: String): EncryptedString = value
            override fun decrypt(value: EncryptedString): String = value
            override fun encrypt(value: PlainByteArray): EncryptedByteArray =
                EncryptedByteArray(value.array.copyOf())

            override fun decrypt(value: EncryptedByteArray): PlainByteArray =
                PlainByteArray(value.array.copyOf())
        },
        pgpCrypto = GOpenPGPCrypto(),
    )

    @Test
    fun canUpdateItem() {
        val userAddress = TestUtils.createUserAddress(cryptoContext)
        val instance = UpdateItem(cryptoContext)
        val (vaultKey, itemKey) = generateKeys()
        val lastRevision = Random.nextLong()

        val (sessionKey, keyPacket) = generateKeyPacketForVaultKey(vaultKey)
        val contents = ItemContents.Note(title = StringUtils.randomString(), note = StringUtils.randomString())
        val body = instance.updateItem(vaultKey, itemKey, keyPacket, userAddress, contents, lastRevision)

        assertEquals(lastRevision, body.lastRevision)
        assertEquals(vaultKey.rotationId, body.rotationId)

        val decodedContents = cryptoContext.pgpCrypto.getBase64Decoded(body.content)
        val decryptedContent = cryptoContext.pgpCrypto.decryptData(decodedContents, sessionKey)
        val asItemContents = ItemV1.Item.parseFrom(decryptedContent)
        assertEquals(contents.title, asItemContents.metadata.name)
        assertEquals(contents.note, asItemContents.metadata.note)

        itemKey.usePrivateKey(cryptoContext) {
            val decrypted = decryptData(getBase64Decoded(body.itemKeySignature), sessionKey)
            val asSignature = getArmored(decrypted, PGPHeader.Signature)
            assertTrue(verifyData(decryptedContent, asSignature))
        }

        userAddress.useKeys(cryptoContext) {
            val decrypted = decryptData(getBase64Decoded(body.userSignature), sessionKey)
            val asSignature = getArmored(decrypted, PGPHeader.Signature)
            assertTrue(verifyData(decryptedContent, asSignature))
        }
    }

    private fun generateKeyPacketForVaultKey(vaultKey: VaultKey): Pair<SessionKey, KeyPacket> {
        val sessionKey = cryptoContext.pgpCrypto.generateNewSessionKey()
        val keyPacket = cryptoContext.pgpCrypto.encryptSessionKey(sessionKey, vaultKey.publicKey(cryptoContext).key)
        return Pair(
            sessionKey,
            KeyPacket(
                rotationId = vaultKey.rotationId,
                keyPacket = keyPacket
            )
        )
    }

    private fun generateKeys(): Pair<VaultKey, ItemKey> {
        val vaultKey = TestUtils.createVaultKey(cryptoContext)
        val itemKey = TestUtils.createItemKeyForVaultKey(cryptoContext, vaultKey)
        return Pair(vaultKey, itemKey)
    }
}
