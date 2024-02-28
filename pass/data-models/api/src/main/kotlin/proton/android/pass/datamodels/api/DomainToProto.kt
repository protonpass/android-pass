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

import com.google.protobuf.ByteString
import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.domain.CreditCardType
import proton.android.pass.domain.CustomFieldContent
import proton.android.pass.domain.ItemContents
import proton_pass_item_v1.ItemV1
import proton_pass_item_v1.extraField
import proton_pass_item_v1.extraHiddenField
import proton_pass_item_v1.extraTextField
import proton_pass_item_v1.extraTotp
import java.util.UUID

@Suppress("LongMethod", "ComplexMethod")
fun ItemContents.serializeToProto(
    itemUuid: String = UUID.randomUUID().toString(),
    builder: ItemV1.Item.Builder = ItemV1.Item.newBuilder(),
    encryptionContext: EncryptionContext
): ItemV1.Item {
    builder.setMetadata(
        builder.metadata.toBuilder()
            .setName(title)
            .setNote(note)
            .setItemUuid(itemUuid)
            .build()
    )

    val content = when (this) {
        is ItemContents.Login -> {
            val packageNameList = packageInfoSet.map {
                ItemV1.AllowedAndroidApp.newBuilder()
                    .setPackageName(it.packageName.value)
                    .setAppName(it.appName.value)
                    .build()
            }
            builder.platformSpecific = builder.platformSpecific.toBuilder()
                .setAndroid(
                    ItemV1.AndroidSpecific.newBuilder()
                        .clearAllowedApps()
                        .addAllAllowedApps(packageNameList)
                        .build()
                )
                .build()

            builder.clearExtraFields()
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

            builder.content.toBuilder().setLogin(
                builder.content.login.toBuilder()
                    .setUsername(username)
                    .setPassword(encryptionContext.decrypt(password.encrypted))
                    .setTotpUri(encryptionContext.decrypt(primaryTotp.encrypted))

                    // URLs
                    .clearUrls()
                    .addAllUrls(urls.filter { it.isNotBlank() })

                    // Passkeys
                    .clearPasskeys()
                    .addAllPasskeys(
                        passkeys.map {
                            ItemV1.Passkey.newBuilder()
                                .setKeyId(it.id.value)
                                .setContent(ByteString.copyFrom(encryptionContext.decrypt(it.contents)))
                                .setDomain(it.domain)
                                .setRpId(it.rpId)
                                .setRpName(it.rpName)
                                .setUserName(it.userName)
                                .setUserDisplayName(it.userDisplayName)
                                .setUserId(ByteString.copyFrom(it.userId))
                                .setNote(it.note)
                                .setCreateTime(it.createTime.epochSeconds.toInt())
                                .setUserHandle(ByteString.copyFrom(it.userHandle))
                                .setCredentialId(ByteString.copyFrom(it.credentialId))
                                .build()
                        }
                    )
                    .build()
            )
        }

        is ItemContents.Note -> builder.content.toBuilder().setNote(
            ItemV1.ItemNote.newBuilder().build()
        )

        is ItemContents.Alias -> builder.content.toBuilder().setAlias(
            ItemV1.ItemAlias.newBuilder().build()
        )

        is ItemContents.CreditCard -> {
            builder.content.toBuilder().setCreditCard(
                builder.content.creditCard.toBuilder()
                    .setCardholderName(cardHolder)
                    .setNumber(number)
                    .setCardType(
                        when (type) {
                            CreditCardType.Other -> ItemV1.CardType.Other
                            CreditCardType.Visa -> ItemV1.CardType.Visa
                            CreditCardType.MasterCard -> ItemV1.CardType.Mastercard
                            CreditCardType.AmericanExpress -> ItemV1.CardType.AmericanExpress
                        }
                    )
                    .setVerificationNumber(encryptionContext.decrypt(cvv.encrypted))
                    .setPin(encryptionContext.decrypt(pin.encrypted))
                    .setExpirationDate(expirationDate)
                    .build()
            )
        }

        is ItemContents.Unknown -> throw IllegalStateException("Cannot be unknown")
    }.build()

    return builder
        .setContent(content)
        .build()
}

