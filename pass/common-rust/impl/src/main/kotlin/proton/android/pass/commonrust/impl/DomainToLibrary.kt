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

import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.domain.AutofillUrl
import proton.android.pass.domain.AutofillUrlMode
import proton.android.pass.domain.CreditCardType
import proton.android.pass.domain.CustomFieldContent
import proton.android.pass.domain.ExtraSectionContent
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.Passkey
import proton.android.pass.domain.PasskeyCreationData
import proton.android.pass.domain.WifiSecurityType
import proton.android.pass.domain.entity.PackageInfo
import proton.android.pass.types.AliasItem as LibraryAliasItem
import proton.android.pass.types.AllowedAndroidApp as LibraryAllowedAndroidApp
import proton.android.pass.types.AndroidSpecific as LibraryAndroidSpecific
import proton.android.pass.types.AutofillUrl as LibraryAutofillUrl
import proton.android.pass.types.AutofillUrlMode as LibraryAutofillUrlMode
import proton.android.pass.types.CardType as LibraryCardType
import proton.android.pass.types.CreditCardItem as LibraryCreditCardItem
import proton.android.pass.types.CustomItem as LibraryCustomItem
import proton.android.pass.types.CustomSection as LibraryCustomSection
import proton.android.pass.types.IdentityItem as LibraryIdentityItem
import proton.android.pass.types.ItemContent as LibraryItemContent
import proton.android.pass.types.ItemData as LibraryItemData
import proton.android.pass.types.ItemExtraField as LibraryItemExtraField
import proton.android.pass.types.ItemExtraFieldContent as LibraryItemExtraFieldContent
import proton.android.pass.types.LoginItem as LibraryLoginItem
import proton.android.pass.types.NoteItem as LibraryNoteItem
import proton.android.pass.types.Passkey as LibraryPasskey
import proton.android.pass.types.PasskeyCreationData as LibraryPasskeyCreationData
import proton.android.pass.types.PlatformSpecific as LibraryPlatformSpecific
import proton.android.pass.types.SshKeyItem as LibrarySshKeyItem
import proton.android.pass.types.WifiItem as LibraryWifiItem
import proton.android.pass.types.WifiSecurity as LibraryWifiSecurity

internal const val MILLIS_IN_SECOND = 1_000L

internal fun ItemContents.toLibraryItemData(
    itemUuid: String,
    existing: LibraryItemData?,
    encryptionContext: EncryptionContext
): LibraryItemData = LibraryItemData(
    title = title,
    note = note,
    itemUuid = itemUuid,
    content = toLibraryContent(encryptionContext),
    extraFields = customFields.toLibraryExtraFields(encryptionContext),
    platformSpecific = toLibraryPlatformSpecific(existing),
    customIcon = existing?.customIcon
)

private fun ItemContents.toLibraryContent(encryptionContext: EncryptionContext): LibraryItemContent = when (this) {
    is ItemContents.Login -> LibraryItemContent.Login(toLibraryLogin(encryptionContext))
    is ItemContents.Note -> LibraryItemContent.Note(LibraryNoteItem())
    is ItemContents.Alias -> LibraryItemContent.Alias(LibraryAliasItem())
    is ItemContents.CreditCard -> LibraryItemContent.CreditCard(toLibraryCreditCard(encryptionContext))
    is ItemContents.Identity -> LibraryItemContent.Identity(toLibraryIdentity(encryptionContext))
    is ItemContents.Custom -> LibraryItemContent.Custom(
        LibraryCustomItem(sections = sectionContentList.toLibrarySections(encryptionContext))
    )
    is ItemContents.WifiNetwork -> LibraryItemContent.Wifi(toLibraryWifi(encryptionContext))
    is ItemContents.SSHKey -> LibraryItemContent.SshKey(
        LibrarySshKeyItem(
            privateKey = encryptionContext.decrypt(privateKey.encrypted),
            publicKey = publicKey,
            sections = sectionContentList.toLibrarySections(encryptionContext)
        )
    )
    is ItemContents.Unknown -> throw IllegalStateException("Cannot be unknown")
}

