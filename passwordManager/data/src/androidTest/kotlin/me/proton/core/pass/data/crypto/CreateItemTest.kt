package me.proton.core.pass.data.crypto

import me.proton.core.crypto.android.context.AndroidCryptoContext
import me.proton.core.crypto.android.pgp.GOpenPGPCrypto
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.pgp.PGPHeader
import me.proton.core.crypto.common.pgp.SessionKey
import me.proton.core.key.domain.decryptData
import me.proton.core.key.domain.decryptSessionKey
import me.proton.core.key.domain.getArmored
import me.proton.core.key.domain.getBase64Decoded
import me.proton.core.key.domain.useKeys
import me.proton.core.key.domain.verifyData
import me.proton.core.pass.data.requests.CreateItemRequest
import me.proton.core.pass.domain.ItemContents
import me.proton.core.pass.domain.key.ItemKey
import me.proton.core.pass.domain.key.VaultKey
import me.proton.core.pass.domain.key.usePrivateKey
import me.proton.core.pass.test.TestUtils.randomString
import me.proton.core.pass.test.crypto.TestKeyStoreCrypto
import me.proton.core.user.domain.entity.UserAddress
import org.junit.Test
import proton_pass_item_v1.ItemV1
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CreateItemTest {

    private val cryptoContext: CryptoContext = AndroidCryptoContext(
        keyStoreCrypto = TestKeyStoreCrypto,
        pgpCrypto = GOpenPGPCrypto(),
    )

    @Test
    fun testCanCreateItem() {
        val vaultKey = TestUtils.createVaultKey(cryptoContext)
        val itemKey = TestUtils.createItemKeyForVaultKey(cryptoContext, vaultKey)
        val userAddress = TestUtils.createUserAddress(cryptoContext)
        val contents = ItemContents.Note(
            title = randomString(),
            note = randomString()
        )

        val instance = CreateItem(cryptoContext)
        val request = instance.createItem(vaultKey, itemKey, userAddress, contents)

        assertEquals(request.contentFormatVersion, CreateItem.CONTENT_FORMAT_VERSION)
        assertTrue(request.labels.isEmpty())
        assertEquals(request.rotationId, vaultKey.rotationId)

        val (sessionKey, decryptedContents) = decryptContents(vaultKey, request)
        val parsed = ItemV1.Item.parseFrom(decryptedContents)
        assertEquals(parsed.metadata.name, contents.title)
        assertEquals(parsed.metadata.note, contents.note)

        verifyUserSignature(userAddress, sessionKey, decryptedContents, request)
        verifyItemKeySignature(itemKey, sessionKey, decryptedContents, request)
    }

    private fun decryptContents(vaultKey: VaultKey, request: CreateItemRequest): Pair<SessionKey, ByteArray> =
        vaultKey.usePrivateKey(cryptoContext) {
            val decodedKeyPacket = getBase64Decoded(request.vaultKeyPacket)
            val decryptedSessionKey = decryptSessionKey(decodedKeyPacket)

            val decodedContent = getBase64Decoded(request.content)
            val decryptedData = decryptedSessionKey.decryptData(context, decodedContent)
            Pair(decryptedSessionKey, decryptedData)
        }


    private fun verifyUserSignature(
        userAddress: UserAddress,
        sessionKey: SessionKey,
        decryptedContents: ByteArray,
        request: CreateItemRequest
    ) {
        userAddress.useKeys(cryptoContext) {
            val decodedSignature = getBase64Decoded(request.userSignature)
            val decryptedSignature = sessionKey.decryptData(context, decodedSignature)
            val signature = getArmored(decryptedSignature, PGPHeader.Signature)
            assertTrue(verifyData(decryptedContents, signature))
        }
    }

    private fun verifyItemKeySignature(
        itemKey: ItemKey,
        sessionKey: SessionKey,
        decryptedContents: ByteArray,
        request: CreateItemRequest
    ) {
        itemKey.usePrivateKey(cryptoContext) {
            val decodedItemKeySignature = getBase64Decoded(request.itemKeySignature)
            val decryptedItemKeySignature = sessionKey.decryptData(context, decodedItemKeySignature)
            val itemKeySignature = getArmored(decryptedItemKeySignature, PGPHeader.Signature)
            assertTrue(verifyData(decryptedContents, itemKeySignature))

            val decodedVaultKeyPacket = getBase64Decoded(request.vaultKeyPacket)
            val decodedVaultKeyPacketSignature = getBase64Decoded(request.vaultKeyPacketSignature)
            val vaultKeyPacketSignature = getArmored(decodedVaultKeyPacketSignature, PGPHeader.Signature)
            assertTrue(verifyData(decodedVaultKeyPacket, vaultKeyPacketSignature))
        }
    }
}
