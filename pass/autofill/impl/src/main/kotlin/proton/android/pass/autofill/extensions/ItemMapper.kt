package proton.android.pass.autofill.extensions

import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.autofill.entities.AutofillItem
import proton.pass.domain.Item
import proton.pass.domain.ItemType

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
