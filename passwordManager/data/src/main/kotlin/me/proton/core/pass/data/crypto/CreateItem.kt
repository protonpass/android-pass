package me.proton.core.pass.data.crypto

import javax.inject.Inject
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.pgp.Armored
import me.proton.core.crypto.common.pgp.Unarmored
import me.proton.core.crypto.common.pgp.dataPacket
import me.proton.core.crypto.common.pgp.keyPacket
import me.proton.core.key.domain.decryptSessionKey
import me.proton.core.key.domain.entity.key.PublicKey
import me.proton.core.key.domain.signData
import me.proton.core.key.domain.useKeys
import me.proton.core.pass.domain.ItemContents
import me.proton.core.pass.domain.key.ItemKey
import me.proton.core.pass.domain.key.VaultKey
import me.proton.core.pass.domain.key.publicKey
import me.proton.core.pass.domain.key.usePrivateKey
import me.proton.core.user.domain.entity.UserAddress
import proton_pass_item_v1.ItemV1

@Serializable
data class CreateItemRequest(
    @SerialName("RotationID")
    val rotationId: String,
    @SerialName("Labels")
    val labels: List<String>,
    @SerialName("VaultKeyPacket")
    val vaultKeyPacket: String,
    @SerialName("VaultKeyPacketSignature")
    val vaultKeyPacketSignature: String,
    @SerialName("ContentFormatVersion")
    val contentFormatVersion: Int,
    @SerialName("Content")
    val content: String,
    @SerialName("UserSignature")
    val userSignature: String,
    @SerialName("ItemKeySignature")
    val itemKeySignature: String,
)

class CreateItem @Inject constructor(
    val cryptoContext: CryptoContext
) {
    companion object {
        const val CONTENT_FORMAT_VERSION = 1
    }

    fun createItem(
        vaultKey: VaultKey,
        itemKey: ItemKey,
        userAddress: UserAddress,
        itemContents: ItemContents
    ): CreateItemRequest {
        val serializedItem = serializeItem(itemContents)
        val vaultKeyPublicKey = vaultKey.publicKey(cryptoContext)

        val (encryptedContents, vaultKeyPacket) = encryptContent(serializedItem, vaultKeyPublicKey)
        val sessionKey = vaultKey.usePrivateKey(cryptoContext) {
            decryptSessionKey(vaultKeyPacket)
        }

        val userSignature = userAddress.useKeys(cryptoContext) { signData(serializedItem) }
        val (vaultKeyPacketSignature, itemKeySignature) = itemKey.usePrivateKey(cryptoContext) {
            Pair(signData(vaultKeyPacket), signData(serializedItem))
        }

        val encryptedUserSignature =
            cryptoContext.pgpCrypto.encryptData(unarmor(userSignature), sessionKey)
        val encryptedItemSignature =
            cryptoContext.pgpCrypto.encryptData(unarmor(itemKeySignature), sessionKey)

        return CreateItemRequest(
            rotationId = vaultKey.rotationId,
            labels = emptyList(),
            vaultKeyPacket = b64(vaultKeyPacket),
            vaultKeyPacketSignature = b64(unarmor(vaultKeyPacketSignature)),
            contentFormatVersion = CONTENT_FORMAT_VERSION,
            userSignature = b64(encryptedUserSignature),
            itemKeySignature = b64(encryptedItemSignature),
            content = b64(encryptedContents)
        )
    }

    private fun b64(data: ByteArray) = cryptoContext.pgpCrypto.getBase64Encoded(data)
    private fun unarmor(data: Armored) = cryptoContext.pgpCrypto.getUnarmored(data)

    private fun encryptContent(
        serializedItem: ByteArray,
        vaultPublicKey: PublicKey
    ): Pair<Unarmored, Unarmored> {
        val encrypted = cryptoContext.pgpCrypto.encryptData(serializedItem, vaultPublicKey.key)
        val packets = cryptoContext.pgpCrypto.getEncryptedPackets(encrypted)
        return Pair(packets.dataPacket(), packets.keyPacket())
    }

    private fun serializeItem(contents: ItemContents): ByteArray {
        val builder = ItemV1.Item.newBuilder()
            .setMetadata(
                ItemV1.Metadata.newBuilder()
                    .setName(contents.title)
                    .setNote(contents.note)
                    .build()
            )
        val contentBuilder = ItemV1.Content.newBuilder()
        val content = when (contents) {
            is ItemContents.Login -> {
                contentBuilder.setLogin(
                    ItemV1.ItemLogin.newBuilder()
                        .setUsername(contents.username)
                        .setPassword(contents.password)
                        .addAllUrls(contents.urls)
                        .build()
                )
            }
            is ItemContents.Note -> contentBuilder.setNote(
                ItemV1.ItemNote.newBuilder().build()
            )
            is ItemContents.Alias -> contentBuilder.setAlias(
                ItemV1.ItemAlias.newBuilder().build()
            )
        }.build()

        return builder
            .setContent(content)
            .build()
            .toByteArray()
    }
}
