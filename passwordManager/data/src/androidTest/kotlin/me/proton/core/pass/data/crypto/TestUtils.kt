package me.proton.core.pass.data.crypto

import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.pgp.Armored
import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.entity.key.KeyId
import me.proton.core.key.domain.entity.key.PrivateKey
import me.proton.core.user.domain.entity.AddressId
import me.proton.core.user.domain.entity.UserAddress
import me.proton.core.user.domain.entity.UserAddressKey

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
                passphrase = EncryptedByteArray(keyPassphrase),
                isPrimary = true
            ),
        )
    }
}
