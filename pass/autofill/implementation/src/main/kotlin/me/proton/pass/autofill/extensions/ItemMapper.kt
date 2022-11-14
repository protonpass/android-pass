package me.proton.pass.autofill.extensions

import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.common.keystore.decrypt
import me.proton.pass.autofill.entities.AutofillItem
import me.proton.pass.domain.Item
import me.proton.pass.domain.ItemType
import me.proton.pass.presentation.components.model.ItemUiModel

fun ItemUiModel.toAutoFillItem(crypto: KeyStoreCrypto): AutofillItem =
    if (itemType is ItemType.Login) {
        val asLogin = itemType as ItemType.Login
        AutofillItem.Login(
            username = asLogin.username,
            password = asLogin.password.decrypt(crypto)
        )
    } else {
        AutofillItem.Unknown
    }

fun Item.toAutofillItem(crypto: KeyStoreCrypto): AutofillItem =
    if (itemType is ItemType.Login) {
        val asLogin = itemType as ItemType.Login
        AutofillItem.Login(
            username = asLogin.username,
            password = asLogin.password.decrypt(crypto)
        )
    } else {
        AutofillItem.Unknown
    }
