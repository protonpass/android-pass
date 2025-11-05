/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.test

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
import me.proton.core.user.domain.entity.Type
import me.proton.core.user.domain.entity.User
import me.proton.core.user.domain.entity.UserAddress
import me.proton.core.user.domain.entity.UserAddressKey
import me.proton.core.user.domain.entity.UserKey
import proton.android.pass.account.fakes.TestKeyStoreCrypto
import proton.android.pass.crypto.api.Base64
import proton.android.pass.crypto.api.EncryptionKey
import proton.android.pass.crypto.fakes.context.TestEncryptionContext
import proton.android.pass.domain.key.ItemKey
import proton.android.pass.domain.key.ShareKey

object TestUtils {

    private val cryptoContext: CryptoContext = AndroidCryptoContext(
        keyStoreCrypto = TestKeyStoreCrypto,
        pgpCrypto = GOpenPGPCrypto()
    )

    fun createUser(): User {
        val passphrase = TestUtils.randomString()
        val encodedPassphrase = passphrase.encodeToByteArray()
        val userKey =
            cryptoContext.pgpCrypto.generateNewPrivateKey("test", "test@test", encodedPassphrase)
        val encryptedPassphrase =
            cryptoContext.keyStoreCrypto.encrypt(PlainByteArray(encodedPassphrase))

        val userId = UserId("123")
        return User(
            userId = userId,
            email = "test@test",
            name = "test name",
            displayName = "Display name",
            currency = "EUR",
            type = Type.Proton,
            credit = 0,
            createdAtUtc = 0,
            usedSpace = 0,
            maxSpace = 10_000,
            maxUpload = 8_000,
            role = null,
            private = false,
            services = 123,
            subscribed = 1,
            delinquent = null,
            recovery = null,
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
            ),
            flags = emptyMap()
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
            key = TestEncryptionContext.encrypt(key.value()),
            responseKey = Base64.encodeBase64String(key.value()),
            createTime = 123_456_789,
            isActive = true,
            userKeyId = "userKeyId"
        ) to key
    }

    fun createItemKey(): Pair<ItemKey, EncryptionKey> {
        val key = EncryptionKey.generate()
        return ItemKey(
            rotation = 1,
            key = TestEncryptionContext.encrypt(key.value()),
            responseKey = Base64.encodeBase64String(key.value())
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

    fun generatePassphrase(): String = randomString(32)

    fun randomString(length: Int = 10): String {
        val dict = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        var res = ""
        while (res.length < length) {
            res += dict.random()
        }
        return res
    }
}
