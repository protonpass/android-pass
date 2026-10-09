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

package proton.android.pass.commonui.api

import proton.android.pass.commonrust.api.ItemContentSerializer
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemEncrypted
import proton.android.pass.domain.toItemContents

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
    folderId = folderId,
    category = itemType.category,
    revision = revision,
    shareCount = shareCount,
    shareType = shareType
)

fun ItemEncrypted.toUiModel(context: EncryptionContext, itemContentSerializer: ItemContentSerializer): ItemUiModel {
    val decryptedContent = context.decrypt(content)
    val itemType = itemContentSerializer.deserializeItem(decryptedContent, context, aliasEmail).itemType
    return ItemUiModel(
        id = id,
        shareId = shareId,
        userId = userId,
        contents = toItemContents(
            decrypt = { context.decrypt(it) },
            itemType = itemType,
            title = title,
            note = note,
            itemFlags = itemFlags,
            slNote = slNote
        ),
        state = state,
        createTime = createTime,
        modificationTime = modificationTime,
        lastAutofillTime = lastAutofillTime.value(),
        isPinned = isPinned,
        pinTime = pinTime.value(),
        folderId = folderId,
        category = itemType.category,
        revision = revision,
        shareCount = shareCount,
        shareType = shareType
    )
}

fun Item.itemName(context: EncryptionContext): String = context.decrypt(title)
