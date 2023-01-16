package proton.android.pass.commonui.api

import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.crypto.api.context.EncryptionContext
import proton.pass.domain.Item

fun Item.toUiModel(context: EncryptionContext): ItemUiModel =
    ItemUiModel(
        id = id,
        shareId = shareId,
        name = context.decrypt(title),
        note = context.decrypt(note),
        itemType = itemType,
        modificationTime = modificationTime
    )

fun Item.itemName(context: EncryptionContext): String =
    context.decrypt(title)
