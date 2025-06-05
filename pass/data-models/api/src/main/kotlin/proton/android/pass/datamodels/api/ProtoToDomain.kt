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

import com.google.protobuf.Timestamp
import kotlinx.datetime.Instant
import proton.android.pass.common.api.None
import proton.android.pass.common.api.some
import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.domain.AddressDetails
import proton.android.pass.domain.ByteArrayWrapper
import proton.android.pass.domain.ContactDetails
import proton.android.pass.domain.CreditCardType
import proton.android.pass.domain.CustomField
import proton.android.pass.domain.ExtraSection
import proton.android.pass.domain.ItemType
import proton.android.pass.domain.Passkey
import proton.android.pass.domain.PasskeyCreationData
import proton.android.pass.domain.PasskeyId
import proton.android.pass.domain.PersonalDetails
import proton.android.pass.domain.WifiSecurityType
import proton.android.pass.domain.WorkDetails
import proton.android.pass.domain.entity.AppName
import proton.android.pass.domain.entity.PackageInfo
import proton.android.pass.domain.entity.PackageName
import proton_pass_item_v1.ItemV1
import proton_pass_item_v1.creationDataOrNull

private const val MILLIS_IN_SECOND = 1_000L
private const val NANOS_IN_MILLI = 1_000_000L

fun ItemType.Companion.fromParsed(
    context: EncryptionContext,
    parsed: ItemV1.Item,
    aliasEmail: String? = null
): ItemType = when (parsed.content.contentCase) {
    ItemV1.Content.ContentCase.LOGIN -> createLoginItemType(parsed, context)
    ItemV1.Content.ContentCase.NOTE -> createNoteItemType(parsed, context)
    ItemV1.Content.ContentCase.ALIAS -> createAliasItemType(parsed, aliasEmail, context)
    ItemV1.Content.ContentCase.CREDIT_CARD -> createCreditCardItemType(parsed, context)
    ItemV1.Content.ContentCase.IDENTITY -> createIdentityItemType(parsed, context)
    ItemV1.Content.ContentCase.CUSTOM -> createCustomItemType(parsed, context)
    ItemV1.Content.ContentCase.SSH_KEY -> createSSHKeyItemType(parsed, context)
    ItemV1.Content.ContentCase.WIFI -> createWifiNetworkItemType(parsed, context)
    ItemV1.Content.ContentCase.CONTENT_NOT_SET,
    null -> ItemType.Unknown
}

private fun createNoteItemType(parsed: ItemV1.Item, context: EncryptionContext): ItemType.Note = ItemType.Note(
    text = parsed.metadata.note,
    customFields = parsed.extraFieldsList.map { field -> field.toDomain(context) }
)

private fun createAliasItemType(
    parsed: ItemV1.Item,
    aliasEmail: String?,
    context: EncryptionContext
): ItemType.Alias {
    requireNotNull(aliasEmail)
    return ItemType.Alias(
        aliasEmail = aliasEmail,
        customFields = parsed.extraFieldsList.map { field -> field.toDomain(context) }
    )
}

@Suppress("LongMethod")
private fun createIdentityItemType(parsed: ItemV1.Item, context: EncryptionContext): ItemType.Identity {
    val content = parsed.content.identity
    return ItemType.Identity(
        personalDetails = PersonalDetails(
            firstName = content.firstName,
            middleName = content.middleName,
            lastName = content.lastName,
            fullName = content.fullName,
            birthdate = content.birthdate,
            gender = content.gender,
            email = content.email,
            phoneNumber = content.phoneNumber,
            customFields = content.extraPersonalDetailsList.map { field ->
                field.toDomain(context)
            }
        ),
        addressDetails = AddressDetails(
            organization = content.organization,
            streetAddress = content.streetAddress,
            zipOrPostalCode = content.zipOrPostalCode,
            city = content.city,
            stateOrProvince = content.stateOrProvince,
            countryOrRegion = content.countryOrRegion,
            floor = content.floor,
            county = content.county,
            customFields = content.extraAddressDetailsList.map { field ->
                field.toDomain(context)
            }
        ),
        contactDetails = ContactDetails(
            socialSecurityNumber = context.encrypt(content.socialSecurityNumber),
            passportNumber = content.passportNumber,
            licenseNumber = content.licenseNumber,
            website = content.website,
            xHandle = content.xHandle,
            secondPhoneNumber = content.secondPhoneNumber,
            linkedin = content.linkedin,
            reddit = content.reddit,
            facebook = content.facebook,
            yahoo = content.yahoo,
            instagram = content.instagram,
            customFields = content.extraContactDetailsList.map { field ->
                field.toDomain(context)
            }
        ),
        workDetails = WorkDetails(
            company = content.company,
            jobTitle = content.jobTitle,
            personalWebsite = content.personalWebsite,
            workPhoneNumber = content.workPhoneNumber,
            workEmail = content.workEmail,
            customFields = content.extraWorkDetailsList.map { field ->
                field.toDomain(context)
            }
        ),
        extraSections = content.extraSectionsList.map { section ->
            section.toDomain(context)
        }
    )
}

