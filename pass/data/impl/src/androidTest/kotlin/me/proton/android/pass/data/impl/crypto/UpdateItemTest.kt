package me.proton.android.pass.data.impl.crypto

import me.proton.android.pass.data.impl.extensions.serializeToProto
import me.proton.core.crypto.android.context.AndroidCryptoContext
import me.proton.core.crypto.android.pgp.GOpenPGPCrypto
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.pgp.PGPHeader
import me.proton.core.crypto.common.pgp.SessionKey
import me.proton.core.key.domain.decryptData
import me.proton.core.key.domain.getArmored
import me.proton.core.key.domain.getBase64Decoded
import me.proton.core.key.domain.useKeys
import me.proton.core.key.domain.verifyData
import me.proton.core.test.android.instrumented.utils.StringUtils
import me.proton.pass.domain.ItemContents
import me.proton.pass.domain.KeyPacket
import me.proton.pass.domain.key.VaultKey
import me.proton.pass.domain.key.publicKey
import me.proton.pass.domain.key.usePrivateKey
import me.proton.pass.test.crypto.TestKeyStoreCrypto
import org.junit.Test
import proton_pass_item_v1.ItemV1
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UpdateItemTest {

    private val cryptoContext: CryptoContext = AndroidCryptoContext(
        keyStoreCrypto = TestKeyStoreCrypto,
        pgpCrypto = GOpenPGPCrypto(),
    )

    @Test
    fun canUpdateItem() {
        val userAddress = TestUtils.createUserAddress(cryptoContext)
        val instance = UpdateItem(cryptoContext)
        val (vaultKey, itemKey) = TestUtils.createVaultKeyItemKey(cryptoContext)
        val lastRevision = Random.nextLong()

        val (sessionKey, keyPacket) = generateKeyPacketForVaultKey(vaultKey)
        val contents = ItemContents.Note(title = StringUtils.randomString(), note = StringUtils.randomString())
        val body = instance.createRequest(
            vaultKey,
            itemKey,
            keyPacket,
            userAddress,
            contents.serializeToProto(),
            lastRevision
        )

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
}
