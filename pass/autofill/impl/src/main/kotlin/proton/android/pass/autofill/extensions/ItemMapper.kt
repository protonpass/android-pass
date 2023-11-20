/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.autofill.extensions

import proton.android.pass.autofill.entities.AutofillItem
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.some
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ItemType
import proton.android.pass.domain.ShareId

fun ItemUiModel.toAutoFillItem(): Option<AutofillItem> = when (val content = contents) {
    is ItemContents.Login -> {
        AutofillItem.Login(
            shareId = shareId.id,
            itemId = id.id,
            username = content.username,
            password = content.password.encrypted,
            totp = content.primaryTotp.encrypted
        ).some()
    }

    is ItemContents.CreditCard -> AutofillItem.CreditCard(
        itemId = id.id,
        shareId = shareId.id,
        number = content.number,
        cardHolder = content.cardHolder,
        expiration = content.expirationDate,
        cvv = content.cvv.encrypted
    ).some()

    else -> None
}

fun Item.toAutofillItem(): Option<AutofillItem> = when (val type = itemType) {
    is ItemType.Login -> AutofillItem.Login(
        shareId = shareId.id,
        itemId = id.id,
        username = type.username,
        password = type.password,
        totp = type.primaryTotp
    ).some()

    is ItemType.CreditCard ->
        AutofillItem.CreditCard(
            itemId = id.id,
            shareId = shareId.id,
            number = type.number,
            cardHolder = type.cardHolder,
            expiration = type.expirationDate,
            cvv = type.cvv
        ).some()

    else -> None
}

data class CreatedAlias(
    val shareId: ShareId,
    val itemId: ItemId,
    val alias: String
)

fun CreatedAlias.toAutofillItem(): AutofillItem.Login = AutofillItem.Login(
    shareId = shareId.id,
    itemId = itemId.id,
    username = alias,
    password = null,
    totp = null
)
