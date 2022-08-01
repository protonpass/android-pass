package me.proton.core.pass.data.crypto

import javax.inject.Inject
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.PlainByteArray
import me.proton.core.crypto.common.keystore.encrypt
import me.proton.core.crypto.common.pgp.*
import me.proton.core.key.domain.encryptData
import me.proton.core.key.domain.entity.key.ArmoredKey
import me.proton.core.key.domain.entity.key.PrivateKey
import me.proton.core.key.domain.getEncryptedPackets
import me.proton.core.key.domain.signData
import me.proton.core.key.domain.useKeys
import me.proton.core.pass.domain.key.ItemKey
import me.proton.core.pass.domain.key.SigningKey
import me.proton.core.pass.domain.key.VaultKey
import me.proton.core.pass.domain.key.usePrivateKey
import me.proton.core.pass.domain.repositories.VaultItemKeyList
import me.proton.core.user.domain.entity.UserAddress
import proton_key_vault_v1.VaultV1

@Serializable
data class CreateVaultRequest(
    @SerialName("AddressID")
    val addressId: String,
    @SerialName("Content")
    val content: String,
    @SerialName("ContentFormatVersion")
    val contentFormatVersion: Int,
    @SerialName("ContentEncryptedAddressSignature")
    val contentEncryptedAddressSignature: String,
    @SerialName("ContentEncryptedVaultSignature")
    val contentEncryptedVaultSignature: String,
    @SerialName("VaultKey")
    val vaultKey: String,
    @SerialName("VaultKeyPassphrase")
    val vaultKeyPassphrase: String,
    @SerialName("VaultKeySignature")
    val vaultKeySignature: String,
    @SerialName("KeyPacket")
    val keyPacket: String,
    @SerialName("KeyPacketSignature")
    val keyPacketSignature: String,
    @SerialName("SigningKey")
    val signingKey: String,
    @SerialName("SigningKeyPassphrase")
    val signingKeyPassphrase: String,
    @SerialName("SigningKeyPassphraseKeyPacket")
    val signingKeyPassphraseKeyPacket: String,
    @SerialName("AcceptanceSignature")
    val acceptanceSignature: String,
    @SerialName("ItemKey")
    val itemKey: String,
    @SerialName("ItemKeyPassphrase")
    val itemKeyPassphrase: String,
    @SerialName("ItemKeyPassphraseKeyPacket")
    val itemKeyPassphraseKeyPacket: String,
    @SerialName("ItemKeySignature")
    val itemKeySignature: String,
)

