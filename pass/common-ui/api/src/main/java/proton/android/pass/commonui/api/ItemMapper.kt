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
import proton.android.pass.datamodels.api.toContent
import proton.android.pass.domain.AddressDetailsContent
import proton.android.pass.domain.ContactDetailsContent
import proton.android.pass.domain.ExtraSectionContent
import proton.android.pass.domain.HiddenState
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemType
import proton.android.pass.domain.PersonalDetailsContent
import proton.android.pass.domain.WorkDetailsContent

fun Item.toUiModel(context: EncryptionContext): ItemUiModel = ItemUiModel(
    id = id,
    shareId = shareId,
    userId = userId,
    contents = toItemContents(context),
    state = state,
    createTime = createTime,
    modificationTime = modificationTime,
    lastAutofillTime = lastAutofillTime.value(),
    isPinned = isPinned,
    category = itemType.category,
    revision = revision
)

fun Item.itemName(context: EncryptionContext): String = context.decrypt(title)

fun Item.toItemContents(encryptionContext: EncryptionContext): ItemContents = when (val type = itemType) {
    is ItemType.Alias -> createAlias(encryptionContext, type)
    is ItemType.Login -> createLogin(encryptionContext, type)
    is ItemType.Note -> createNote(encryptionContext)
    is ItemType.CreditCard -> createCreditCard(encryptionContext, type)
    is ItemType.Identity -> createIdentity(encryptionContext, type)

    ItemType.Password,
    ItemType.Unknown -> ItemContents.Unknown(
        title = encryptionContext.decrypt(title),
        note = encryptionContext.decrypt(note)
    )
}

private fun Item.createAlias(encryptionContext: EncryptionContext, type: ItemType.Alias) = ItemContents.Alias(
    title = encryptionContext.decrypt(title),
    note = encryptionContext.decrypt(note),
    aliasEmail = type.aliasEmail,
    isDisabled = isAliasDisabled
)

private fun Item.createLogin(encryptionContext: EncryptionContext, type: ItemType.Login) = ItemContents.Login(
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

private fun Item.createNote(encryptionContext: EncryptionContext) = ItemContents.Note(
    title = encryptionContext.decrypt(title),
    note = encryptionContext.decrypt(note)
)

private fun Item.createCreditCard(encryptionContext: EncryptionContext, type: ItemType.CreditCard) =
    ItemContents.CreditCard(
        title = encryptionContext.decrypt(title),
        note = encryptionContext.decrypt(note),
        type = type.creditCardType,
        cardHolder = type.cardHolder,
        number = encryptionContext.decrypt(type.number),
        cvv = concealedOrEmpty(type.cvv, encryptionContext),
        pin = concealedOrEmpty(type.pin, encryptionContext),
        expirationDate = type.expirationDate
    )

@Suppress("LongMethod")
private fun Item.createIdentity(encryptionContext: EncryptionContext, type: ItemType.Identity) = ItemContents.Identity(
    title = encryptionContext.decrypt(title),
    note = encryptionContext.decrypt(note),
    personalDetailsContent = PersonalDetailsContent(
        fullName = type.personalDetails.fullName,
        firstName = type.personalDetails.firstName,
        middleName = type.personalDetails.middleName,
        lastName = type.personalDetails.lastName,
        birthdate = type.personalDetails.birthdate,
        gender = type.personalDetails.gender,
        email = type.personalDetails.email,
        phoneNumber = type.personalDetails.phoneNumber,
        customFields = type.personalDetails.customFields.mapNotNull {
            it.toContent(encryptionContext, true)
        }
    ),
    addressDetailsContent = AddressDetailsContent(
        organization = type.addressDetails.organization,
        streetAddress = type.addressDetails.streetAddress,
        zipOrPostalCode = type.addressDetails.zipOrPostalCode,
        city = type.addressDetails.city,
        stateOrProvince = type.addressDetails.stateOrProvince,
        countryOrRegion = type.addressDetails.countryOrRegion,
        floor = type.addressDetails.floor,
        county = type.addressDetails.county,
        customFields = type.addressDetails.customFields.mapNotNull {
            it.toContent(encryptionContext, true)
        }
    ),
    contactDetailsContent = ContactDetailsContent(
        socialSecurityNumber = type.contactDetails.socialSecurityNumber,
        passportNumber = type.contactDetails.passportNumber,
        licenseNumber = type.contactDetails.licenseNumber,
        website = type.contactDetails.website,
        xHandle = type.contactDetails.xHandle,
        secondPhoneNumber = type.contactDetails.secondPhoneNumber,
        linkedin = type.contactDetails.linkedin,
        reddit = type.contactDetails.reddit,
        facebook = type.contactDetails.facebook,
        yahoo = type.contactDetails.yahoo,
        instagram = type.contactDetails.instagram,
        customFields = type.contactDetails.customFields.mapNotNull {
            it.toContent(encryptionContext, true)
        }
    ),
    workDetailsContent = WorkDetailsContent(
        company = type.workDetails.company,
        jobTitle = type.workDetails.jobTitle,
        personalWebsite = type.workDetails.personalWebsite,
        workPhoneNumber = type.workDetails.workPhoneNumber,
        workEmail = type.workDetails.workEmail,
        customFields = type.workDetails.customFields.mapNotNull {
            it.toContent(encryptionContext, true)
        }
    ),
    extraSectionContentList = type.extraSections.map {
        ExtraSectionContent(
            title = it.sectionName,
            customFields = it.customFields.mapNotNull { customField ->
                customField.toContent(
                    encryptionContext,
                    true
                )
            }
        )
    }
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
