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
import proton.android.pass.crypto.api.usecases.EncryptedMigrateItemBody
import proton.android.pass.crypto.api.usecases.MigrateItem
import proton.android.pass.domain.key.ShareKey
import javax.inject.Inject

class MigrateItemImpl @Inject constructor(
    private val encryptionContextProvider: EncryptionContextProvider
) : MigrateItem {
    override fun migrate(
        destinationKey: ShareKey,
        encryptedItemContents: EncryptedByteArray,
        contentFormatVersion: Int
    ): EncryptedMigrateItemBody {
        val (decryptedDestinationKey, decryptedContents) =
            encryptionContextProvider.withEncryptionContext {
                EncryptionKey(decrypt(destinationKey.key)) to decrypt(encryptedItemContents)
            }

        val newItemKey = EncryptionKey.generate()
        val encryptedItemKey =
            encryptionContextProvider.withEncryptionContext(decryptedDestinationKey) {
                encrypt(newItemKey.value(), EncryptionTag.ItemKey)
            }

        val reencryptedContents = encryptionContextProvider.withEncryptionContext(newItemKey) {
            encrypt(decryptedContents, EncryptionTag.ItemContent)
        }


        return EncryptedMigrateItemBody(
            keyRotation = destinationKey.rotation,
            contentFormatVersion = contentFormatVersion,
            content = Base64.encodeBase64String(reencryptedContents.array),
            itemKey = Base64.encodeBase64String(encryptedItemKey.array)
        )

    }
}
