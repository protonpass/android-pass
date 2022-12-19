package me.proton.pass.presentation.extension

import me.proton.android.pass.data.api.crypto.EncryptionContext
import me.proton.pass.domain.Item
import me.proton.pass.presentation.components.model.ItemUiModel

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
