package me.proton.pass.autofill.extensions

import me.proton.android.pass.data.api.crypto.EncryptionContext
import me.proton.pass.autofill.entities.AutofillItem
import me.proton.pass.domain.Item
import me.proton.pass.domain.ItemType
import me.proton.pass.presentation.components.model.ItemUiModel

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
