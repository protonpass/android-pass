package me.proton.android.pass.crypto.impl.usecases

import me.proton.android.pass.crypto.api.usecases.EncryptedCreateVault
import me.proton.android.pass.crypto.impl.TestUtils
import me.proton.core.crypto.android.context.AndroidCryptoContext
import me.proton.core.crypto.android.pgp.GOpenPGPCrypto
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.PlainByteArray
import me.proton.core.crypto.common.keystore.encrypt
import me.proton.core.crypto.common.pgp.PGPHeader
import me.proton.core.crypto.common.pgp.dataPacket
import me.proton.core.crypto.common.pgp.keyPacket
import me.proton.core.key.domain.decryptData
import me.proton.core.key.domain.decryptSessionKey
import me.proton.core.key.domain.entity.key.PrivateKey
import me.proton.core.key.domain.entity.key.PrivateKeyRing
import me.proton.core.key.domain.entity.key.PublicKeyRing
import me.proton.core.key.domain.entity.keyholder.KeyHolderContext
import me.proton.core.key.domain.getArmored
import me.proton.core.key.domain.getBase64Decoded
import me.proton.core.key.domain.getEncryptedPackets
import me.proton.core.key.domain.publicKey
import me.proton.core.key.domain.useKeys
import me.proton.core.key.domain.verifyData
import me.proton.core.user.domain.entity.UserAddress
import me.proton.pass.test.crypto.TestKeyStoreCrypto
import org.junit.Test
import proton_pass_vault_v1.VaultV1
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CreateVaultImplTest {
    private val cryptoContext: CryptoContext = AndroidCryptoContext(
        keyStoreCrypto = TestKeyStoreCrypto,
        pgpCrypto = GOpenPGPCrypto(),
    )

    @Test
    fun canCreateVault() {
        val userAddress = TestUtils.createUserAddress(cryptoContext)
        val vaultName = Utils.generatePassphrase()
        val vaultDescription = Utils.generatePassphrase()
        val vaultMetadata = VaultV1.Vault.newBuilder().setName(vaultName).setDescription(vaultDescription).build()

        val instance = CreateVaultImpl(cryptoContext)
        val(request, _) = instance.createVaultRequest(vaultMetadata, userAddress)

        val signingKey = validateSigningKey(request, userAddress)
        val vaultKey = validateVaultKey(request, userAddress, signingKey)
        validateItemKey(request, signingKey, vaultKey)
        validateVaultMetadata(vaultMetadata, request, vaultKey)
    }

    private fun validateSigningKey(
        request: EncryptedCreateVault,
        userAddress: UserAddress
    ): PrivateKey {
        val crypto = cryptoContext.pgpCrypto
        assertTrue(request.signingKey.isNotEmpty())
        assertTrue(request.signingKeyPassphrase.isNotEmpty())
        assertTrue(request.signingKeyPassphraseKeyPacket.isNotEmpty())

        val passphraseKeyPacket = crypto.getBase64Decoded(request.signingKeyPassphraseKeyPacket)
        val decodedPassphrase = crypto.getBase64Decoded(request.signingKeyPassphrase)

        val signingKey = userAddress.useKeys(cryptoContext) {
            val passphraseSessionKey = decryptSessionKey(passphraseKeyPacket)
            val passphrase = decryptData(decodedPassphrase, passphraseSessionKey)
            readKey(request.signingKey, passphrase)
        }

        // Signature verification
        val signingKeyFingerprint = Utils.getPrimaryV5Fingerprint(cryptoContext, signingKey.key)
        val decodedAcceptanceSignature = crypto.getBase64Decoded(request.acceptanceSignature)

        val armoredAcceptanceSignature = crypto.getArmored(decodedAcceptanceSignature, PGPHeader.Signature)
        val verified = userAddress.useKeys(cryptoContext) {
            verifyData(signingKeyFingerprint.encodeToByteArray(), armoredAcceptanceSignature)
        }

        assertTrue(verified)
        return signingKey
    }

    private fun validateVaultKey(
        request: EncryptedCreateVault,
        userAddress: UserAddress,
        signingKey: PrivateKey
    ): PrivateKey {
        val crypto = cryptoContext.pgpCrypto
        val decodedPassphraseKeyPacket = crypto.getBase64Decoded(request.keyPacket)
        val decodedPassphrase = crypto.getBase64Decoded(request.vaultKeyPassphrase)

        val vaultKey = userAddress.useKeys(cryptoContext) {
            val passphraseSessionKey = decryptSessionKey(decodedPassphraseKeyPacket)
            val passphrase = crypto.decryptData(decodedPassphrase, passphraseSessionKey)
            readKey(request.vaultKey, passphrase)
        }

        // Signature verification
        val vaultKeyFingerprint = Utils.getPrimaryV5Fingerprint(cryptoContext, vaultKey.key)
        val vaultKeySignature = crypto.getBase64Decoded(request.vaultKeySignature)
        val armoredVaultKeySignature = crypto.getArmored(vaultKeySignature, PGPHeader.Signature)

        val signingKeyPublicKey = signingKey.publicKey(cryptoContext)
        val verified = crypto.verifyData(
            vaultKeyFingerprint.encodeToByteArray(),
            armoredVaultKeySignature,
            signingKeyPublicKey.key
        )
        assertTrue(verified)
        return vaultKey
    }

    private fun validateItemKey(
        request: EncryptedCreateVault,
        signingKey: PrivateKey,
        vaultKey: PrivateKey,
    ) {
        val crypto = cryptoContext.pgpCrypto

        val decodedPassphraseKeyPacket = crypto.getBase64Decoded(request.itemKeyPassphraseKeyPacket)
        val decodedPassphrase = crypto.getBase64Decoded(request.itemKeyPassphrase)

        val vaultKeyContext = KeyHolderContext(
            cryptoContext,
            PrivateKeyRing(cryptoContext, listOf(vaultKey)),
            PublicKeyRing(listOf(vaultKey.publicKey(cryptoContext)))
        )
        val passphraseSessionKey = vaultKeyContext.decryptSessionKey(decodedPassphraseKeyPacket)
        val passphrase = passphraseSessionKey.decryptData(cryptoContext, decodedPassphrase)

        val itemKey = readKey(request.itemKey, passphrase)

        // Signature verification
        val itemKeyFingerprint = Utils.getPrimaryV5Fingerprint(cryptoContext, itemKey.key)
        val itemKeySignature = crypto.getBase64Decoded(request.itemKeySignature)
        val armoredItemKeySignature = crypto.getArmored(itemKeySignature, PGPHeader.Signature)

        val verified = crypto.verifyData(
            itemKeyFingerprint.encodeToByteArray(),
            armoredItemKeySignature,
            signingKey.publicKey(cryptoContext).key
        )
        assertTrue(verified)
    }

    private fun validateVaultMetadata(
        metadata: VaultV1.Vault,
        request: EncryptedCreateVault,
        vaultKey: PrivateKey
    ) {
        val vaultKeyContext = KeyHolderContext(
            cryptoContext,
            PrivateKeyRing(cryptoContext, listOf(vaultKey)),
            PublicKeyRing(listOf(vaultKey.publicKey(cryptoContext)))
        )

        val decrypted = vaultKeyContext.use {
            val decodedContents = it.getArmored(it.getBase64Decoded(request.content))
            val packets = it.getEncryptedPackets(decodedContents)
            it.decryptData(packets.dataPacket(), packets.keyPacket())
        }

        val parsed = VaultV1.Vault.parseFrom(decrypted)
        assertEquals(metadata.name, parsed.name)
        assertEquals(metadata.description, parsed.description)
    }

    private fun readKey(key: String, passphrase: ByteArray): PrivateKey {
        return PrivateKey(
            key = key,
            isPrimary = true,
            isActive = true,
            canEncrypt = true,
            canVerify = true,
            passphrase = PlainByteArray(passphrase).encrypt(cryptoContext.keyStoreCrypto),
        )
    }
}