private fun ItemContents.Login.toLibraryLogin(encryptionContext: EncryptionContext): LibraryLoginItem =
    LibraryLoginItem(
        email = itemEmail,
        username = itemUsername,
        password = encryptionContext.decrypt(password.encrypted),
        urls = urls.filter { it.isNotBlank() },
        totpUri = encryptionContext.decrypt(primaryTotp.encrypted),
        passkeys = passkeys.map { it.toLibrary() },
        autofillUrls = autofillUrls.map { it.toLibrary() }
    )

private fun ItemContents.CreditCard.toLibraryCreditCard(encryptionContext: EncryptionContext): LibraryCreditCardItem =
    LibraryCreditCardItem(
        cardholderName = cardHolder,
        cardType = type.toLibrary(),
        number = number,
        verificationNumber = encryptionContext.decrypt(cvv.encrypted),
        expirationDate = expirationDate,
        pin = encryptionContext.decrypt(pin.encrypted)
    )

private fun ItemContents.Identity.toLibraryIdentity(encryptionContext: EncryptionContext): LibraryIdentityItem =
    LibraryIdentityItem(
        fullName = personalDetailsContent.fullName,
        email = personalDetailsContent.email,
        phoneNumber = personalDetailsContent.phoneNumber,
        firstName = personalDetailsContent.firstName,
        middleName = personalDetailsContent.middleName,
        lastName = personalDetailsContent.lastName,
        birthdate = personalDetailsContent.birthdate,
        gender = personalDetailsContent.gender,
        extraPersonalDetails = personalDetailsContent.customFields.toLibraryExtraFields(encryptionContext),
        organization = addressDetailsContent.organization,
        streetAddress = addressDetailsContent.streetAddress,
        zipOrPostalCode = addressDetailsContent.zipOrPostalCode,
        city = addressDetailsContent.city,
        stateOrProvince = addressDetailsContent.stateOrProvince,
        countryOrRegion = addressDetailsContent.countryOrRegion,
        floor = addressDetailsContent.floor,
        county = addressDetailsContent.county,
        extraAddressDetails = addressDetailsContent.customFields.toLibraryExtraFields(encryptionContext),
        socialSecurityNumber = encryptionContext.decrypt(contactDetailsContent.socialSecurityNumber.encrypted),
        passportNumber = contactDetailsContent.passportNumber,
        licenseNumber = contactDetailsContent.licenseNumber,
        website = contactDetailsContent.website,
        xHandle = contactDetailsContent.xHandle,
        secondPhoneNumber = contactDetailsContent.secondPhoneNumber,
        linkedin = contactDetailsContent.linkedin,
        reddit = contactDetailsContent.reddit,
        facebook = contactDetailsContent.facebook,
        yahoo = contactDetailsContent.yahoo,
        instagram = contactDetailsContent.instagram,
        extraContactDetails = contactDetailsContent.customFields.toLibraryExtraFields(encryptionContext),
        company = workDetailsContent.company,
        jobTitle = workDetailsContent.jobTitle,
        personalWebsite = workDetailsContent.personalWebsite,
        workPhoneNumber = workDetailsContent.workPhoneNumber,
        workEmail = workDetailsContent.workEmail,
        extraWorkDetails = workDetailsContent.customFields.toLibraryExtraFields(encryptionContext),
        extraSections = extraSectionContentList.toLibrarySections(encryptionContext)
    )

private fun ItemContents.WifiNetwork.toLibraryWifi(encryptionContext: EncryptionContext): LibraryWifiItem =
    LibraryWifiItem(
        ssid = ssid,
        password = encryptionContext.decrypt(password.encrypted),
        security = wifiSecurityType.toLibrary(),
        sections = sectionContentList.toLibrarySections(encryptionContext)
    )

private fun List<ExtraSectionContent>.toLibrarySections(
    encryptionContext: EncryptionContext
): List<LibraryCustomSection> = map {
    LibraryCustomSection(
        sectionName = it.title,
        sectionFields = it.customFieldList.toLibraryExtraFields(encryptionContext)
    )
}

private fun ItemContents.toLibraryPlatformSpecific(existing: LibraryItemData?): LibraryPlatformSpecific? = when {
    this is ItemContents.Login -> packageInfoSet.toLibraryPlatformSpecific(existing)
    existing != null -> existing.platformSpecific
    else -> LibraryPlatformSpecific(android = LibraryAndroidSpecific(allowedApps = emptyList()))
}

