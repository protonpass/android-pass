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

package proton.android.pass.commonui.api

import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.datamodels.api.fromParsed
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemEncrypted
import proton.android.pass.domain.ItemType
import proton.android.pass.domain.toItemContents
import proton_pass_item_v1.ItemV1

fun Item.toUiModel(context: EncryptionContext): ItemUiModel = ItemUiModel(
    id = id,
    shareId = shareId,
    userId = userId,
    contents = toItemContents { context.decrypt(it) },
    state = state,
    createTime = createTime,
    modificationTime = modificationTime,
    lastAutofillTime = lastAutofillTime.value(),
    isPinned = isPinned,
    pinTime = pinTime.value(),
    category = itemType.category,
    revision = revision,
    shareCount = shareCount,
    shareType = shareType
)

fun ItemEncrypted.toUiModel(context: EncryptionContext): ItemUiModel {
    val decryptedContent = context.decrypt(content)
    val parsed = ItemV1.Item.parseFrom(decryptedContent)
    val itemType = ItemType.fromParsed(context, parsed, aliasEmail)
    return ItemUiModel(
        id = id,
        shareId = shareId,
        userId = userId,
        contents = toItemContents(
            decrypt = { context.decrypt(it) },
            itemType = itemType,
            title = title,
            note = note,
            itemFlags = itemFlags
        ),
        state = state,
        createTime = createTime,
        modificationTime = modificationTime,
        lastAutofillTime = lastAutofillTime.value(),
        isPinned = isPinned,
        pinTime = pinTime.value(),
        category = itemType.category,
        revision = revision,
        shareCount = shareCount,
        shareType = shareType
    )
}

fun Item.itemName(context: EncryptionContext): String = context.decrypt(title)
