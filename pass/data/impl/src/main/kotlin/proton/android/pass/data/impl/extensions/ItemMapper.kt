/*
 * Copyright (c) 2023-2026 Proton AG
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

package proton.android.pass.data.impl.extensions

import kotlinx.datetime.Instant
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.toOption
import proton.android.pass.commonrust.api.ItemContentSerializer
import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.data.impl.db.entities.ItemEntity
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemEncrypted
import proton.android.pass.domain.ItemFlags
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ParsedItem
import proton.android.pass.domain.FolderId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareType

fun ItemEntity.toDomain(context: EncryptionContext, itemContentSerializer: ItemContentSerializer): Item {
    val decrypted = context.decrypt(encryptedContent)
    val parsed = itemContentSerializer.deserializeItem(decrypted, context, aliasEmail)
    return toDomain(parsed)
}

fun List<ItemEntity>.toDomain(context: EncryptionContext, itemContentSerializer: ItemContentSerializer): List<Item> {
    if (isEmpty()) return emptyList()
    val decrypted = map { context.decrypt(it.encryptedContent) }
    try {
        val parsed = itemContentSerializer.deserializeItems(decrypted, context, map { it.aliasEmail })
        return zip(parsed) { entity, item -> entity.toDomain(item) }
    } finally {
        decrypted.forEach { it.fill(0) }
    }
}

private fun ItemEntity.toDomain(parsed: ParsedItem): Item = Item(
    id = ItemId(id),
    userId = UserId(userId),
    itemUuid = parsed.itemUuid,
    revision = revision,
    shareId = ShareId(shareId),
    folderId = folderId?.let(::FolderId),
    itemType = parsed.itemType,
    title = encryptedTitle,
    note = encryptedNote,
    content = encryptedContent,
    state = state,
    packageInfoSet = parsed.packageInfoSet,
    modificationTime = Instant.fromEpochSeconds(modifyTime),
    createTime = Instant.fromEpochSeconds(createTime),
    lastAutofillTime = lastUsedTime.toOption().map(Instant::fromEpochSeconds),
    isPinned = isPinned,
    pinTime = pinTime.toOption().map(Instant::fromEpochSeconds),
    itemFlags = ItemFlags(flags),
    shareCount = shareCount,
    contentFormatVersion = contentFormatVersion,
    shareType = if (encryptedKey != null) ShareType.Vault else ShareType.Item,
    slNote = slNote
)

fun ItemEntity.toEncryptedDomain(): ItemEncrypted = ItemEncrypted(
    id = ItemId(id),
    userId = UserId(userId),
    revision = revision,
    shareId = ShareId(shareId),
    folderId = folderId?.let(::FolderId),
    title = encryptedTitle,
    note = encryptedNote,
    content = encryptedContent,
    state = state,
    aliasEmail = aliasEmail,
    createTime = Instant.fromEpochSeconds(createTime),
    modificationTime = Instant.fromEpochSeconds(modifyTime),
    lastAutofillTime = lastUsedTime?.let { Some(Instant.fromEpochSeconds(it)) } ?: None,
    isPinned = isPinned,
    pinTime = pinTime?.let { Some(Instant.fromEpochSeconds(it)) } ?: None,
    itemFlags = ItemFlags(flags),
    shareCount = shareCount,
    shareType = if (encryptedKey != null) ShareType.Vault else ShareType.Item,
    slNote = slNote
)
