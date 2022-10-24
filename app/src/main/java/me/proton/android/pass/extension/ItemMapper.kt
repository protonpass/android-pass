package me.proton.android.pass.extension

import me.proton.core.crypto.common.context.CryptoContext
import me.proton.pass.data.extensions.itemName
import me.proton.pass.domain.Item
import me.proton.pass.presentation.components.model.ItemUiModel

fun Item.toUiModel(cryptoContext: CryptoContext): ItemUiModel =
    ItemUiModel(
        id = id,
        shareId = shareId,
        name = itemName(cryptoContext),
        itemType = itemType
    )
