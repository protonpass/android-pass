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

package proton.android.pass.datamodels.api

import proton.android.pass.crypto.api.context.EncryptionContext
import proton.pass.domain.CreditCardType
import proton.pass.domain.CustomField
import proton.pass.domain.ItemType
import proton.pass.domain.entity.AppName
import proton.pass.domain.entity.PackageInfo
import proton.pass.domain.entity.PackageName
import proton_pass_item_v1.ItemV1

fun ItemType.Companion.fromParsed(
    context: EncryptionContext,
    parsed: ItemV1.Item,
    aliasEmail: String? = null
): ItemType {
    return when (parsed.content.contentCase) {
        ItemV1.Content.ContentCase.LOGIN -> ItemType.Login(
            username = parsed.content.login.username,
            password = context.encrypt(parsed.content.login.password),
            websites = parsed.content.login.urlsList,
            packageInfoSet = parsed.platformSpecific.android.allowedAppsList.map {
                PackageInfo(PackageName(it.packageName), AppName(it.appName))
            }.toSet(),
            primaryTotp = context.encrypt(parsed.content.login.totpUri),
            customFields = parsed.extraFieldsList.map { field ->
                field.toDomain(context)
            }
        )
        ItemV1.Content.ContentCase.NOTE -> ItemType.Note(parsed.metadata.note)
        ItemV1.Content.ContentCase.ALIAS -> {
            requireNotNull(aliasEmail)
            ItemType.Alias(aliasEmail = aliasEmail)
        }
        ItemV1.Content.ContentCase.CREDIT_CARD -> {
            val content = parsed.content.creditCard
            ItemType.CreditCard(
                cardHolder = content.cardholderName,
                number = context.encrypt(content.number),
                cvv = context.encrypt(content.cvv),
                pin = context.encrypt(content.pin),
                creditCardType = content.cardType.toDomain(),
                expirationDate = content.expirationDate
            )
        }
        else -> ItemType.Unknown

    }
}

fun ItemV1.ExtraField.toDomain(context: EncryptionContext): CustomField {
    return when (this.contentCase) {
        ItemV1.ExtraField.ContentCase.TEXT -> CustomField.Text(
            label = this.fieldName,
            value = this.text.content
        )
        ItemV1.ExtraField.ContentCase.HIDDEN -> CustomField.Hidden(
            label = this.fieldName,
            value = context.encrypt(this.hidden.content)
        )
        ItemV1.ExtraField.ContentCase.TOTP -> CustomField.Totp(
            label = this.fieldName,
            value = context.encrypt(this.totp.totpUri)
        )
        else -> CustomField.Unknown
    }
}

fun ItemV1.CardType.toDomain(): CreditCardType = when (this) {
    ItemV1.CardType.Visa -> CreditCardType.Visa
    ItemV1.CardType.Mastercard -> CreditCardType.MasterCard
    ItemV1.CardType.AmericanExpress -> CreditCardType.AmericanExpress

    ItemV1.CardType.Other,
    ItemV1.CardType.Unspecified,
    ItemV1.CardType.UNRECOGNIZED -> CreditCardType.Other
}
