/*
 * Copyright (c) 2025 Proton AG
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

package proton.android.pass.domain

fun toItemContents(
    decrypt: (String) -> String,
    itemType: ItemType,
    title: String,
    note: String,
    flags: Flags
): ItemContents = when (itemType) {
    is ItemType.Alias -> createAlias(decrypt, title, note, itemType, flags.isAliasDisabled())
    is ItemType.Login -> createLogin(decrypt, title, note, itemType)
    is ItemType.Note -> createNote(decrypt, title, note)
    is ItemType.CreditCard -> createCreditCard(decrypt, title, note, itemType)
    is ItemType.Identity -> createIdentity(decrypt, title, note, itemType)
    is ItemType.Custom -> createCustom(decrypt, title, note, itemType)
    is ItemType.WifiNetwork -> createWifiNetWork(decrypt, title, note, itemType)
    is ItemType.SSHKey -> createSSHKey(decrypt, title, note, itemType)
    ItemType.Password, ItemType.Unknown -> ItemContents.Unknown(
        title = decrypt(title),
        note = decrypt(note)
    )
}

@Suppress("UNCHECKED_CAST")
fun <T : ItemContents> Item.toItemContents(decrypt: (String) -> String): T = when (val type = this.itemType) {
    is ItemType.Alias -> createAlias(decrypt, title, note, type, flags.isAliasDisabled())
    is ItemType.Login -> createLogin(decrypt, title, note, type)
    is ItemType.Note -> createNote(decrypt, title, note)
    is ItemType.CreditCard -> createCreditCard(decrypt, title, note, type)
    is ItemType.Identity -> createIdentity(decrypt, title, note, type)
    is ItemType.Custom -> createCustom(decrypt, title, note, type)
    is ItemType.WifiNetwork -> createWifiNetWork(decrypt, title, note, type)
    is ItemType.SSHKey -> createSSHKey(decrypt, title, note, type)
    ItemType.Password, ItemType.Unknown -> ItemContents.Unknown(
        title = decrypt(title),
        note = decrypt(note)
    )
} as T

private fun createAlias(
    decrypt: (String) -> String,
    title: String,
    note: String,
    type: ItemType.Alias,
    isAliasDisabled: Boolean
) = ItemContents.Alias(
    title = decrypt(title),
    note = decrypt(note),
    aliasEmail = type.aliasEmail,
    isDisabled = isAliasDisabled
)

private fun createLogin(
    decrypt: (String) -> String,
    title: String,
    note: String,
    type: ItemType.Login
) = ItemContents.Login(
    title = decrypt(title),
    note = decrypt(note),
    itemEmail = type.itemEmail,
    itemUsername = type.itemUsername,
    password = concealedOrEmpty(type.password, decrypt),
    urls = type.websites,
    packageInfoSet = type.packageInfoSet,
    primaryTotp = concealedOrEmpty(type.primaryTotp, decrypt),
    customFields = type.customFields.mapNotNull { it.toContent(decrypt, true) },
    passkeys = type.passkeys
)

private fun createNote(
    decrypt: (String) -> String,
    title: String,
    note: String
) = ItemContents.Note(
    title = decrypt(title),
    note = decrypt(note)
)

private fun createCreditCard(
    decrypt: (String) -> String,
    title: String,
    note: String,
    type: ItemType.CreditCard
) = ItemContents.CreditCard(
    title = decrypt(title),
    note = decrypt(note),
    type = type.creditCardType,
    cardHolder = type.cardHolder,
    number = decrypt(type.number),
    cvv = concealedOrEmpty(type.cvv, decrypt),
    pin = concealedOrEmpty(type.pin, decrypt),
    expirationDate = type.expirationDate
)

private fun createIdentity(
    decrypt: (String) -> String,
    title: String,
    note: String,
    type: ItemType.Identity
) = ItemContents.Identity(
    title = decrypt(title),
    note = decrypt(note),
    personalDetailsContent = type.personalDetails.toContent(decrypt),
    addressDetailsContent = type.addressDetails.toContent(decrypt),
    contactDetailsContent = type.contactDetails.toContent(decrypt),
    workDetailsContent = type.workDetails.toContent(decrypt),
    extraSectionContentList = type.extraSections.map { it.toContent(decrypt) }
)

private fun ExtraSection.toContent(decrypt: (String) -> String) = ExtraSectionContent(
    title = sectionName,
    customFieldList = customFields.mapNotNull { it.toContent(decrypt, true) }
)

private fun PersonalDetails.toContent(decrypt: (String) -> String) = PersonalDetailsContent(
    fullName = fullName,
    firstName = firstName,
    middleName = middleName,
    lastName = lastName,
    birthdate = birthdate,
    gender = gender,
    email = email,
    phoneNumber = phoneNumber,
    customFields = customFields.mapNotNull { it.toContent(decrypt, true) }
)

private fun AddressDetails.toContent(decrypt: (String) -> String) = AddressDetailsContent(
    organization = organization,
    streetAddress = streetAddress,
    zipOrPostalCode = zipOrPostalCode,
    city = city,
    stateOrProvince = stateOrProvince,
    countryOrRegion = countryOrRegion,
    floor = floor,
    county = county,
    customFields = customFields.mapNotNull { it.toContent(decrypt, true) }
)

private fun ContactDetails.toContent(decrypt: (String) -> String) = ContactDetailsContent(
    socialSecurityNumber = concealedOrEmpty(socialSecurityNumber, decrypt),
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
    customFields = customFields.mapNotNull { it.toContent(decrypt, true) }
)

private fun WorkDetails.toContent(decrypt: (String) -> String) = WorkDetailsContent(
    company = company,
    jobTitle = jobTitle,
    personalWebsite = personalWebsite,
    workPhoneNumber = workPhoneNumber,
    workEmail = workEmail,
    customFields = customFields.mapNotNull { it.toContent(decrypt, true) }
)

private fun createCustom(
    decrypt: (String) -> String,
    title: String,
    note: String,
    type: ItemType.Custom
) = ItemContents.Custom(
    title = decrypt(title),
    note = decrypt(note),
    customFieldList = type.customFields.mapNotNull { it.toContent(decrypt, true) },
    sectionContentList = type.extraSections.map { it.toContent(decrypt) }
)

private fun createWifiNetWork(
    decrypt: (String) -> String,
    title: String,
    note: String,
    type: ItemType.WifiNetwork
) = ItemContents.WifiNetwork(
    title = decrypt(title),
    note = decrypt(note),
    ssid = type.ssid,
    password = concealedOrEmpty(type.password, decrypt),
    wifiSecurityType = type.wifiSecurityType,
    customFieldList = type.customFields.mapNotNull { it.toContent(decrypt, true) },
    sectionContentList = type.extraSections.map { it.toContent(decrypt) }
)

private fun createSSHKey(
    decrypt: (String) -> String,
    title: String,
    note: String,
    type: ItemType.SSHKey
) = ItemContents.SSHKey(
    title = decrypt(title),
    note = decrypt(note),
    publicKey = type.publicKey,
    privateKey = concealedOrEmpty(type.privateKey, decrypt),
    customFieldList = type.customFields.mapNotNull { it.toContent(decrypt, true) },
    sectionContentList = type.extraSections.map { it.toContent(decrypt) }
)

private fun concealedOrEmpty(value: String, decrypt: (String) -> String): HiddenState {
    if (value.isEmpty()) return HiddenState.Empty(value)
    val decryptedValue = decrypt(value)
    return if (decryptedValue.isEmpty()) {
        HiddenState.Empty(value)
    } else {
        HiddenState.Concealed(value)
    }
}
