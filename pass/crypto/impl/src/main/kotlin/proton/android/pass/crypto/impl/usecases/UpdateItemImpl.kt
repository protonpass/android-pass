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
import proton.android.pass.crypto.api.usecases.EncryptedUpdateItemRequest
import proton.android.pass.crypto.api.usecases.UpdateItem
import proton.android.pass.domain.key.ItemKey
import proton_pass_item_v1.ItemV1
import javax.inject.Inject

class UpdateItemImpl @Inject constructor(
    private val encryptionContextProvider: EncryptionContextProvider
) : UpdateItem {

    override fun createRequest(
        itemKey: ItemKey,
        itemContent: ItemV1.Item,
        lastRevision: Long
    ): EncryptedUpdateItemRequest {
        val serializedItem = itemContent.toByteArray()
        val decryptedItemKey = encryptionContextProvider.withEncryptionContext {
            EncryptionKey(decrypt(itemKey.key))
        }

        val encryptedContents = encryptionContextProvider.withEncryptionContext(decryptedItemKey) {
            encrypt(serializedItem, EncryptionTag.ItemContent)
        }

        return EncryptedUpdateItemRequest(
            keyRotation = itemKey.rotation,
            lastRevision = lastRevision,
            contentFormatVersion = CONTENT_FORMAT_VERSION,
            content = Base64.encodeBase64String(encryptedContents.array)
        )
    }

    companion object {
        const val CONTENT_FORMAT_VERSION = 1
    }
}
