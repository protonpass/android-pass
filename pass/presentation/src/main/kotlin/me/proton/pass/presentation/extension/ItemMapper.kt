package me.proton.pass.presentation.extension

import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.common.keystore.decrypt
import me.proton.pass.domain.Item
import me.proton.pass.presentation.components.model.ItemUiModel

fun Item.toUiModel(keyStoreCrypto: KeyStoreCrypto): ItemUiModel =
    ItemUiModel(
        id = id,
        shareId = shareId,
        name = itemName(keyStoreCrypto),
        itemType = itemType
    )

fun Item.itemName(keyStoreCrypto: KeyStoreCrypto): String =
    title.decrypt(keyStoreCrypto)