private fun Set<PackageInfo>.toLibraryPlatformSpecific(existing: LibraryItemData?): LibraryPlatformSpecific? {
    if (existing != null && isEmpty() && existing.platformSpecific?.android == null) {
        return existing.platformSpecific
    }
    val existingApps = existing?.platformSpecific?.android?.allowedApps.orEmpty()
    return LibraryPlatformSpecific(
        android = LibraryAndroidSpecific(
            allowedApps = map { info ->
                val existingHashes = existingApps
                    .firstOrNull { it.packageName == info.packageName.value }
                    ?.hashes
                    .orEmpty()
                LibraryAllowedAndroidApp(
                    packageName = info.packageName.value,
                    hashes = (existingHashes + info.hashes).distinct(),
                    appName = info.appName.value
                )
            }
        )
    )
}

private fun List<CustomFieldContent>.toLibraryExtraFields(
    encryptionContext: EncryptionContext
): List<LibraryItemExtraField> = map { customField ->
    LibraryItemExtraField(
        name = customField.label,
        content = when (customField) {
            is CustomFieldContent.Text -> LibraryItemExtraFieldContent.Text(customField.value)
            is CustomFieldContent.Hidden ->
                LibraryItemExtraFieldContent.Hidden(encryptionContext.decrypt(customField.value.encrypted))
            is CustomFieldContent.Totp ->
                LibraryItemExtraFieldContent.Totp(encryptionContext.decrypt(customField.value.encrypted))
            is CustomFieldContent.Date ->
                LibraryItemExtraFieldContent.Timestamp((customField.value ?: 0L) / MILLIS_IN_SECOND)
        }
    )
}

internal fun Passkey.toLibrary(): LibraryPasskey = LibraryPasskey(
    keyId = id.value,
    content = contents.data,
    domain = domain,
    rpId = rpId ?: "",
    rpName = rpName,
    userName = userName,
    userDisplayName = userDisplayName,
    userId = userId.data,
    createTime = createTime.epochSeconds.toUInt(),
    note = note,
    credentialId = credentialId.data,
    userHandle = userHandle?.data ?: ByteArray(0),
    creationData = creationData?.toLibrary()
)

private fun PasskeyCreationData.toLibrary(): LibraryPasskeyCreationData = LibraryPasskeyCreationData(
    osName = osName,
    osVersion = osVersion,
    deviceName = deviceName,
    appVersion = appVersion
)

internal fun AutofillUrl.toLibrary(): LibraryAutofillUrl = LibraryAutofillUrl(url = url, mode = mode.toLibrary())

private fun AutofillUrlMode.toLibrary(): LibraryAutofillUrlMode = when (this) {
    AutofillUrlMode.Default -> LibraryAutofillUrlMode.DEFAULT
    AutofillUrlMode.Exact -> LibraryAutofillUrlMode.EXACT
    AutofillUrlMode.Never -> LibraryAutofillUrlMode.NEVER
    AutofillUrlMode.StartWith -> LibraryAutofillUrlMode.START_WITH
    AutofillUrlMode.Pattern -> LibraryAutofillUrlMode.PATTERN
    AutofillUrlMode.RegularExpression -> LibraryAutofillUrlMode.REGULAR_EXPRESSION
    AutofillUrlMode.ExactPath -> LibraryAutofillUrlMode.EXACT_PATH
}

private fun CreditCardType.toLibrary(): LibraryCardType = when (this) {
    CreditCardType.Other -> LibraryCardType.OTHER
    CreditCardType.Visa -> LibraryCardType.VISA
    CreditCardType.MasterCard -> LibraryCardType.MASTERCARD
    CreditCardType.AmericanExpress -> LibraryCardType.AMERICAN_EXPRESS
}

private fun WifiSecurityType.toLibrary(): LibraryWifiSecurity = when (this) {
    WifiSecurityType.Unknown -> LibraryWifiSecurity.UNSPECIFIED_WIFI_SECURITY
    WifiSecurityType.WPA -> LibraryWifiSecurity.WPA
    WifiSecurityType.WPA2 -> LibraryWifiSecurity.WPA2
    WifiSecurityType.WPA3 -> LibraryWifiSecurity.WPA3
    WifiSecurityType.WEP -> LibraryWifiSecurity.WEP
}
