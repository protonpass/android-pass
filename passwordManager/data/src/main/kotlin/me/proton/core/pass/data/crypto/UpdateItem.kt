package me.proton.core.pass.data.crypto

import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.key.domain.decryptSessionKey
import me.proton.core.key.domain.encryptData
import me.proton.core.key.domain.signData
import me.proton.core.key.domain.useKeys
import me.proton.core.pass.data.requests.UpdateItemRequest
import me.proton.core.pass.domain.KeyPacket
import me.proton.core.pass.domain.key.ItemKey
import me.proton.core.pass.domain.key.VaultKey
import me.proton.core.pass.domain.key.usePrivateKey
import me.proton.core.user.domain.entity.UserAddress
import proton_pass_item_v1.ItemV1
import javax.inject.Inject

class UpdateItem @Inject constructor(
    private val cryptoContext: CryptoContext
) : BaseCryptoOperation(cryptoContext) {

    @Suppress("LongParameterList")
    fun createRequest(
        vaultKey: VaultKey,
        itemKey: ItemKey,
        keyPacket: KeyPacket,
        userAddress: UserAddress,
        itemContent: ItemV1.Item,
        lastRevision: Long
    ): UpdateItemRequest {
        val serializedItem = itemContent.toByteArray()
        val sessionKey = vaultKey.usePrivateKey(cryptoContext) {
            decryptSessionKey(keyPacket.keyPacket)
        }
        val encryptedContents = cryptoContext.pgpCrypto.encryptData(serializedItem, sessionKey)

        val userSignature = userAddress.useKeys(cryptoContext) {
            val signature = signData(serializedItem)
            encryptData(unarmor(signature), sessionKey)
        }

        val itemKeySignature = itemKey.usePrivateKey(cryptoContext) {
            val signature = signData(serializedItem)
            encryptData(unarmor(signature), sessionKey)
        }

        return UpdateItemRequest(
            rotationId = vaultKey.rotationId,
            contentFormatVersion = CONTENT_FORMAT_VERSION,
            content = b64(encryptedContents),
            userSignature = b64(userSignature),
            itemKeySignature = b64(itemKeySignature),
            lastRevision = lastRevision
        )
    }

    companion object {
        const val CONTENT_FORMAT_VERSION = 1
    }
}
