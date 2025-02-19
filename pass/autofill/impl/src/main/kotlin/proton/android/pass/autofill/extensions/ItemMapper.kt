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
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId

fun ItemUiModel.toAutoFillItem(shouldLinkPackageName: Boolean = false): AutofillItem = when (val content = contents) {
    is ItemContents.Login -> AutofillItem.Login(
        shareId = shareId.id,
        itemId = id.id,
        username = content.displayValue,
        password = content.password.encrypted,
        totp = content.primaryTotp.encrypted,
        shouldLinkPackageName = shouldLinkPackageName,
        email = content.itemEmail
    )

    is ItemContents.CreditCard -> AutofillItem.CreditCard(
        itemId = id.id,
        shareId = shareId.id,
        number = content.number,
        cardHolder = content.cardHolder,
        expiration = content.expirationDate,
        cvv = content.cvv.encrypted
    )

    is ItemContents.Identity -> AutofillItem.Identity(
        shareId = shareId.id,
        itemId = id.id,
        fullName = content.personalDetailsContent.fullName,
        firstName = content.personalDetailsContent.firstName,
        middleName = content.personalDetailsContent.middleName,
        lastName = content.personalDetailsContent.lastName,
        address = content.addressDetailsContent.streetAddress,
        city = content.addressDetailsContent.city,
        postalCode = content.addressDetailsContent.zipOrPostalCode,
        phoneNumber = content.personalDetailsContent.phoneNumber,
        organization = content.addressDetailsContent.organization,
        country = content.addressDetailsContent.countryOrRegion
    )

    is ItemContents.Alias -> AutofillItem.Login(
        shareId = shareId.id,
        itemId = id.id,
        username = content.aliasEmail,
        password = null,
        totp = null,
        shouldLinkPackageName = false
    )
    is ItemContents.Note,
    is ItemContents.Custom,
    is ItemContents.Unknown -> throw IllegalStateException("Unsupported item type")
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
    totp = null,
    shouldLinkPackageName = false
)
