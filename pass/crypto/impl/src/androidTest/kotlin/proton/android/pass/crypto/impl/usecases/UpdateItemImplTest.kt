package proton.android.pass.crypto.impl.usecases

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
import org.junit.Test
import proton.android.pass.crypto.fakes.utils.TestUtils
import proton.android.pass.crypto.impl.extensions.serializeToProto
import proton.android.pass.test.crypto.TestKeyStoreCrypto
import proton.pass.domain.ItemContents
import proton.pass.domain.KeyPacket
import proton.pass.domain.key.VaultKey
import proton.pass.domain.key.publicKey
import proton.pass.domain.key.usePrivateKey
import proton_pass_item_v1.ItemV1
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UpdateItemImplTest {
    private val cryptoContext: CryptoContext = AndroidCryptoContext(
        keyStoreCrypto = TestKeyStoreCrypto,
        pgpCrypto = GOpenPGPCrypto(),
    )

    @Test
    fun canUpdateItem() {
        val userAddress = TestUtils.createUserAddress(cryptoContext)
        val instance = UpdateItemImpl(cryptoContext)
        val (vaultKey, itemKey) = TestUtils.createVaultKeyItemKey(cryptoContext)
        val lastRevision = Random.nextLong()

        val (sessionKey, keyPacket) = generateKeyPacketForVaultKey(vaultKey)
        val contents = ItemContents.Note(
            title = proton.android.pass.test.TestUtils.randomString(),
            note = proton.android.pass.test.TestUtils.randomString()
        )
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
        val keyPacket = cryptoContext.pgpCrypto.encryptSessionKey(
            sessionKey,
            vaultKey.publicKey(cryptoContext).key
        )
        return Pair(
            sessionKey,
            KeyPacket(
                rotationId = vaultKey.rotationId,
                keyPacket = keyPacket
            )
        )
    }
}

