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

package proton.android.pass.commonui.api

import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.crypto.api.toEncryptedByteArray
import proton.android.pass.datamodels.api.fromParsed
import proton.android.pass.datamodels.api.toContent
import proton.android.pass.domain.AddressDetails
import proton.android.pass.domain.AddressDetailsContent
import proton.android.pass.domain.ContactDetails
import proton.android.pass.domain.ContactDetailsContent
import proton.android.pass.domain.ExtraSection
import proton.android.pass.domain.ExtraSectionContent
import proton.android.pass.domain.Flags
import proton.android.pass.domain.HiddenState
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemEncrypted
import proton.android.pass.domain.ItemType
import proton.android.pass.domain.PersonalDetails
import proton.android.pass.domain.PersonalDetailsContent
import proton.android.pass.domain.WorkDetails
import proton.android.pass.domain.WorkDetailsContent
import proton_pass_item_v1.ItemV1

fun Item.toUiModel(context: EncryptionContext): ItemUiModel = ItemUiModel(
    id = id,
    shareId = shareId,
    userId = userId,
    contents = toItemContents(
        itemType = itemType,
        encryptionContext = context,
        title = title,
        note = note,
        flags = flags
    ),
    state = state,
    createTime = createTime,
    modificationTime = modificationTime,
    lastAutofillTime = lastAutofillTime.value(),
    isPinned = isPinned,
    category = itemType.category,
    revision = revision,
    shareCount = shareCount,
    isOwner = isOwner
)

fun ItemEncrypted.toUiModel(context: EncryptionContext): ItemUiModel {
    val decryptedContent = context.decrypt(content)
    val parsed = ItemV1.Item.parseFrom(decryptedContent)
    val itemType = ItemType.fromParsed(context, parsed, aliasEmail)
    return ItemUiModel(
        id = id,
        shareId = shareId,
        userId = userId,
        contents = toItemContents(
            encryptionContext = context,
            itemType = itemType,
            title = title,
            note = note,
            flags = flags
        ),
        state = state,
        createTime = createTime,
        modificationTime = modificationTime,
        lastAutofillTime = lastAutofillTime.value(),
        isPinned = isPinned,
        category = itemType.category,
        revision = revision,
        shareCount = shareCount,
        isOwner = isOwner
    )
}

fun Item.itemName(context: EncryptionContext): String = context.decrypt(title)

fun toItemContents(
    itemType: ItemType,
    encryptionContext: EncryptionContext,
    title: String,
    note: String,
    flags: Flags
): ItemContents = when (itemType) {
    is ItemType.Alias -> createAlias(
        encryptionContext,
        title,
        note,
        itemType,
        flags.isAliasDisabled()
    )

    is ItemType.Login -> createLogin(encryptionContext, title, note, itemType)
    is ItemType.Note -> createNote(encryptionContext, title, note)
    is ItemType.CreditCard -> createCreditCard(encryptionContext, title, note, itemType)
    is ItemType.Identity -> createIdentity(encryptionContext, title, note, itemType)
    is ItemType.Custom -> createCustom(encryptionContext, title, note, itemType)
    ItemType.Password,
    ItemType.Unknown -> ItemContents.Unknown(
        title = encryptionContext.decrypt(title),
        note = encryptionContext.decrypt(note)
    )
}

private fun createAlias(
    encryptionContext: EncryptionContext,
    title: String,
    note: String,
    type: ItemType.Alias,
    isAliasDisabled: Boolean
) = ItemContents.Alias(
    title = encryptionContext.decrypt(title),
    note = encryptionContext.decrypt(note),
    aliasEmail = type.aliasEmail,
    isDisabled = isAliasDisabled
)

private fun createLogin(
    encryptionContext: EncryptionContext,
    title: String,
    note: String,
    type: ItemType.Login
) = ItemContents.Login(
    title = encryptionContext.decrypt(title),
    note = encryptionContext.decrypt(note),
    itemEmail = type.itemEmail,
    itemUsername = type.itemUsername,
    password = concealedOrEmpty(type.password, encryptionContext),
    urls = type.websites,
    packageInfoSet = type.packageInfoSet,
    primaryTotp = concealedOrEmpty(type.primaryTotp, encryptionContext),
    customFields = type.customFields.mapNotNull { it.toContent(encryptionContext, true) },
    passkeys = type.passkeys
)

