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
import proton.android.pass.crypto.api.usecases.EncryptedMigrateContent
import proton.android.pass.crypto.api.usecases.EncryptedMigrateItemBody
import proton.android.pass.crypto.api.usecases.EncryptedMigrateItemHistory
import proton.android.pass.crypto.api.usecases.ItemMigrationContent
import proton.android.pass.crypto.api.usecases.ItemMigrationHistoryContent
import proton.android.pass.crypto.api.usecases.ItemMigrationPayload
import proton.android.pass.crypto.api.usecases.MigrateItem
import proton.android.pass.domain.key.ShareKey
import javax.inject.Inject

class MigrateItemImpl @Inject constructor(
    private val encryptionContextProvider: EncryptionContextProvider
) : MigrateItem {

    override fun migrate(destinationKey: ShareKey, payload: ItemMigrationPayload): EncryptedMigrateItemBody =
        with(payload) {
            val decryptedDestinationKey = encryptionContextProvider.withEncryptionContext {
                EncryptionKey(decrypt(destinationKey.key))
            }

            val itemKey = EncryptionKey.generate()

            val encryptedItemKey =
                encryptionContextProvider.withEncryptionContext(decryptedDestinationKey) {
                    encrypt(itemKey.value(), EncryptionTag.ItemKey)
                }

            val item = createEncryptedMigratedItemContent(
                itemKey = itemKey.clone(),
                itemContent = itemContent,
                encryptedItemKey = encryptedItemKey,
                destinationKeyRotation = destinationKey.rotation
            )

            val history = createEncryptedMigratedHistoryContent(
                itemKey = itemKey.clone(),
                itemHistoryContents = historyContents,
                encryptedItemKey = encryptedItemKey,
                destinationKeyRotation = destinationKey.rotation
            )

            itemKey.clear()

            EncryptedMigrateItemBody(
                item = item,
                history = history
            )
        }

    private fun createEncryptedMigratedItemContent(
        itemKey: EncryptionKey,
        itemContent: ItemMigrationContent,
        encryptedItemKey: EncryptedByteArray,
        destinationKeyRotation: Long
    ): EncryptedMigrateContent = with(itemContent) {
        val decryptedContents = encryptionContextProvider.withEncryptionContext {
            decrypt(encryptedItemContents)
        }

        val reEncryptedContents =
            encryptionContextProvider.withEncryptionContext(itemKey) {
                encrypt(decryptedContents, EncryptionTag.ItemContent)
            }

        EncryptedMigrateContent(
            keyRotation = destinationKeyRotation,
            contentFormatVersion = contentFormatVersion,
            content = Base64.encodeBase64String(reEncryptedContents.array),
            itemKey = Base64.encodeBase64String(encryptedItemKey.array)
        )
    }

    private fun createEncryptedMigratedHistoryContent(
        itemKey: EncryptionKey,
        itemHistoryContents: List<ItemMigrationHistoryContent>,
        encryptedItemKey: EncryptedByteArray,
        destinationKeyRotation: Long
    ): List<EncryptedMigrateItemHistory> {
        if (itemHistoryContents.size <= 1) return emptyList()

        return itemHistoryContents.map { itemHistoryContent ->
            EncryptedMigrateItemHistory(
                revision = itemHistoryContent.revision,
                content = createEncryptedMigratedItemContent(
                    itemKey = itemKey.clone(),
                    itemContent = itemHistoryContent.itemContent,
                    encryptedItemKey = encryptedItemKey,
                    destinationKeyRotation = destinationKeyRotation
                )
            )
        }
    }

}
