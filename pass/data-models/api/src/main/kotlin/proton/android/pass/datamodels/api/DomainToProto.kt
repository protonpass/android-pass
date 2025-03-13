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
import com.google.protobuf.timestamp
import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.domain.CreditCardType
import proton.android.pass.domain.CustomFieldContent
import proton.android.pass.domain.ItemContents
import proton_pass_item_v1.ItemV1
import proton_pass_item_v1.extraField
import proton_pass_item_v1.extraHiddenField
import proton_pass_item_v1.extraTextField
import proton_pass_item_v1.extraTimestampField
import proton_pass_item_v1.extraTotp
import java.util.UUID

private const val MILLIS_IN_SECOND = 1_000L
private const val NANOS_IN_MILLI = 1_000_000L

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
                .addAllExtraFields(customFields.mapToExtraFields(encryptionContext))

            builder.content.toBuilder().setLogin(
                builder.content.login.toBuilder()
                    .setItemEmail(itemEmail)
                    .setItemUsername(itemUsername)
                    .setPassword(encryptionContext.decrypt(password.encrypted))
                    .setTotpUri(encryptionContext.decrypt(primaryTotp.encrypted))

                    // URLs
                    .clearUrls()
                    .addAllUrls(urls.filter { it.isNotBlank() })

                    // Passkeys
                    .clearPasskeys()
                    .addAllPasskeys(
                        passkeys.map { passkey ->
                            var passkeyBuilder = ItemV1.Passkey.newBuilder()
                                .setKeyId(passkey.id.value)
                                .setContent(ByteString.copyFrom(passkey.contents))
                                .setDomain(passkey.domain)
                                .setRpId(passkey.rpId)
                                .setRpName(passkey.rpName)
                                .setUserName(passkey.userName)
                                .setUserDisplayName(passkey.userDisplayName)
                                .setUserId(ByteString.copyFrom(passkey.userId))
                                .setNote(passkey.note)
                                .setCreateTime(passkey.createTime.epochSeconds.toInt())
                                .setUserHandle(ByteString.copyFrom(passkey.userHandle))
                                .setCredentialId(ByteString.copyFrom(passkey.credentialId))

                            passkey.creationData?.let { creationData ->
                                passkeyBuilder = passkeyBuilder.setCreationData(
                                    ItemV1.PasskeyCreationData.newBuilder()
                                        .setOsName(creationData.osName)
                                        .setOsVersion(creationData.osVersion)
                                        .setDeviceName(creationData.deviceName)
                                        .setAppVersion(creationData.appVersion)
                                        .build()
                                )
                            }

                            passkeyBuilder.build()
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

        is ItemContents.Identity -> {
            builder.content.toBuilder().setIdentity(
                builder.content.identity.toBuilder()
                    // Personal Details
                    .setFullName(personalDetailsContent.fullName)
                    .setEmail(personalDetailsContent.email)
                    .setPhoneNumber(personalDetailsContent.phoneNumber)
                    .setFirstName(personalDetailsContent.firstName)
                    .setMiddleName(personalDetailsContent.middleName)
                    .setLastName(personalDetailsContent.lastName)
                    .setBirthdate(personalDetailsContent.birthdate)
                    .setGender(personalDetailsContent.gender)
                    .clearExtraPersonalDetails()
                    .addAllExtraPersonalDetails(
                        personalDetailsContent.customFields.mapToExtraFields(encryptionContext)
                    )
                    // Address details
                    .setOrganization(addressDetailsContent.organization)
                    .setStreetAddress(addressDetailsContent.streetAddress)
                    .setZipOrPostalCode(addressDetailsContent.zipOrPostalCode)
                    .setCity(addressDetailsContent.city)
                    .setStateOrProvince(addressDetailsContent.stateOrProvince)
                    .setCountryOrRegion(addressDetailsContent.countryOrRegion)
                    .setFloor(addressDetailsContent.floor)
                    .setCounty(addressDetailsContent.county)
                    .clearExtraAddressDetails()
                    .addAllExtraAddressDetails(
                        addressDetailsContent.customFields.mapToExtraFields(encryptionContext)
                    )
                    // Contact details
                    .setSocialSecurityNumber(contactDetailsContent.socialSecurityNumber)
                    .setPassportNumber(contactDetailsContent.passportNumber)
                    .setLicenseNumber(contactDetailsContent.licenseNumber)
                    .setWebsite(contactDetailsContent.website)
                    .setXHandle(contactDetailsContent.xHandle)
                    .setSecondPhoneNumber(contactDetailsContent.secondPhoneNumber)
                    .setLinkedin(contactDetailsContent.linkedin)
                    .setReddit(contactDetailsContent.reddit)
                    .setFacebook(contactDetailsContent.facebook)
                    .setYahoo(contactDetailsContent.yahoo)
                    .setInstagram(contactDetailsContent.instagram)
                    .clearExtraContactDetails()
                    .addAllExtraContactDetails(
                        contactDetailsContent.customFields.mapToExtraFields(encryptionContext)
                    )
                    // Work details
                    .setJobTitle(workDetailsContent.jobTitle)
                    .setCompany(workDetailsContent.company)
                    .setPersonalWebsite(workDetailsContent.personalWebsite)
                    .setWorkPhoneNumber(workDetailsContent.workPhoneNumber)
                    .setWorkEmail(workDetailsContent.workEmail)
                    .clearExtraWorkDetails()
                    .addAllExtraWorkDetails(
                        workDetailsContent.customFields.mapToExtraFields(encryptionContext)
                    )
                    .clearExtraSections()
                    .addAllExtraSections(
                        extraSectionContentList.map {
                            ItemV1.CustomSection.newBuilder()
                                .setSectionName(it.title)
                                .clearSectionFields()
                                .addAllSectionFields(it.customFieldList.mapToExtraFields(encryptionContext))
                                .build()
                        }
                    )
                    .build()
            )

        }

        is ItemContents.Custom ->
            builder.content.toBuilder().setCustom(
                builder.clearExtraFields()
                    .addAllExtraFields(customFieldList.mapToExtraFields(encryptionContext))
                    .content
                    .custom
                    .toBuilder()
                    .clearSections()
                    .addAllSections(
                        sectionContentList.map {
                            ItemV1.CustomSection.newBuilder()
                                .setSectionName(it.title)
                                .clearSectionFields()
                                .addAllSectionFields(it.customFieldList.mapToExtraFields(encryptionContext))
                                .build()
                        }
                    )
                    .build()
            )

        is ItemContents.WifiNetwork ->
            builder.content.toBuilder().setWifi(
                builder.clearExtraFields()
                    .addAllExtraFields(customFieldList.mapToExtraFields(encryptionContext))
                    .content
                    .wifi
                    .toBuilder()
                    .setSsid(ssid)
                    .setPassword(encryptionContext.decrypt(password.encrypted))
                    .setSecurityValue(wifiSecurityType.id)
                    .clearSections()
                    .addAllSections(
                        sectionContentList.map {
                            ItemV1.CustomSection.newBuilder()
                                .setSectionName(it.title)
                                .clearSectionFields()
                                .addAllSectionFields(it.customFieldList.mapToExtraFields(encryptionContext))
                                .build()
                        }
                    )
                    .build()
            )

        is ItemContents.SSHKey ->
            builder.content.toBuilder().setSshKey(
                builder.clearExtraFields()
                    .addAllExtraFields(customFieldList.mapToExtraFields(encryptionContext))
                    .content
                    .sshKey
                    .toBuilder()
                    .setPublicKey(publicKey)
                    .setPrivateKey(encryptionContext.decrypt(privateKey.encrypted))
                    .clearSections()
                    .addAllSections(
                        sectionContentList.map {
                            ItemV1.CustomSection.newBuilder()
                                .setSectionName(it.title)
                                .clearSectionFields()
                                .addAllSectionFields(it.customFieldList.mapToExtraFields(encryptionContext))
                                .build()
                        }
                    )
                    .build()
            )

        is ItemContents.Unknown -> throw IllegalStateException("Cannot be unknown")
    }.build()

    return builder
        .setContent(content)
        .build()
}

private fun List<CustomFieldContent>.mapToExtraFields(encryptionContext: EncryptionContext): List<ItemV1.ExtraField> =
    mapNotNull { customField ->
        when (customField) {
            is CustomFieldContent.Hidden -> extraField {
                fieldName = customField.label
                hidden = extraHiddenField {
                    content = encryptionContext.decrypt(customField.value.encrypted)
                }
            }

            is CustomFieldContent.Text -> extraField {
                fieldName = customField.label
                text = extraTextField {
                    content = customField.value
                }
            }

            is CustomFieldContent.Totp -> extraField {
                fieldName = customField.label
                totp = extraTotp {
                    totpUri = encryptionContext.decrypt(customField.value.encrypted)
                }
            }

            is CustomFieldContent.Date -> extraField {
                fieldName = customField.label
                timestamp = extraTimestampField {
                    timestamp = timestamp {
                        seconds = customField.value / MILLIS_IN_SECOND
                        nanos = (customField.value % MILLIS_IN_SECOND * NANOS_IN_MILLI).toInt()
                    }
                }
            }
        }
    }
