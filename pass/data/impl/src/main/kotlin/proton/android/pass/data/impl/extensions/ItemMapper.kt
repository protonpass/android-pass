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

package proton.android.pass.data.impl.extensions

import kotlinx.datetime.Instant
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.toOption
import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.data.impl.db.entities.ItemEntity
import proton.android.pass.datamodels.api.fromParsed
import proton.android.pass.domain.Flags
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemEncrypted
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ItemType
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareType
import proton.android.pass.domain.entity.AppName
import proton.android.pass.domain.entity.PackageInfo
import proton.android.pass.domain.entity.PackageName
import proton_pass_item_v1.ItemV1

fun ItemEntity.itemType(context: EncryptionContext): ItemType {
    val decrypted = context.decrypt(encryptedContent)
    val parsed = ItemV1.Item.parseFrom(decrypted)
    return ItemType.fromParsed(context, parsed = parsed, aliasEmail = this.aliasEmail)
}

fun ItemEntity.allowedApps(context: EncryptionContext): Set<PackageInfo> {
    val decrypted = context.decrypt(encryptedContent)
    val parsed = ItemV1.Item.parseFrom(decrypted)
    return parsed.platformSpecific.android.allowedAppsList.map {
        PackageInfo(PackageName(it.packageName), AppName(it.appName))
    }.toSet()
}

fun ItemEntity.toDomain(context: EncryptionContext): Item {
    val decrypted = context.decrypt(encryptedContent)
    val parsed = ItemV1.Item.parseFrom(decrypted)

    return Item(
        id = ItemId(id),
        userId = UserId(userId),
        itemUuid = parsed.metadata.itemUuid,
        revision = revision,
        shareId = ShareId(shareId),
        itemType = ItemType.fromParsed(context, parsed, aliasEmail),
        title = encryptedTitle,
        note = encryptedNote,
        content = encryptedContent,
        state = state,
        packageInfoSet = allowedApps(context),
        modificationTime = Instant.fromEpochSeconds(modifyTime),
        createTime = Instant.fromEpochSeconds(createTime),
        lastAutofillTime = lastUsedTime.toOption().map(Instant::fromEpochSeconds),
        isPinned = isPinned,
        flags = Flags(flags),
        shareCount = shareCount,
        shareType = if (encryptedKey != null) ShareType.Vault else ShareType.Item
    )
}

fun ItemEntity.toEncryptedDomain(): ItemEncrypted = ItemEncrypted(
    id = ItemId(id),
    userId = UserId(userId),
    revision = revision,
    shareId = ShareId(shareId),
    title = encryptedTitle,
    note = encryptedNote,
    content = encryptedContent,
    state = state,
    aliasEmail = aliasEmail,
    createTime = Instant.fromEpochSeconds(createTime),
    modificationTime = Instant.fromEpochSeconds(modifyTime),
    lastAutofillTime = lastUsedTime?.let { Some(Instant.fromEpochSeconds(it)) } ?: None,
    isPinned = isPinned,
    flags = Flags(flags),
    shareCount = shareCount,
    shareType = if (encryptedKey != null) ShareType.Vault else ShareType.Item
)
