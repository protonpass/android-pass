package proton.android.pass.commonui.api

import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.toOption
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.crypto.api.context.EncryptionContext
import proton.pass.domain.Item
import proton.pass.domain.ItemType

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

fun Item.loginUsername(): Option<String> = when (val type = itemType) {
    is ItemType.Login -> type.username.toOption()
    else -> None
}
