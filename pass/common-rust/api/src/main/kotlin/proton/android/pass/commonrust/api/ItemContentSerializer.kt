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

package proton.android.pass.commonrust.api

import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ParsedItem
import proton.android.pass.domain.Passkey
import proton.android.pass.domain.VaultProperties
import proton.android.pass.domain.attachments.AttachmentContent
import proton.android.pass.domain.entity.PackageInfo
import java.util.UUID

interface ItemContentSerializer {

    fun serializeItem(
        contents: ItemContents,
        encryptionContext: EncryptionContext,
        itemUuid: String = UUID.randomUUID().toString()
    ): ByteArray

    fun deserializeItem(
        bytes: ByteArray,
        encryptionContext: EncryptionContext,
        aliasEmail: String? = null
    ): ParsedItem

    fun deserializeItems(
        bytes: List<ByteArray>,
        encryptionContext: EncryptionContext,
        aliasEmails: List<String?>
    ): List<ParsedItem>

    fun extractSearchableContent(bytes: ByteArray): List<String>

    fun performItemUpdate(
        original: ByteArray,
        contents: ItemContents,
        encryptionContext: EncryptionContext
    ): ByteArray

    fun addPackageInfoToItem(original: ByteArray, packageInfo: PackageInfo): ByteArray?
    fun addUrlToItem(original: ByteArray, url: String): ByteArray
    fun addPasskeyToItem(original: ByteArray, passkey: Passkey): ByteArray

    fun serializeVault(vault: VaultProperties): ByteArray
    fun deserializeVault(bytes: ByteArray): VaultProperties
    fun performVaultUpdate(original: ByteArray, updated: VaultProperties): ByteArray

    fun serializeFolder(name: String): ByteArray
    fun deserializeFolder(bytes: ByteArray): String
    fun deserializeFolders(bytes: List<ByteArray>): List<String?>
    fun performFolderUpdate(original: ByteArray, name: String): ByteArray

    fun serializeAttachmentContent(content: AttachmentContent): ByteArray
    fun deserializeAttachmentContent(bytes: ByteArray): AttachmentContent
    fun updateAttachmentName(original: ByteArray, name: String): ByteArray
}
