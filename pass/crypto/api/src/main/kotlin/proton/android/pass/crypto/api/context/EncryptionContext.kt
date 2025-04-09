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

package proton.android.pass.crypto.api.context

import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.EncryptedString

// This strings must be used as-is, do not modify them unless changes in other clients are
// also applied
private const val VAULT_CONTENT_TAG = "vaultcontent"
private const val ITEM_KEY_TAG = "itemkey"
private const val ITEM_CONTENT_TAG = "itemcontent"
private const val LINK_KEY_TAG = "linkkey"
private const val FILE_KEY_TAG = "filekey"
private const val FILE_CONTENTS_TAG = "filedata"
private const val FILE_METADATA_TAG = "v2;filemetadata.item.pass.proton"

private fun fileDataV2Tag(chunkIndex: Int, numChunks: Int) =
    "v2;$chunkIndex;$numChunks;filedata.item.pass.proton".encodeToByteArray()


sealed class EncryptionTag(val value: ByteArray, val name: String) {
    data object VaultContent : EncryptionTag(
        value = VAULT_CONTENT_TAG.encodeToByteArray(),
        name = "VaultContent"
    )
    data object ItemKey : EncryptionTag(
        value = ITEM_KEY_TAG.encodeToByteArray(),
        name = "ItemKey"
    )
    data object ItemContent : EncryptionTag(
        value = ITEM_CONTENT_TAG.encodeToByteArray(),
        name = "ItemContent"
    )
    data object LinkKey : EncryptionTag(
        value = LINK_KEY_TAG.encodeToByteArray(),
        name = "LinkKey"
    )
    data object FileKey : EncryptionTag(
        value = FILE_KEY_TAG.encodeToByteArray(),
        name = "FileKey"
    )
    data object FileData : EncryptionTag(
        value = FILE_CONTENTS_TAG.encodeToByteArray(),
        name = "FileData"
    )
    data object FileMetadata : EncryptionTag(
        value = FILE_METADATA_TAG.encodeToByteArray(),
        name = "FileMetadata"
    )
    class FileDataV2(chunkIndex: Int, numChunks: Int) : EncryptionTag(
        value = fileDataV2Tag(chunkIndex = chunkIndex, numChunks = numChunks),
        name = "FileDataV2"
    )
}

interface EncryptionContext {
    fun encrypt(content: String): EncryptedString
    fun encrypt(content: ByteArray, tag: EncryptionTag? = null): EncryptedByteArray

    fun decrypt(content: EncryptedString): String
    fun decrypt(content: EncryptedByteArray, tag: EncryptionTag? = null): ByteArray
}
