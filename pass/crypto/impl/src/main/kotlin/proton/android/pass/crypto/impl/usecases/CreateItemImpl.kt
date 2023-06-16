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

import proton.android.pass.crypto.api.Base64
import proton.android.pass.crypto.api.EncryptionKey
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.crypto.api.context.EncryptionTag
import proton.android.pass.crypto.api.usecases.CreateItem
import proton.android.pass.crypto.api.usecases.CreateItemPayload
import proton.android.pass.crypto.api.usecases.EncryptedCreateItem
import proton.android.pass.crypto.impl.usecases.Utils.generateUuid
import proton.android.pass.datamodels.api.serializeToProto
import proton.pass.domain.ItemContents
import proton.pass.domain.key.ShareKey
import javax.inject.Inject

class CreateItemImpl @Inject constructor(
    private val encryptionContextProvider: EncryptionContextProvider,
) : CreateItem {

    override fun create(
        shareKey: ShareKey,
        itemContents: ItemContents
    ): CreateItemPayload {
        val serializedItem = encryptionContextProvider.withEncryptionContext {
            itemContents.serializeToProto(itemUuid = generateUuid(), this).toByteArray()
        }
        val itemKey = EncryptionKey.generate()

        val encryptedContents = encryptionContextProvider.withEncryptionContext(itemKey.clone()) {
            encrypt(serializedItem, EncryptionTag.ItemContent)
        }

        val decryptedShareKey = encryptionContextProvider.withEncryptionContext {
            EncryptionKey(decrypt(shareKey.key))
        }

        val encryptedItemKey = encryptionContextProvider.withEncryptionContext(decryptedShareKey) {
            encrypt(itemKey.value(), EncryptionTag.ItemKey)
        }

        val request = EncryptedCreateItem(
            keyRotation = shareKey.rotation,
            contentFormatVersion = CONTENT_FORMAT_VERSION,
            content = Base64.encodeBase64String(encryptedContents.array),
            itemKey = Base64.encodeBase64String(encryptedItemKey.array)
        )
        return CreateItemPayload(
            request = request,
            itemKey = itemKey
        )
    }

    companion object {
        const val CONTENT_FORMAT_VERSION = 1
    }
}

