package me.proton.core.pass.autofill.extensions

import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.common.keystore.decrypt
import me.proton.core.pass.autofill.entities.AutofillItem
import me.proton.core.pass.data.extensions.itemName
import me.proton.core.pass.domain.Item
import me.proton.core.pass.domain.ItemType
import me.proton.core.pass.presentation.components.model.ItemUiModel

fun Item.toUiModel(cryptoContext: CryptoContext): ItemUiModel =
    ItemUiModel(
        id = id,
        shareId = shareId,
        name = itemName(cryptoContext),
        itemType = itemType
    )

fun ItemUiModel.toAutoFillItem(crypto: KeyStoreCrypto): AutofillItem {
    if (itemType is ItemType.Login) {
        val asLogin = itemType as ItemType.Login
        return AutofillItem.Login(
            username = asLogin.username,
            password = asLogin.password.decrypt(crypto)
        )
    } else {
        return AutofillItem.Unknown
    }
}
