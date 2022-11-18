package me.proton.android.pass.data.impl.crypto

import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.PlainByteArray
import me.proton.core.crypto.common.keystore.encrypt
import me.proton.core.crypto.common.pgp.Armored
import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.entity.key.ArmoredKey
import me.proton.core.key.domain.entity.key.KeyId
import me.proton.core.key.domain.entity.key.PrivateKey
import me.proton.core.test.android.instrumented.utils.StringUtils
import me.proton.core.user.domain.entity.AddressId
import me.proton.core.user.domain.entity.UserAddress
import me.proton.core.user.domain.entity.UserAddressKey
import me.proton.pass.domain.key.ItemKey
import me.proton.pass.domain.key.VaultKey

object TestUtils {

    fun createUserAddress(
        cryptoContext: CryptoContext,
        key: Armored? = null,
        passphrase: ByteArray? = null
    ): UserAddress {
        val addressId = AddressId("abc")
        return UserAddress(
            UserId("123"),
            addressId,
            "test@test",
            canSend = true,
            canReceive = true,
            enabled = true,
            keys = listOf(createUserAddressKey(cryptoContext, addressId, key, passphrase)),
            signedKeyList = null,
            order = 1
        )
    }

    fun createVaultKeyItemKey(cryptoContext: CryptoContext): Pair<VaultKey, ItemKey> {
        val vaultKey = createVaultKey(cryptoContext)
        val itemKey = createItemKeyForVaultKey(cryptoContext, vaultKey)
        return Pair(vaultKey, itemKey)
    }

    fun createVaultKey(cryptoContext: CryptoContext): VaultKey {
        val passphrase = StringUtils.randomString()
        val encodedPassphrase = passphrase.encodeToByteArray()
        val key = cryptoContext.pgpCrypto.generateNewPrivateKey("TestVault", "test@vault", encodedPassphrase)
        val encryptedPassphrase = cryptoContext.keyStoreCrypto.encrypt(PlainByteArray(encodedPassphrase))

        return VaultKey(
            rotationId = StringUtils.randomString(),
            rotation = 1,
            key = ArmoredKey.Private(
                armored = key,
                key = PrivateKey(
                    key = key,
                    isPrimary = true,
                    passphrase = encryptedPassphrase
                )
            ),
            encryptedKeyPassphrase = encryptedPassphrase
        )
    }

    fun createItemKeyForVaultKey(cryptoContext: CryptoContext, vaultKey: VaultKey): ItemKey {
        val passphrase = StringUtils.randomString()
        val encodedPassphrase = passphrase.encodeToByteArray()
        val key = cryptoContext.pgpCrypto.generateNewPrivateKey("TestItem", "test@item", encodedPassphrase)
        val encryptedPassphrase = cryptoContext.keyStoreCrypto.encrypt(PlainByteArray(encodedPassphrase))

        return ItemKey(
            rotationId = vaultKey.rotationId,
            key = ArmoredKey.Private(
                armored = key,
                key = PrivateKey(
                    key = key,
                    isPrimary = true,
                    passphrase = encryptedPassphrase
                )
            ),
            encryptedKeyPassphrase = encryptedPassphrase
        )
    }

    fun createUserAddressKey(
        cryptoContext: CryptoContext,
        addressId: AddressId,
        key: Armored? = null,
        passphrase: ByteArray? = null
    ): UserAddressKey {
        val (userPrivateKey, keyPassphrase) = if (key != null && passphrase != null) {
            Pair(key, passphrase)
        } else {
            val keyPassphrase = Utils.generatePassphrase().encodeToByteArray()
            val userPrivateKey = cryptoContext
                .pgpCrypto
                .generateNewPrivateKey(
                    "androidTest",
                    "androidTest@androidTest",
                    keyPassphrase
                )
            Pair(userPrivateKey, keyPassphrase)
        }

        return UserAddressKey(
            addressId,
            1,
            123,
            null,
            null,
            null,
            true,
            KeyId("asda"),
            PrivateKey(
                userPrivateKey,
                passphrase = PlainByteArray(keyPassphrase).encrypt(cryptoContext.keyStoreCrypto),
                isPrimary = true
            ),
        )
    }
}
