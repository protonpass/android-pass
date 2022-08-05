package me.proton.core.pass.data.crypto

import javax.inject.Inject
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.key.domain.decryptSessionKey
import me.proton.core.key.domain.encryptData
import me.proton.core.key.domain.signData
import me.proton.core.key.domain.useKeys
import me.proton.core.pass.data.extensions.serializeToProto
import me.proton.core.pass.domain.ItemContents
import me.proton.core.pass.domain.KeyPacket
import me.proton.core.pass.domain.key.ItemKey
import me.proton.core.pass.domain.key.VaultKey
import me.proton.core.pass.domain.key.usePrivateKey
import me.proton.core.user.domain.entity.UserAddress

@Serializable
data class UpdateItemRequest(
    @SerialName("RotationID")
    val rotationId: String,
    @SerialName("LastRevision")
    val lastRevision: Long,
    @SerialName("ContentFormatVersion")
    val contentFormatVersion: Int,
    @SerialName("Content")
    val content: String,
    @SerialName("UserSignature")
    val userSignature: String,
    @SerialName("ItemKeySignature")
    val itemKeySignature: String,
)

class UpdateItem @Inject constructor(
    private val cryptoContext: CryptoContext,
) : BaseCryptoOperation(cryptoContext) {

    companion object {
        const val CONTENT_FORMAT_VERSION = 1
    }

    fun updateItem(
        vaultKey: VaultKey,
        itemKey: ItemKey,
        keyPacket: KeyPacket,
        userAddress: UserAddress,
        itemContents: ItemContents,
        lastRevision: Long
    ): UpdateItemRequest {

        val serializedItem = itemContents.serializeToProto()
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
}
