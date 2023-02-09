package proton.android.pass.autofill.extensions

import proton.android.pass.autofill.entities.AutofillItem
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.toOption
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.pass.domain.Item
import proton.pass.domain.ItemType

fun ItemUiModel.toAutoFillItem(): Option<AutofillItem> =
    if (itemType is ItemType.Login) {
        val asLogin = itemType as ItemType.Login
        AutofillItem(
            shareId = shareId.id,
            itemId = id.id,
            username = asLogin.username,
            password = asLogin.password,
            totp = asLogin.primaryTotp
        ).toOption()
    } else {
        None
    }

fun Item.toAutofillItem(): Option<AutofillItem> =
    if (itemType is ItemType.Login) {
        val asLogin = itemType as ItemType.Login
        AutofillItem(
            shareId = shareId.id,
            itemId = id.id,
            username = asLogin.username,
            password = asLogin.password,
            totp = asLogin.primaryTotp
        ).toOption()
    } else {
        None
    }
