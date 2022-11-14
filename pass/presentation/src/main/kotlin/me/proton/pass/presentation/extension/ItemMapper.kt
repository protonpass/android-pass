package me.proton.pass.presentation.extension

import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.decrypt
import me.proton.pass.domain.Item
import me.proton.pass.presentation.components.model.ItemUiModel

fun Item.toUiModel(cryptoContext: CryptoContext): ItemUiModel =
    ItemUiModel(
        id = id,
        shareId = shareId,
        name = itemName(cryptoContext),
        itemType = itemType
    )

fun Item.itemName(cryptoContext: CryptoContext): String =
    title.decrypt(cryptoContext.keyStoreCrypto)