private fun createCreditCardItemType(parsed: ItemV1.Item, context: EncryptionContext): ItemType.CreditCard {
    val content = parsed.content.creditCard
    return ItemType.CreditCard(
        cardHolder = content.cardholderName,
        number = context.encrypt(content.number),
        cvv = context.encrypt(content.verificationNumber),
        pin = context.encrypt(content.pin),
        creditCardType = content.cardType.toDomain(),
        expirationDate = content.expirationDate,
        customFields = parsed.extraFieldsList.map { field -> field.toDomain(context) }
    )
}

private fun createLoginItemType(parsed: ItemV1.Item, context: EncryptionContext) = ItemType.Login(
    itemEmail = parsed.content.login.itemEmail,
    itemUsername = parsed.content.login.itemUsername,
    password = context.encrypt(parsed.content.login.password),
    websites = parsed.content.login.urlsList,
    packageInfoSet = parsed.platformSpecific.android.allowedAppsList.map {
        PackageInfo(PackageName(it.packageName), AppName(it.appName))
    }.toSet(),
    primaryTotp = context.encrypt(parsed.content.login.totpUri),
    customFields = parsed.extraFieldsList.map { field ->
        field.toDomain(context)
    },
    passkeys = parsed.content.login.passkeysList.map {
        Passkey(
            id = PasskeyId(it.keyId),
            domain = it.domain,
            rpId = it.rpId,
            rpName = it.rpName,
            userName = it.userName,
            userDisplayName = it.userDisplayName,
            userId = ByteArrayWrapper(it.userId.toByteArray()),
            contents = ByteArrayWrapper(it.content.toByteArray()),
            note = it.note,
            createTime = Instant.fromEpochSeconds(it.createTime.toLong()),
            credentialId = ByteArrayWrapper(it.credentialId.toByteArray()),
            userHandle = it.userHandle?.let { ByteArrayWrapper(it.toByteArray()) },
            creationData = it.creationDataOrNull?.let { creationData ->
                PasskeyCreationData(
                    osName = creationData.osName,
                    osVersion = creationData.osVersion,
                    deviceName = creationData.deviceName,
                    appVersion = creationData.appVersion
                )
            }
        )
    }
)

private fun createCustomItemType(parsed: ItemV1.Item, context: EncryptionContext): ItemType.Custom = ItemType.Custom(
    customFields = parsed.extraFieldsList.map { field ->
        field.toDomain(context)
    },
    extraSections = parsed.content.custom.sectionsList.map { section ->
        section.toDomain(context)
    }
)

private fun createWifiNetworkItemType(parsed: ItemV1.Item, context: EncryptionContext): ItemType.WifiNetwork =
    ItemType.WifiNetwork(
        ssid = parsed.content.wifi.ssid,
        password = context.encrypt(parsed.content.wifi.password),
        wifiSecurityType = when (parsed.content.wifi.security) {
            ItemV1.WifiSecurity.WPA -> WifiSecurityType.WPA
            ItemV1.WifiSecurity.WPA2 -> WifiSecurityType.WPA2
            ItemV1.WifiSecurity.WPA3 -> WifiSecurityType.WPA3
            ItemV1.WifiSecurity.WEP -> WifiSecurityType.WEP
            ItemV1.WifiSecurity.UnspecifiedWifiSecurity,
            ItemV1.WifiSecurity.UNRECOGNIZED,
            null -> WifiSecurityType.Unknown
        },
        customFields = parsed.extraFieldsList.map { field ->
            field.toDomain(context)
        },
        extraSections = parsed.content.wifi.sectionsList.map { section ->
            section.toDomain(context)
        }
    )

private fun createSSHKeyItemType(parsed: ItemV1.Item, context: EncryptionContext): ItemType.SSHKey = ItemType.SSHKey(
    publicKey = parsed.content.sshKey.publicKey,
    privateKey = context.encrypt(parsed.content.sshKey.privateKey),
    customFields = parsed.extraFieldsList.map { field ->
        field.toDomain(context)
    },
    extraSections = parsed.content.sshKey.sectionsList.map { section ->
        section.toDomain(context)
    }
)

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

        ItemV1.ExtraField.ContentCase.TIMESTAMP -> {
            val isEmpty = this.timestamp.timestamp.equals(Timestamp.getDefaultInstance())
            CustomField.Date(
                label = this.fieldName,
                value = if (isEmpty) {
                    None
                } else {
                    (
                        this.timestamp.timestamp.seconds * MILLIS_IN_SECOND +
                            this.timestamp.timestamp.nanos / NANOS_IN_MILLI
                        ).some()
                }
            )
        }

        ItemV1.ExtraField.ContentCase.CONTENT_NOT_SET,
        null -> CustomField.Unknown
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

fun ItemV1.CustomSection.toDomain(context: EncryptionContext): ExtraSection = ExtraSection(
    sectionName = this.sectionName,
    customFields = this.sectionFieldsList.map { field ->
        field.toDomain(context)
    }
)
