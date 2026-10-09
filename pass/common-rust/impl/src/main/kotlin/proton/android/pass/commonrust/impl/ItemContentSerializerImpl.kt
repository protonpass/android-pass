/*
 * Copyright (c) 2026 Proton AG
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

package proton.android.pass.commonrust.impl

import proton.android.pass.commonrust.ProtonPassSerialization
import proton.android.pass.commonrust.api.ItemContentSerializer
import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemType
import proton.android.pass.domain.ParsedItem
import proton.android.pass.domain.Passkey
import proton.android.pass.domain.VaultProperties
import proton.android.pass.domain.attachments.AttachmentContent
import proton.android.pass.domain.entity.PackageInfo
import proton.android.pass.log.api.PassLogger
import proton.android.pass.types.FolderData as LibraryFolderData
import proton.android.pass.types.ItemAttachmentContent as LibraryItemAttachmentContent
import java.util.UUID
import javax.inject.Inject

class ItemContentSerializerImpl @Inject constructor() : ItemContentSerializer {

    private val serialization by lazy { ProtonPassSerialization() }

    override fun serializeItem(
        contents: ItemContents,
        encryptionContext: EncryptionContext,
        itemUuid: String
    ): ByteArray = serialization.itemDataSerialize(
        contents.toLibraryItemData(itemUuid = itemUuid, existing = null, encryptionContext = encryptionContext)
    )

    override fun deserializeItem(
        bytes: ByteArray,
        encryptionContext: EncryptionContext,
        aliasEmail: String?
    ): ParsedItem = deserializeItems(listOf(bytes), encryptionContext, listOf(aliasEmail)).single()

    override fun deserializeItems(
        bytes: List<ByteArray>,
        encryptionContext: EncryptionContext,
        aliasEmails: List<String?>
    ): List<ParsedItem> {
        require(bytes.size == aliasEmails.size) { "bytes and aliasEmails must have the same size" }
        val result = serialization.itemDataDeserializeMany(bytes)
        return alignBatch(bytes.size, result.successes, result.errors.map { it.index.toInt() })
            .mapIndexed { index, data ->
                data
                    ?.let { runCatching { it.toParsedItem(encryptionContext, aliasEmails[index]) }.getOrNull() }
                    ?: unknownParsedItem().also { PassLogger.w(TAG, "Item at index $index could not be loaded") }
            }
    }

    override fun extractSearchableContent(bytes: ByteArray): List<String> =
        serialization.itemDataDeserialize(bytes).searchableContent()

    override fun performItemUpdate(
        original: ByteArray,
        contents: ItemContents,
        encryptionContext: EncryptionContext
    ): ByteArray {
        val originalData = serialization.itemDataDeserialize(original)
        val updated = contents.toLibraryItemData(
            itemUuid = originalData.itemUuid,
            existing = originalData,
            encryptionContext = encryptionContext
        )
        return serialization.itemDataPerformUpdate(original, updated)
    }

    override fun addPackageInfoToItem(original: ByteArray, packageInfo: PackageInfo): ByteArray? =
        serialization.itemDataDeserialize(original)
            .withPackageInfoIfChanged(packageInfo)
            ?.let { serialization.itemDataPerformUpdate(original, it) }

    override fun addUrlToItem(
        original: ByteArray,
        url: String,
        isAutofillUrlAdvancedModesEnabled: Boolean
    ): ByteArray {
        val updated = serialization.itemDataDeserialize(original).withUrl(url, isAutofillUrlAdvancedModesEnabled)
        return serialization.itemDataPerformUpdate(original, updated)
    }

    override fun addPasskeyToItem(original: ByteArray, passkey: Passkey): ByteArray {
        val updated = serialization.itemDataDeserialize(original).withPasskey(passkey)
        return serialization.itemDataPerformUpdate(original, updated)
    }

    override fun serializeVault(vault: VaultProperties): ByteArray =
        serialization.vaultDataSerialize(vault.toLibraryVaultData())

    override fun deserializeVault(bytes: ByteArray): VaultProperties =
        serialization.vaultDataDeserialize(bytes).toVaultProperties()

    override fun performVaultUpdate(original: ByteArray, updated: VaultProperties): ByteArray {
        val originalData = serialization.vaultDataDeserialize(original)
        return serialization.vaultDataPerformUpdate(original, updated.toLibraryVaultData(originalData))
    }

    override fun serializeFolder(name: String): ByteArray =
        serialization.folderDataSerialize(LibraryFolderData(name = name))

    override fun deserializeFolder(bytes: ByteArray): String = serialization.folderDataDeserialize(bytes).name

    override fun deserializeFolders(bytes: List<ByteArray>): List<String?> {
        val result = serialization.folderDataDeserializeMany(bytes)
        return alignBatch(bytes.size, result.successes, result.errors.map { it.index.toInt() }).map { it?.name }
    }

    override fun performFolderUpdate(original: ByteArray, name: String): ByteArray =
        serialization.folderDataPerformUpdate(original, LibraryFolderData(name = name))

    override fun serializeAttachmentContent(content: AttachmentContent): ByteArray =
        serialization.itemAttachmentContentSerialize(
            LibraryItemAttachmentContent(name = content.name, mimeType = content.mimeType)
        )

    override fun deserializeAttachmentContent(bytes: ByteArray): AttachmentContent =
        serialization.itemAttachmentContentDeserialize(bytes).let {
            AttachmentContent(name = it.name, mimeType = it.mimeType)
        }

    override fun updateAttachmentName(original: ByteArray, name: String): ByteArray =
        serialization.itemAttachmentContentSerialize(
            serialization.itemAttachmentContentDeserialize(original).copy(name = name)
        )
}

private fun <T> alignBatch(
    size: Int,
    successes: List<T>,
    failedIndexes: List<Int>
): List<T?> {
    val failed = failedIndexes.toSet()
    val remaining = successes.iterator()
    return List(size) { index -> if (index in failed) null else remaining.next() }
}

private fun unknownParsedItem() = ParsedItem(
    itemUuid = UUID.randomUUID().toString(),
    title = "",
    note = "",
    itemType = ItemType.Unknown,
    packageInfoSet = emptySet()
)

private const val TAG = "ItemContentSerializer"
