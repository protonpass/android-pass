package me.proton.core.pass.data.crypto

import javax.inject.Inject
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.PlainByteArray
import me.proton.core.crypto.common.keystore.encrypt
import me.proton.core.crypto.common.pgp.Armored
import me.proton.core.crypto.common.pgp.dataPacket
import me.proton.core.crypto.common.pgp.keyPacket
import me.proton.core.key.domain.encryptData
import me.proton.core.key.domain.entity.key.ArmoredKey
import me.proton.core.key.domain.entity.key.PrivateKey
import me.proton.core.key.domain.entity.key.PrivateKeyRing
import me.proton.core.key.domain.entity.key.PublicKey
import me.proton.core.key.domain.entity.key.PublicKeyRing
import me.proton.core.key.domain.entity.keyholder.KeyHolderContext
import me.proton.core.key.domain.getEncryptedPackets
import me.proton.core.key.domain.publicKey
import me.proton.core.key.domain.signData
import me.proton.core.key.domain.useKeys
import me.proton.core.pass.domain.key.ItemKey
import me.proton.core.pass.domain.key.VaultKey
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

    fun createVaultRequest(vaultMetadata: VaultV1.Vault, userAddress: UserAddress): Pair<CreateVaultRequest, VaultItemKeyList> {
        // Generate signing key
        val (signingKeyPassphrase, lockedSigningKey, signingKeyPublicKey) = generateKeyWithPassphrase(
            "VaultSigningKey",
            "vault_signing@proton"
        )
        val (signingKeyEncryptedPassphrase, signingKeyPassphraseKeyPacket, signingKeySignature) = userAddress.useKeys(
            cryptoContext
        ) {
            val encryptedPassphrase = encryptData(signingKeyPassphrase)
            val packets = getEncryptedPackets(encryptedPassphrase)

            val keyFingerprint = Utils.getPrimaryV5Fingerprint(cryptoContext, lockedSigningKey.key)
            val keySignature = signData(keyFingerprint.encodeToByteArray())

            Triple(packets.dataPacket(), packets.keyPacket(), keySignature)
        }

        // Generate vault key
        val (vaultKeyPassphrase, lockedVaultKey, vaultKeyPublicKey) = generateKeyWithPassphrase(
            "VaultKey",
            "vault@proton"
        )

        val (vaultKeyEncryptedPassphrase, vaultKeyPassphraseKeyPacket, vaultKeySignature) = userAddress.useKeys(
            cryptoContext
        ) {
            val encryptedPassphrase = encryptData(vaultKeyPassphrase)
            val packets = getEncryptedPackets(encryptedPassphrase)

            val keyFingerprint = Utils.getPrimaryV5Fingerprint(cryptoContext, lockedVaultKey.key)
            val keySignature = KeyHolderContext(
                cryptoContext,
                PrivateKeyRing(cryptoContext, listOf(lockedSigningKey)),
                PublicKeyRing(listOf(signingKeyPublicKey))
            ).use {
                it.signData(keyFingerprint.encodeToByteArray())
            }

            Triple(packets.dataPacket(), packets.keyPacket(), keySignature)
        }
        val vaultKeyKeyHolder = KeyHolderContext(
            cryptoContext,
            PrivateKeyRing(cryptoContext, listOf(lockedVaultKey)),
            PublicKeyRing(listOf(vaultKeyPublicKey))
        )

        val serializedMetadata = vaultMetadata.toByteArray()
        val encryptedName = vaultKeyKeyHolder.encryptData(serializedMetadata)
        val nameVaultKeySignature = vaultKeyKeyHolder.signData(serializedMetadata)
        val vaultKeyPassphraseKeyPacketSignature =
            vaultKeyKeyHolder.signData(vaultKeyPassphraseKeyPacket)

        // Generate item key
        val (itemKeyPassphrase, lockedItemKey) = generateKeyWithPassphrase(
            "ItemKey",
            "item@proton"
        )

        val (itemKeyEncryptedPassphrase, itemKeyPassphraseKeyPacket, itemKeySignature) = vaultKeyKeyHolder.use {
            val encryptedPassphrase = it.encryptData(itemKeyPassphrase)
            val packets = it.getEncryptedPackets(encryptedPassphrase)

            val keyFingerprint = Utils.getPrimaryV5Fingerprint(cryptoContext, lockedItemKey.key)
            val keySignature = KeyHolderContext(
                cryptoContext,
                PrivateKeyRing(cryptoContext, listOf(lockedSigningKey)),
                PublicKeyRing(listOf(signingKeyPublicKey))
            ).use { signingKeyContext ->
                signingKeyContext.signData(keyFingerprint.encodeToByteArray())
            }

            Triple(packets.dataPacket(), packets.keyPacket(), keySignature)
        }

        val nameAddressSignature = userAddress.useKeys(cryptoContext) {
            signData(serializedMetadata)
        }

        val encryptedNameAddressSignature =
            vaultKeyKeyHolder.encryptData(nameAddressSignature.encodeToByteArray())
        val encryptedNameVaultSignature =
            vaultKeyKeyHolder.encryptData(nameVaultKeySignature.encodeToByteArray())

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
            vaultKeyList = listOf(
                VaultKey(
                    rotationId = "TEMP_ROTATION_ID",
                    rotation = 1,
                    key = ArmoredKey.Private(lockedVaultKey.key, lockedVaultKey),
                    encryptedKeyPassphrase = PlainByteArray(vaultKeyPassphrase).encrypt(cryptoContext.keyStoreCrypto)
                )
            ),
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
    ): Triple<ByteArray, PrivateKey, PublicKey> {
        val passphrase = Utils.generatePassphrase()
        val key = cryptoContext.pgpCrypto.generateNewPrivateKey(
            username,
            domain,
            passphrase.encodeToByteArray()
        )
        val encryptedPassphrase = PlainByteArray(passphrase.encodeToByteArray()).encrypt(cryptoContext.keyStoreCrypto)
        val privateKey = PrivateKey(
            key,
            isPrimary = true,
            passphrase = encryptedPassphrase
        )
        return Triple(
            passphrase.encodeToByteArray(),
            privateKey,
            privateKey.publicKey(cryptoContext)
        )
    }
}
