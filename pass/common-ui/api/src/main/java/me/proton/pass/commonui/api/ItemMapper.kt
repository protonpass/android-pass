package me.proton.pass.commonui.api

import me.proton.android.pass.commonuimodels.api.ItemUiModel
import me.proton.android.pass.crypto.api.context.EncryptionContext
import me.proton.pass.domain.Item

fun Item.toUiModel(context: EncryptionContext): ItemUiModel =
    ItemUiModel(
        id = id,
        shareId = shareId,
        name = context.decrypt(title),
        note = context.decrypt(note),
        itemType = itemType
    )

fun Item.itemName(context: EncryptionContext): String =
    context.decrypt(title)
