/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.features.itemcreate.identity.presentation

import proton.android.pass.domain.CustomFieldType
import proton.android.pass.features.itemcreate.identity.ui.IdentitySectionType

sealed interface IdentityField {
    data object Title : IdentityField
    data object FullName : IdentityField
    data object FirstName : IdentityField
    data object MiddleName : IdentityField
    data object LastName : IdentityField
    data object Birthdate : IdentityField
    data object Gender : IdentityField
    data object Email : IdentityField
    data object PhoneNumber : IdentityField
    data object Organization : IdentityField
    data object StreetAddress : IdentityField
    data object ZipOrPostalCode : IdentityField
    data object City : IdentityField
    data object StateOrProvince : IdentityField
    data object CountryOrRegion : IdentityField
    data object SocialSecurityNumber : IdentityField
    data object PassportNumber : IdentityField
    data object LicenseNumber : IdentityField
    data object Website : IdentityField
    data object XHandle : IdentityField
    data object SecondPhoneNumber : IdentityField
    data object Company : IdentityField
    data object JobTitle : IdentityField
    data object County : IdentityField
    data object Facebook : IdentityField
    data object Floor : IdentityField
    data object Instagram : IdentityField
    data object Linkedin : IdentityField
    data object PersonalWebsite : IdentityField
    data object WorkEmail : IdentityField
    data object WorkPhoneNumber : IdentityField
    data object Yahoo : IdentityField
    data object Reddit : IdentityField

    data class CustomField(
        val sectionType: IdentitySectionType,
        val customFieldType: CustomFieldType,
        val index: Int
    ) : IdentityField
}

fun IdentityField.section(): IdentitySectionType = when (this) {
    IdentityField.FirstName,
    IdentityField.MiddleName,
    IdentityField.LastName,
    IdentityField.Birthdate,
    IdentityField.Gender,
    IdentityField.Title,
    IdentityField.FullName,
    IdentityField.SocialSecurityNumber,
    IdentityField.PassportNumber,
    IdentityField.LicenseNumber -> IdentitySectionType.PersonalDetails

    IdentityField.Floor,
    IdentityField.County,
    IdentityField.StreetAddress,
    IdentityField.City,
    IdentityField.StateOrProvince,
    IdentityField.ZipOrPostalCode,
    IdentityField.CountryOrRegion -> IdentitySectionType.AddressDetails

    IdentityField.Facebook,
    IdentityField.Linkedin,
    IdentityField.Reddit,
    IdentityField.Yahoo,
    IdentityField.Instagram,
    IdentityField.XHandle,
    IdentityField.PhoneNumber,
    IdentityField.SecondPhoneNumber,
    IdentityField.Email -> IdentitySectionType.ContactDetails

    IdentityField.WorkEmail,
    IdentityField.WorkPhoneNumber,
    IdentityField.JobTitle,
    IdentityField.Company,
    IdentityField.PersonalWebsite,
    IdentityField.Website,
    IdentityField.Organization -> IdentitySectionType.WorkDetails

    is IdentityField.CustomField -> this.sectionType
}
