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

import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.PlainByteArray
import me.proton.core.crypto.common.keystore.encrypt
import me.proton.core.crypto.common.pgp.Armored
import me.proton.core.key.domain.entity.key.KeyId
import me.proton.core.key.domain.entity.key.PrivateKey
import me.proton.core.user.domain.entity.AddressId
import me.proton.core.user.domain.entity.UserAddressKey

object UserAddressKeyTestFactory {

    fun createUserAddressKey(
        cryptoContext: CryptoContext,
        addressId: AddressId,
        key: Armored? = null,
        passphrase: ByteArray? = null
    ): UserAddressKey {
        val (userPrivateKey, keyPassphrase) = if (key != null && passphrase != null) {
            Pair(key, passphrase)
        } else {
            val keyPassphrase = StringTestFactory.generatePassphrase().encodeToByteArray()
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
}