class CreateVault @Inject constructor(
    val cryptoContext: CryptoContext
) {

    companion object {
        const val CONTENT_FORMAT_VERSION = 1
    }

    fun createVaultRequest(
        vaultMetadata: VaultV1.Vault,
        userAddress: UserAddress
    ): Pair<CreateVaultRequest, VaultItemKeyList> {
        // Generate signing key
        val (signingKeyPassphrase, lockedSigningKey) = generateKeyWithPassphrase(
            "VaultSigningKey",
            "vault_signing@proton"
        )
        val signingKey = SigningKey(ArmoredKey.Private(lockedSigningKey.key, lockedSigningKey))
        val (signingKeyEncryptedPassphrase, signingKeyPassphraseKeyPacket, signingKeySignature) = userAddress.useKeys(
            cryptoContext
        ) {
            val encryptedPassphrase = encryptData(signingKeyPassphrase)
            val packets = getEncryptedPackets(encryptedPassphrase)

            val keyFingerprint = Utils.getPrimaryV5Fingerprint(cryptoContext, lockedSigningKey.key)
            val keySignature = signData(keyFingerprint.encodeToByteArray())
            PacketsAndSignature(
                dataPacket = packets.dataPacket(),
                keyPacket = packets.keyPacket(),
                signature = keySignature
            )
        }

        // Generate vault key
        val (vaultKeyPassphrase, lockedVaultKey) = generateKeyWithPassphrase(
            "VaultKey",
            "vault@proton"
        )

        val (vaultKeyEncryptedPassphrase, vaultKeyPassphraseKeyPacket, vaultKeySignature) = userAddress.useKeys(
            cryptoContext
        ) {
            val encryptedPassphrase = encryptData(vaultKeyPassphrase)
            val packets = getEncryptedPackets(encryptedPassphrase)

            val keyFingerprint = Utils.getPrimaryV5Fingerprint(cryptoContext, lockedVaultKey.key)
            val keySignature = signingKey.usePrivateKey(cryptoContext) {
                signData(keyFingerprint.encodeToByteArray())
            }

            PacketsAndSignature(
                dataPacket = packets.dataPacket(),
                keyPacket = packets.keyPacket(),
                signature = keySignature
            )
        }
        val vaultKey = VaultKey(
            "TEMP_ROTATION_ID",
            1,
            ArmoredKey.Private(lockedVaultKey.key, lockedVaultKey),
            lockedVaultKey.passphrase
        )

        val serializedMetadata = vaultMetadata.toByteArray()
        val encryptedName =
            vaultKey.usePrivateKey(cryptoContext) { encryptData(serializedMetadata) }

        val (nameVaultKeySignature, vaultKeyPassphraseKeyPacketSignature) = vaultKey.usePrivateKey(
            cryptoContext
        ) {
            Pair(signData(serializedMetadata), signData(vaultKeyPassphraseKeyPacket))
        }

        // Generate item key
        val (itemKeyPassphrase, lockedItemKey) = generateKeyWithPassphrase(
            "ItemKey",
            "item@proton"
        )

        val (itemKeyEncryptedPassphrase, itemKeyPassphraseKeyPacket, itemKeySignature) = vaultKey.usePrivateKey(
            cryptoContext
        ) {
            val encryptedPassphrase = encryptData(itemKeyPassphrase)
            val packets = getEncryptedPackets(encryptedPassphrase)

            val keyFingerprint = Utils.getPrimaryV5Fingerprint(cryptoContext, lockedItemKey.key)
            val keySignature = signingKey.usePrivateKey(cryptoContext) {
                signData(keyFingerprint.encodeToByteArray())
            }

            PacketsAndSignature(
                dataPacket = packets.dataPacket(),
                keyPacket = packets.keyPacket(),
                signature = keySignature
            )
        }

        val nameAddressSignature = userAddress.useKeys(cryptoContext) {
            signData(serializedMetadata)
        }

        val (encryptedNameAddressSignature, encryptedNameVaultSignature) = vaultKey.usePrivateKey(
            cryptoContext
        ) {
            Pair(
                encryptData(nameAddressSignature.encodeToByteArray()),
                encryptData(nameVaultKeySignature.encodeToByteArray())
            )
        }

        val b64 = ({ input: ByteArray -> cryptoContext.pgpCrypto.getBase64Encoded(input) })
        val unarmor = ({ input: Armored -> cryptoContext.pgpCrypto.getUnarmored(input) })

        return CreateVaultRequest(
            addressId = userAddress.addressId.id,

            // Name
            content = b64(unarmor(encryptedName)),
            contentFormatVersion = CONTENT_FORMAT_VERSION,
            contentEncryptedAddressSignature = b64(unarmor(encryptedNameAddressSignature)),
            contentEncryptedVaultSignature = b64(unarmor(encryptedNameVaultSignature)),

            // Vault Key
            vaultKey = lockedVaultKey.key,
            vaultKeyPassphrase = b64(vaultKeyEncryptedPassphrase),
            vaultKeySignature = b64(unarmor(vaultKeySignature)),
            keyPacket = b64(vaultKeyPassphraseKeyPacket),
            keyPacketSignature = b64(unarmor(vaultKeyPassphraseKeyPacketSignature)),

            // Signing key
            signingKey = lockedSigningKey.key,
            signingKeyPassphrase = b64(signingKeyEncryptedPassphrase),
            signingKeyPassphraseKeyPacket = b64(signingKeyPassphraseKeyPacket),
            acceptanceSignature = b64(unarmor(signingKeySignature)),

            // Item key
            itemKey = lockedItemKey.key,
            itemKeyPassphrase = b64(itemKeyEncryptedPassphrase),
            itemKeyPassphraseKeyPacket = b64(itemKeyPassphraseKeyPacket),
            itemKeySignature = b64(unarmor(itemKeySignature)),
        ) to VaultItemKeyList(
            vaultKeyList = listOf(vaultKey),
            itemKeyList = listOf(
                ItemKey(
                    rotationId = "TEMP_ROTATION_ID",
                    key = ArmoredKey.Private(lockedItemKey.key, lockedItemKey),
                    encryptedKeyPassphrase = PlainByteArray(itemKeyPassphrase).encrypt(cryptoContext.keyStoreCrypto)
                )
            )
        )
    }

    private fun generateKeyWithPassphrase(
        username: String,
        domain: String
    ): GeneratedKey {
        val passphrase = Utils.generatePassphrase()
        val key = cryptoContext.pgpCrypto.generateNewPrivateKey(
            username,
            domain,
            passphrase.encodeToByteArray()
        )
        val encryptedPassphrase =
            PlainByteArray(passphrase.encodeToByteArray()).encrypt(cryptoContext.keyStoreCrypto)
        return GeneratedKey(
            passphrase.encodeToByteArray(),
            PrivateKey(
                key,
                isPrimary = true,
                passphrase = encryptedPassphrase
            )
        )
    }

    internal data class GeneratedKey(
        val passphrase: ByteArray,
        val privateKey: PrivateKey,
    )

    internal data class PacketsAndSignature(
        val dataPacket: Unarmored,
        val keyPacket: Unarmored,
        val signature: Signature
    )
}
