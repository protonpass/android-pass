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

import proton.android.pass.types.CustomSection as LibraryCustomSection
import proton.android.pass.types.IdentityItem as LibraryIdentityItem
import proton.android.pass.types.ItemContent as LibraryItemContent
import proton.android.pass.types.ItemData as LibraryItemData
import proton.android.pass.types.ItemExtraField as LibraryItemExtraField
import proton.android.pass.types.ItemExtraFieldContent as LibraryItemExtraFieldContent

internal fun LibraryItemData.searchableContent(): List<String> = buildList {
    addExtraFields(extraFields)
    when (val itemContent = content) {
        is LibraryItemContent.Login -> {
            addIfNotBlank(itemContent.v1.email)
            addIfNotBlank(itemContent.v1.username)
            itemContent.v1.urls.forEach(::addIfNotBlank)
        }
        is LibraryItemContent.CreditCard -> addIfNotBlank(itemContent.v1.cardholderName)
        is LibraryItemContent.Identity -> addIdentity(itemContent.v1)
        is LibraryItemContent.Wifi -> {
            addIfNotBlank(itemContent.v1.ssid)
            addSections(itemContent.v1.sections)
        }
        is LibraryItemContent.SshKey -> addSections(itemContent.v1.sections)
        is LibraryItemContent.Custom -> addSections(itemContent.v1.sections)
        is LibraryItemContent.Note,
        is LibraryItemContent.Alias -> Unit
    }
}

private fun MutableList<String>.addIdentity(identity: LibraryIdentityItem) {
    listOf(
        identity.fullName,
        identity.firstName,
        identity.middleName,
        identity.lastName,
        identity.birthdate,
        identity.gender,
        identity.email,
        identity.phoneNumber
    ).forEach(::addIfNotBlank)
    addExtraFields(identity.extraPersonalDetails)
    listOf(
        identity.organization,
        identity.streetAddress,
        identity.zipOrPostalCode,
        identity.city,
        identity.stateOrProvince,
        identity.countryOrRegion,
        identity.floor,
        identity.county
    ).forEach(::addIfNotBlank)
    addExtraFields(identity.extraAddressDetails)
    listOf(
        identity.passportNumber,
        identity.licenseNumber,
        identity.website,
        identity.xHandle,
        identity.secondPhoneNumber,
        identity.linkedin,
        identity.reddit,
        identity.facebook,
        identity.yahoo,
        identity.instagram
    ).forEach(::addIfNotBlank)
    addExtraFields(identity.extraContactDetails)
    listOf(
        identity.company,
        identity.jobTitle,
        identity.personalWebsite,
        identity.workPhoneNumber,
        identity.workEmail
    ).forEach(::addIfNotBlank)
    addExtraFields(identity.extraWorkDetails)
    addSections(identity.extraSections)
}

private fun MutableList<String>.addSections(sections: List<LibraryCustomSection>) {
    sections.forEach { addExtraFields(it.sectionFields) }
}

private fun MutableList<String>.addExtraFields(fields: List<LibraryItemExtraField>) {
    fields.forEach { field ->
        addIfNotBlank(field.name)
        when (val fieldContent = field.content) {
            is LibraryItemExtraFieldContent.Text -> addIfNotBlank(fieldContent.v1)
            is LibraryItemExtraFieldContent.Hidden,
            is LibraryItemExtraFieldContent.Totp,
            is LibraryItemExtraFieldContent.Timestamp -> Unit
        }
    }
}

private fun MutableList<String>.addIfNotBlank(value: String) {
    if (value.isNotBlank()) add(value)
}
