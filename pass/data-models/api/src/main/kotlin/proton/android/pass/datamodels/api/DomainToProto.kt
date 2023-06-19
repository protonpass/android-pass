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
import proton.pass.domain.CustomFieldContent
import proton.pass.domain.ItemContents
import proton_pass_item_v1.ItemV1
import proton_pass_item_v1.extraField
import proton_pass_item_v1.extraHiddenField
import proton_pass_item_v1.extraTextField
import proton_pass_item_v1.extraTotp
import java.util.UUID

@Suppress("LongMethod", "ComplexMethod")
fun ItemContents.serializeToProto(
    itemUuid: String? = null,
    encryptionContext: EncryptionContext
): ItemV1.Item {
    val uuid = itemUuid ?: UUID.randomUUID().toString()
    val builder = ItemV1.Item.newBuilder()
        .setMetadata(
            ItemV1.Metadata.newBuilder()
                .setName(title)
                .setNote(note)
                .setItemUuid(uuid)
                .build()
        )
    val contentBuilder = ItemV1.Content.newBuilder()
    val content = when (this) {
        is ItemContents.Login -> {
            if (packageInfoSet.isNotEmpty()) {
                val packageNameList = packageInfoSet.map {
                    ItemV1.AllowedAndroidApp.newBuilder()
                        .setPackageName(it.packageName.value)
                        .setAppName(it.appName.value)
                        .build()
                }
                builder.platformSpecific = ItemV1.PlatformSpecific.newBuilder()
                    .setAndroid(
                        ItemV1.AndroidSpecific.newBuilder()
                            .addAllAllowedApps(packageNameList)
                            .build()
                    )
                    .build()
            }

            for (customField in customFields) {
                when (customField) {
                    is CustomFieldContent.Text -> {
                        builder.addExtraFields(
                            extraField {
                                fieldName = customField.label
                                text = extraTextField {
                                    content = customField.value
                                }
                            }
                        )
                    }

                    is CustomFieldContent.Hidden -> {
                        builder.addExtraFields(
                            extraField {
                                fieldName = customField.label
                                hidden = extraHiddenField {
                                    content = encryptionContext.decrypt(customField.value.encrypted)
                                }
                            }
                        )
                    }

                    is CustomFieldContent.Totp -> {
                        builder.addExtraFields(
                            extraField {
                                fieldName = customField.label
                                totp = extraTotp {
                                    totpUri = encryptionContext.decrypt(customField.value.encrypted)
                                }
                            }
                        )
                    }
                }
            }

            val itemBuilder = ItemV1.ItemLogin.newBuilder()
            itemBuilder.username = username
            itemBuilder.password = encryptionContext.decrypt(password.encrypted)
            itemBuilder.addAllUrls(urls.filter { it.isNotBlank() })
            itemBuilder.totpUri = encryptionContext.decrypt(primaryTotp.encrypted)
            contentBuilder.setLogin(itemBuilder.build())
        }

        is ItemContents.Note -> contentBuilder.setNote(
            ItemV1.ItemNote.newBuilder().build()
        )

        is ItemContents.Alias -> contentBuilder.setAlias(
            ItemV1.ItemAlias.newBuilder().build()
        )

        is ItemContents.CreditCard -> {
            val itemBuilder = ItemV1.ItemCreditCard.newBuilder()
            itemBuilder.cardholderName = cardHolder
            itemBuilder.number = number
            itemBuilder.cardType = when (type) {
                CreditCardType.Other -> ItemV1.CardType.Other
                CreditCardType.Visa -> ItemV1.CardType.Visa
                CreditCardType.MasterCard -> ItemV1.CardType.Mastercard
                CreditCardType.AmericanExpress -> ItemV1.CardType.AmericanExpress
            }
            itemBuilder.cvv = encryptionContext.decrypt(cvv.encrypted)
            itemBuilder.pin = encryptionContext.decrypt(pin.encrypted)
            itemBuilder.expirationDate = expirationDate
            contentBuilder.setCreditCard(itemBuilder.build())
        }

        is ItemContents.Unknown -> throw IllegalStateException("Cannot be unknown")
    }.build()

    return builder
        .setContent(content)
        .build()
}

