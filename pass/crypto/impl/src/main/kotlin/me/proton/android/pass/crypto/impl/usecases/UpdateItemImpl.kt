package me.proton.android.pass.crypto.impl.usecases

import com.proton.gopenpgp.armor.Armor
import me.proton.android.pass.crypto.api.usecases.EncryptedUpdateItemRequest
import me.proton.android.pass.crypto.api.usecases.UpdateItem
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.key.domain.decryptSessionKey
import me.proton.core.key.domain.encryptData
import me.proton.core.key.domain.signData
import me.proton.core.key.domain.useKeys
import me.proton.core.user.domain.entity.UserAddress
import me.proton.pass.domain.KeyPacket
import me.proton.pass.domain.key.ItemKey
import me.proton.pass.domain.key.VaultKey
import me.proton.pass.domain.key.usePrivateKey
import proton_pass_item_v1.ItemV1
import javax.inject.Inject

class UpdateItemImpl @Inject constructor(
    private val cryptoContext: CryptoContext
) : UpdateItem, BaseCryptoOperation(cryptoContext) {

    override fun createRequest(
        vaultKey: VaultKey,
        itemKey: ItemKey,
        keyPacket: KeyPacket,
        userAddress: UserAddress,
        itemContent: ItemV1.Item,
        lastRevision: Long
    ): EncryptedUpdateItemRequest {
        val serializedItem = itemContent.toByteArray()
        val sessionKey = vaultKey.usePrivateKey(cryptoContext) {
            decryptSessionKey(keyPacket.keyPacket)
        }
        val encryptedContents = cryptoContext.pgpCrypto.encryptData(serializedItem, sessionKey)

        val userSignature = userAddress.useKeys(cryptoContext) {
            val signature = signData(serializedItem)
            encryptData(Armor.unarmor(signature), sessionKey)
        }

        val itemKeySignature = itemKey.usePrivateKey(cryptoContext) {
            val signature = signData(serializedItem)
            encryptData(Armor.unarmor(signature), sessionKey)
        }

        return EncryptedUpdateItemRequest(
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
