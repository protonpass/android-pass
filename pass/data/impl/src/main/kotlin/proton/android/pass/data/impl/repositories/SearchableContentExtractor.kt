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

package proton.android.pass.data.impl.repositories

import proton.android.pass.commonrust.api.ItemContentSerializer
import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.data.impl.db.entities.ItemEntity
import proton.android.pass.log.api.PassLogger

/**
 * Decides which decrypted item fields become plaintext in the (SQLCipher-encrypted) search
 * index. This is the data-at-rest exposure surface: only searchable identifiers and metadata
 * are returned here. Secret-bearing fields are deliberately excluded:
 *  - login password and TOTP, credit-card number/CVV/expiry, WiFi password, SSH private key
 *    are never read;
 *  - custom fields contribute their *name* but only TEXT values — HIDDEN / TOTP / TIMESTAMP
 *    values are skipped.
 *
 * The item fields are selected by [ItemContentSerializer.extractSearchableContent]. Adding a secret
 * field there leaks plaintext into the index, so the contract is locked down by
 * LibrarySearchableContentTest and SearchableContentExtractorTest.
 */
internal object SearchableContentExtractor {

    private const val TAG = "SearchableContentExtractor"

    fun extract(
        item: ItemEntity,
        context: EncryptionContext,
        itemContentSerializer: ItemContentSerializer
    ): String = runCatching {
        val decrypted = context.decrypt(item.encryptedContent)
        val parts = try {
            itemContentSerializer.extractSearchableContent(decrypted).toMutableList()
        } finally {
            decrypted.fill(0)
        }
        item.aliasEmail?.takeIf { it.isNotBlank() }?.let(parts::add)
        item.slNote?.let { context.decrypt(it) }?.takeIf { it.isNotBlank() }?.let(parts::add)
        parts.joinToString(" ")
    }.getOrElse { error ->
        PassLogger.d(TAG, "Could not extract searchable content for ${item.id}: ${error.message}")
        ""
    }
}
