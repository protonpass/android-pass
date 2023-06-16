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

package proton.android.pass.crypto.impl.usecases

import me.proton.core.crypto.common.keystore.EncryptedByteArray
import proton.android.pass.crypto.api.Base64
import proton.android.pass.crypto.api.EncryptionKey
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.crypto.api.context.EncryptionTag
import proton.android.pass.crypto.api.usecases.EncryptedItemKey
import proton.android.pass.crypto.api.usecases.OpenItemKey
import proton.pass.domain.key.ItemKey
import proton.pass.domain.key.ShareKey
import javax.inject.Inject

class OpenItemKeyImpl @Inject constructor(
    private val encryptionContextProvider: EncryptionContextProvider
) : OpenItemKey {
    override fun invoke(shareKey: ShareKey, key: EncryptedItemKey): ItemKey {
        if (shareKey.rotation != key.keyRotation) {
            throw IllegalStateException(
                "Received ShareKey with rotation not matching ItemKey " +
                    "rotation [shareKey=${shareKey.rotation}] [itemKey=${key.keyRotation}]"
            )
        }

        val decryptedShareKey = encryptionContextProvider.withEncryptionContext {
            EncryptionKey(decrypt(shareKey.key))
        }

        val decodedItemKey = Base64.decodeBase64(key.key)
        val decryptedItemKey = encryptionContextProvider.withEncryptionContext(decryptedShareKey) {
            decrypt(EncryptedByteArray(decodedItemKey), EncryptionTag.ItemKey)
        }

        val reencryptedItemKey = encryptionContextProvider.withEncryptionContext {
            encrypt(decryptedItemKey)
        }

        return ItemKey(
            rotation = key.keyRotation,
            key = reencryptedItemKey,
            responseKey = key.key
        )
    }
}
