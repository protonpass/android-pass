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

package proton.android.pass.test.domain

import me.proton.core.crypto.android.context.AndroidCryptoContext
import me.proton.core.crypto.android.pgp.GOpenPGPCrypto
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.PlainByteArray
import me.proton.core.crypto.common.keystore.encrypt
import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.entity.key.KeyId
import me.proton.core.key.domain.entity.key.PrivateKey
import me.proton.core.user.domain.entity.Type
import me.proton.core.user.domain.entity.User
import me.proton.core.user.domain.entity.UserKey
import proton.android.pass.account.fakes.FakeKeyStoreCrypto
import proton.android.pass.test.StringTestFactory

object UserTestFactory {

    private val cryptoContext: CryptoContext = AndroidCryptoContext(
        keyStoreCrypto = FakeKeyStoreCrypto,
        pgpCrypto = GOpenPGPCrypto()
    )

    fun create(
        email: String? = null,
        name: String? = null,
        userId: UserId = UserId("12345")
    ): User = User(
        userId = userId,
        email = email,
        name = name,
        displayName = null,
        currency = "",
        type = Type.Proton,
        credit = 0,
        createdAtUtc = 0,
        usedSpace = 0,
        maxSpace = 0,
        maxUpload = 0,
        role = null,
        private = false,
        services = 0,
        subscribed = 0,
        delinquent = null,
        keys = listOf(),
        flags = emptyMap(),
        recovery = null
    )

    fun createWithKeys(
        email: String = "test@test",
        name: String = "test name",
        userId: UserId = UserId("123")
    ): User {
        val passphrase = StringTestFactory.randomString()
        val encodedPassphrase = passphrase.encodeToByteArray()
        val userKey =
            cryptoContext.pgpCrypto.generateNewPrivateKey("test", "test@test", encodedPassphrase)
        val encryptedPassphrase =
            cryptoContext.keyStoreCrypto.encrypt(PlainByteArray(encodedPassphrase))

        return User(
            userId = userId,
            email = email,
            name = name,
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
}
