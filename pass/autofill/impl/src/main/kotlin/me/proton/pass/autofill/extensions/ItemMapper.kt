package me.proton.pass.autofill.extensions

import me.proton.android.pass.commonuimodels.api.ItemUiModel
import me.proton.android.pass.crypto.api.context.EncryptionContext
import me.proton.pass.autofill.entities.AutofillItem
import me.proton.pass.domain.Item
import me.proton.pass.domain.ItemType

fun ItemUiModel.toAutoFillItem(encryptionContext: EncryptionContext): AutofillItem =
    if (itemType is ItemType.Login) {
        val asLogin = itemType as ItemType.Login
        AutofillItem.Login(
            username = asLogin.username,
            password = encryptionContext.decrypt(asLogin.password)
        )
    } else {
        AutofillItem.Unknown
    }

fun Item.toAutofillItem(encryptionContext: EncryptionContext): AutofillItem =
    if (itemType is ItemType.Login) {
        val asLogin = itemType as ItemType.Login
        AutofillItem.Login(
            username = asLogin.username,
            password = encryptionContext.decrypt(asLogin.password)
        )
    } else {
        AutofillItem.Unknown
    }
