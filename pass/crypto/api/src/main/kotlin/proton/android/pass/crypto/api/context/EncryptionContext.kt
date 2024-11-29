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

enum class EncryptionTag(val value: ByteArray) {
    VaultContent(VAULT_CONTENT_TAG.encodeToByteArray()),
    ItemKey(ITEM_KEY_TAG.encodeToByteArray()),
    ItemContent(ITEM_CONTENT_TAG.encodeToByteArray()),
    LinkKey(LINK_KEY_TAG.encodeToByteArray()),
    FileKey(FILE_KEY_TAG.encodeToByteArray())
}

interface EncryptionContext {
    fun encrypt(content: String): EncryptedString
    fun encrypt(content: ByteArray, tag: EncryptionTag? = null): EncryptedByteArray

    fun decrypt(content: EncryptedString): String
    fun decrypt(content: EncryptedByteArray, tag: EncryptionTag? = null): ByteArray
}
