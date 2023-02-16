package proton.android.pass.crypto.fakes.utils

import me.proton.core.crypto.android.context.AndroidCryptoContext
import me.proton.core.crypto.android.pgp.GOpenPGPCrypto
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.PlainByteArray
import me.proton.core.crypto.common.keystore.encrypt
import me.proton.core.crypto.common.pgp.Armored
import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.entity.key.KeyId
import me.proton.core.key.domain.entity.key.PrivateKey
import me.proton.core.user.domain.entity.AddressId
import me.proton.core.user.domain.entity.User
import me.proton.core.user.domain.entity.UserAddress
import me.proton.core.user.domain.entity.UserAddressKey
import me.proton.core.user.domain.entity.UserKey
import org.apache.commons.codec.binary.Base64
import proton.android.pass.crypto.api.EncryptionKey
import proton.android.pass.crypto.fakes.context.TestEncryptionContext
import proton.android.pass.test.TestUtils
import proton.android.pass.test.crypto.TestKeyStoreCrypto
import proton.pass.domain.key.ItemKey
import proton.pass.domain.key.ShareKey

@Suppress("MagicNumber", "UnderscoresInNumericLiterals")
object TestUtils {

    val cryptoContext: CryptoContext = AndroidCryptoContext(
        keyStoreCrypto = TestKeyStoreCrypto,
        pgpCrypto = GOpenPGPCrypto()
    )

    fun createUser(): User {
        val passphrase = TestUtils.randomString()
        val encodedPassphrase = passphrase.encodeToByteArray()
        val userKey = cryptoContext.pgpCrypto.generateNewPrivateKey("test", "test@test", encodedPassphrase)
        val encryptedPassphrase = cryptoContext.keyStoreCrypto.encrypt(PlainByteArray(encodedPassphrase))

        val userId = UserId("123")
        return User(
            userId = userId,
            email = "test@test",
            name = "test name",
            displayName = "Display name",
            currency = "EUR",
            credit = 0,
            usedSpace = 0,
            maxSpace = 10_000,
            maxUpload = 8_000,
            role = null,
            private = false,
            services = 123,
            subscribed = 1,
            delinquent = null,
            keys = listOf(
                UserKey(
                    userId = userId,
                    version = 1,
                    activation = null,
                    active = true,
                    keyId = KeyId("123"),
                    privateKey = PrivateKey(
                        key = userKey,
                        isPrimary = true,
                        isActive = true,
                        canEncrypt = true,
                        canVerify = true,
                        passphrase = encryptedPassphrase
                    )
                )
            )
        )
    }

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

    fun createShareKey(): Pair<ShareKey, EncryptionKey> {
        val key = EncryptionKey.generate()
        return ShareKey(
            rotation = 1,
            key = TestEncryptionContext.encrypt(key.key),
            responseKey = Base64.encodeBase64String(key.key),
            createTime = 123456789
        ) to key
    }

    fun createItemKey(): Pair<ItemKey, EncryptionKey> {
        val key = EncryptionKey.generate()
        return ItemKey(
            rotation = 1,
            key = TestEncryptionContext.encrypt(key.key),
            responseKey = Base64.encodeBase64String(key.key)
        ) to key
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
            val keyPassphrase = generatePassphrase().encodeToByteArray()
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
            )
        )
    }

    fun generatePassphrase(): String = TestUtils.randomString(32)
}