private fun createNote(
    encryptionContext: EncryptionContext,
    title: String,
    note: String
) = ItemContents.Note(
    title = encryptionContext.decrypt(title),
    note = encryptionContext.decrypt(note)
)

private fun createCreditCard(
    encryptionContext: EncryptionContext,
    title: String,
    note: String,
    type: ItemType.CreditCard
) = ItemContents.CreditCard(
    title = encryptionContext.decrypt(title),
    note = encryptionContext.decrypt(note),
    type = type.creditCardType,
    cardHolder = type.cardHolder,
    number = encryptionContext.decrypt(type.number),
    cvv = concealedOrEmpty(type.cvv, encryptionContext),
    pin = concealedOrEmpty(type.pin, encryptionContext),
    expirationDate = type.expirationDate
)

private fun createIdentity(
    encryptionContext: EncryptionContext,
    title: String,
    note: String,
    type: ItemType.Identity
) = ItemContents.Identity(
    title = encryptionContext.decrypt(title),
    note = encryptionContext.decrypt(note),
    personalDetailsContent = type.personalDetails.toContent(encryptionContext),
    addressDetailsContent = type.addressDetails.toContent(encryptionContext),
    contactDetailsContent = type.contactDetails.toContent(encryptionContext),
    workDetailsContent = type.workDetails.toContent(encryptionContext),
    extraSectionContentList = type.extraSections.map { it.toContent(encryptionContext) }
)

private fun PersonalDetails.toContent(encryptionContext: EncryptionContext) = PersonalDetailsContent(
    fullName = fullName,
    firstName = firstName,
    middleName = middleName,
    lastName = lastName,
    birthdate = birthdate,
    gender = gender,
    email = email,
    phoneNumber = phoneNumber,
    customFields = customFields.mapNotNull { it.toContent(encryptionContext, true) }
)

private fun AddressDetails.toContent(encryptionContext: EncryptionContext) = AddressDetailsContent(
    organization = organization,
    streetAddress = streetAddress,
    zipOrPostalCode = zipOrPostalCode,
    city = city,
    stateOrProvince = stateOrProvince,
    countryOrRegion = countryOrRegion,
    floor = floor,
    county = county,
    customFields = customFields.mapNotNull { it.toContent(encryptionContext, true) }
)

private fun ContactDetails.toContent(encryptionContext: EncryptionContext) = ContactDetailsContent(
    socialSecurityNumber = socialSecurityNumber,
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
    customFields = customFields.mapNotNull { it.toContent(encryptionContext, true) }
)

private fun WorkDetails.toContent(encryptionContext: EncryptionContext) = WorkDetailsContent(
    company = company,
    jobTitle = jobTitle,
    personalWebsite = personalWebsite,
    workPhoneNumber = workPhoneNumber,
    workEmail = workEmail,
    customFields = customFields.mapNotNull { it.toContent(encryptionContext, true) }
)

private fun ExtraSection.toContent(encryptionContext: EncryptionContext) = ExtraSectionContent(
    title = sectionName,
    customFields = customFields.mapNotNull { it.toContent(encryptionContext, true) }
)

private fun createCustom(
    encryptionContext: EncryptionContext,
    title: String,
    note: String,
    type: ItemType.Custom
) = ItemContents.Custom(
    title = encryptionContext.decrypt(title),
    note = encryptionContext.decrypt(note),
    sectionContentList = type.extraSections.map { it.toContent(encryptionContext) }
)

private fun concealedOrEmpty(value: String, encryptionContext: EncryptionContext): HiddenState {
    if (value.isEmpty()) return HiddenState.Empty(value)
    val asByteArray = encryptionContext.decrypt(value.toEncryptedByteArray())
    return if (asByteArray.isEmpty()) {
        HiddenState.Empty(value)
    } else {
        HiddenState.Concealed(value)
    }
}
