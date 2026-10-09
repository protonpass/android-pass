/*
 * Copyright (c) 2026 Proton AG
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

package proton.android.pass.commonrust.impl

import kotlinx.datetime.Instant
import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.domain.AddressDetails
import proton.android.pass.domain.AutofillUrl
import proton.android.pass.domain.AutofillUrlMode
import proton.android.pass.domain.ByteArrayWrapper
import proton.android.pass.domain.ContactDetails
import proton.android.pass.domain.CreditCardType
import proton.android.pass.domain.CustomField
import proton.android.pass.domain.ExtraSection
import proton.android.pass.domain.ItemType
import proton.android.pass.domain.ParsedItem
import proton.android.pass.domain.Passkey
import proton.android.pass.domain.PasskeyCreationData
import proton.android.pass.domain.PasskeyId
import proton.android.pass.domain.PersonalDetails
import proton.android.pass.domain.WifiSecurityType
import proton.android.pass.domain.WorkDetails
import proton.android.pass.domain.entity.AppName
import proton.android.pass.domain.entity.PackageInfo
import proton.android.pass.domain.entity.PackageName
import proton.android.pass.types.AutofillUrl as LibraryAutofillUrl
import proton.android.pass.types.AutofillUrlMode as LibraryAutofillUrlMode
import proton.android.pass.types.CardType as LibraryCardType
import proton.android.pass.types.CreditCardItem as LibraryCreditCardItem
import proton.android.pass.types.CustomSection as LibraryCustomSection
import proton.android.pass.types.IdentityItem as LibraryIdentityItem
import proton.android.pass.types.ItemContent as LibraryItemContent
import proton.android.pass.types.ItemData as LibraryItemData
import proton.android.pass.types.ItemExtraField as LibraryItemExtraField
import proton.android.pass.types.ItemExtraFieldContent as LibraryItemExtraFieldContent
import proton.android.pass.types.LoginItem as LibraryLoginItem
import proton.android.pass.types.Passkey as LibraryPasskey
import proton.android.pass.types.PasskeyCreationData as LibraryPasskeyCreationData
import proton.android.pass.types.WifiSecurity as LibraryWifiSecurity

internal fun LibraryItemData.toParsedItem(encryptionContext: EncryptionContext, aliasEmail: String?): ParsedItem =
    ParsedItem(
        itemUuid = itemUuid,
        title = title,
        note = note,
        itemType = toItemType(encryptionContext, aliasEmail),
        packageInfoSet = toParsedPackageInfoSet()
    )

internal fun LibraryItemData.toItemType(encryptionContext: EncryptionContext, aliasEmail: String?): ItemType {
    val customFields = extraFields.map { it.toDomain(encryptionContext) }
    return when (val itemContent = content) {
        is LibraryItemContent.Login -> itemContent.v1.toLogin(this, customFields, encryptionContext)
        is LibraryItemContent.Note -> ItemType.Note(text = note, customFields = customFields)
        is LibraryItemContent.Alias -> ItemType.Alias(
            aliasEmail = requireNotNull(aliasEmail),
            customFields = customFields
        )
        is LibraryItemContent.CreditCard -> itemContent.v1.toDomain(customFields, encryptionContext)
        is LibraryItemContent.Identity -> itemContent.v1.toDomain(encryptionContext)
        is LibraryItemContent.Custom -> ItemType.Custom(
            customFields = customFields,
            extraSections = itemContent.v1.sections.map { it.toDomain(encryptionContext) }
        )
        is LibraryItemContent.SshKey -> ItemType.SSHKey(
            publicKey = itemContent.v1.publicKey,
            privateKey = encryptionContext.encrypt(itemContent.v1.privateKey),
            customFields = customFields,
            extraSections = itemContent.v1.sections.map { it.toDomain(encryptionContext) }
        )
        is LibraryItemContent.Wifi -> ItemType.WifiNetwork(
            ssid = itemContent.v1.ssid,
            password = encryptionContext.encrypt(itemContent.v1.password),
            wifiSecurityType = itemContent.v1.security.toDomain(),
            customFields = customFields,
            extraSections = itemContent.v1.sections.map { it.toDomain(encryptionContext) }
        )
    }
}

private fun LibraryLoginItem.toLogin(
    parsed: LibraryItemData,
    customFields: List<CustomField>,
    encryptionContext: EncryptionContext
): ItemType.Login = ItemType.Login(
    itemEmail = email,
    itemUsername = username,
    password = encryptionContext.encrypt(password),
    websites = urls,
    packageInfoSet = parsed.toParsedPackageInfoSet(),
    primaryTotp = encryptionContext.encrypt(totpUri),
    customFields = customFields,
    autofillUrls = autofillUrls.map { it.toDomain() },
    passkeys = passkeys.map { it.toDomain() }
)

private fun LibraryItemData.toParsedPackageInfoSet(): Set<PackageInfo> =
    platformSpecific?.android?.allowedApps.orEmpty().map {
        PackageInfo(
            packageName = PackageName(it.packageName),
            appName = AppName(it.appName),
            hashes = it.hashes.toSet()
        )
    }.toSet()

private fun LibraryCreditCardItem.toDomain(
    customFields: List<CustomField>,
    encryptionContext: EncryptionContext
): ItemType.CreditCard = ItemType.CreditCard(
    cardHolder = cardholderName,
    number = encryptionContext.encrypt(number),
    cvv = encryptionContext.encrypt(verificationNumber),
    pin = encryptionContext.encrypt(pin),
    creditCardType = cardType.toDomain(),
    expirationDate = expirationDate,
    customFields = customFields
)

private fun LibraryIdentityItem.toDomain(encryptionContext: EncryptionContext): ItemType.Identity = ItemType.Identity(
    personalDetails = PersonalDetails(
        firstName = firstName,
        middleName = middleName,
        lastName = lastName,
        fullName = fullName,
        birthdate = birthdate,
        gender = gender,
        email = email,
        phoneNumber = phoneNumber,
        customFields = extraPersonalDetails.map { it.toDomain(encryptionContext) }
    ),
    addressDetails = AddressDetails(
        organization = organization,
        streetAddress = streetAddress,
        zipOrPostalCode = zipOrPostalCode,
        city = city,
        stateOrProvince = stateOrProvince,
        countryOrRegion = countryOrRegion,
        floor = floor,
        county = county,
        customFields = extraAddressDetails.map { it.toDomain(encryptionContext) }
    ),
    contactDetails = ContactDetails(
        socialSecurityNumber = encryptionContext.encrypt(socialSecurityNumber),
        passportNumber = passportNumber,
        licenseNumber = licenseNumber,
        website = website,
        xHandle = xHandle,
        secondPhoneNumber = secondPhoneNumber,
        linkedin = linkedin,
        reddit = reddit,
        facebook = facebook,
        yahoo = yahoo,
        instagram = instagram,
        customFields = extraContactDetails.map { it.toDomain(encryptionContext) }
    ),
    workDetails = WorkDetails(
        company = company,
        jobTitle = jobTitle,
        personalWebsite = personalWebsite,
        workPhoneNumber = workPhoneNumber,
        workEmail = workEmail,
        customFields = extraWorkDetails.map { it.toDomain(encryptionContext) }
    ),
    extraSections = extraSections.map { it.toDomain(encryptionContext) }
)

private fun LibraryCustomSection.toDomain(encryptionContext: EncryptionContext): ExtraSection = ExtraSection(
    sectionName = sectionName,
    customFields = sectionFields.map { it.toDomain(encryptionContext) }
)

internal fun LibraryItemExtraField.toDomain(encryptionContext: EncryptionContext): CustomField =
    when (val fieldContent = content) {
        is LibraryItemExtraFieldContent.Text -> CustomField.Text(label = name, value = fieldContent.v1)
        is LibraryItemExtraFieldContent.Hidden ->
            CustomField.Hidden(label = name, value = encryptionContext.encrypt(fieldContent.v1))
        is LibraryItemExtraFieldContent.Totp ->
            CustomField.Totp(label = name, value = encryptionContext.encrypt(fieldContent.v1))
        is LibraryItemExtraFieldContent.Timestamp -> CustomField.Date(
            label = name,
            value = fieldContent.v1.takeIf { it != 0L }?.let { it * MILLIS_IN_SECOND }
        )
    }

private fun LibraryPasskey.toDomain(): Passkey = Passkey(
    id = PasskeyId(keyId),
    domain = domain,
    rpId = rpId,
    rpName = rpName,
    userName = userName,
    userDisplayName = userDisplayName,
    userId = ByteArrayWrapper(userId),
    contents = ByteArrayWrapper(content),
    note = note,
    createTime = Instant.fromEpochSeconds(createTime.toLong()),
    credentialId = ByteArrayWrapper(credentialId),
    userHandle = ByteArrayWrapper(userHandle),
    creationData = creationData?.toDomain()
)

private fun LibraryPasskeyCreationData.toDomain(): PasskeyCreationData = PasskeyCreationData(
    osName = osName,
    osVersion = osVersion,
    deviceName = deviceName,
    appVersion = appVersion
)

private fun LibraryAutofillUrl.toDomain(): AutofillUrl = AutofillUrl(url = url, mode = mode.toDomain())

private fun LibraryAutofillUrlMode.toDomain(): AutofillUrlMode = when (this) {
    LibraryAutofillUrlMode.DEFAULT -> AutofillUrlMode.Default
    LibraryAutofillUrlMode.EXACT -> AutofillUrlMode.Exact
    LibraryAutofillUrlMode.NEVER -> AutofillUrlMode.Never
    LibraryAutofillUrlMode.START_WITH -> AutofillUrlMode.StartWith
    LibraryAutofillUrlMode.PATTERN -> AutofillUrlMode.Pattern
    LibraryAutofillUrlMode.REGULAR_EXPRESSION -> AutofillUrlMode.RegularExpression
    LibraryAutofillUrlMode.EXACT_PATH -> AutofillUrlMode.ExactPath
}

private fun LibraryCardType.toDomain(): CreditCardType = when (this) {
    LibraryCardType.VISA -> CreditCardType.Visa
    LibraryCardType.MASTERCARD -> CreditCardType.MasterCard
    LibraryCardType.AMERICAN_EXPRESS -> CreditCardType.AmericanExpress
    LibraryCardType.OTHER,
    LibraryCardType.UNSPECIFIED -> CreditCardType.Other
}

private fun LibraryWifiSecurity.toDomain(): WifiSecurityType = when (this) {
    LibraryWifiSecurity.WPA -> WifiSecurityType.WPA
    LibraryWifiSecurity.WPA2 -> WifiSecurityType.WPA2
    LibraryWifiSecurity.WPA3 -> WifiSecurityType.WPA3
    LibraryWifiSecurity.WEP -> WifiSecurityType.WEP
    LibraryWifiSecurity.UNSPECIFIED_WIFI_SECURITY -> WifiSecurityType.Unknown
}
