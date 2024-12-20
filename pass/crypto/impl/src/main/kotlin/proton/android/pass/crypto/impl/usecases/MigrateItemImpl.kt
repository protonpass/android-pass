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

import proton.android.pass.crypto.api.EncryptionKey
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.crypto.api.context.EncryptionTag
import proton.android.pass.crypto.api.usecases.ItemKeyWithRotation
import proton.android.pass.crypto.api.usecases.MigrateItem
import proton.android.pass.domain.key.ShareKey
import javax.inject.Inject

class MigrateItemImpl @Inject constructor(
    private val encryptionContextProvider: EncryptionContextProvider
) : MigrateItem {

    override fun migrate(destinationKey: ShareKey, itemKeys: List<ItemKeyWithRotation>): List<ItemKeyWithRotation> {
        val decryptedDestinationKey = encryptionContextProvider.withEncryptionContext {
            EncryptionKey(decrypt(destinationKey.key))
        }
        val decryptedItemKeys = encryptionContextProvider.withEncryptionContext {
            itemKeys.map { decrypt(it.itemKey) to it.keyRotation }
        }
        val encryptedItemKeys: List<ItemKeyWithRotation> =
            encryptionContextProvider.withEncryptionContext(decryptedDestinationKey) {
                decryptedItemKeys.map {
                    ItemKeyWithRotation(
                        itemKey = encrypt(it.first, EncryptionTag.ItemKey),
                        keyRotation = it.second
                    )
                }
            }

        return encryptedItemKeys
    }
}
