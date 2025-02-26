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

sealed interface FieldChange {
    @JvmInline
    value class Title(val value: String) : FieldChange

    @JvmInline
    value class FullName(val value: String) : FieldChange

    @JvmInline
    value class FirstName(val value: String) : FieldChange

    @JvmInline
    value class MiddleName(val value: String) : FieldChange

    @JvmInline
    value class LastName(val value: String) : FieldChange

    @JvmInline
    value class Birthdate(val value: String) : FieldChange

    @JvmInline
    value class Gender(val value: String) : FieldChange

    @JvmInline
    value class Email(val value: String) : FieldChange

    @JvmInline
    value class PhoneNumber(val value: String) : FieldChange

    @JvmInline
    value class Organization(val value: String) : FieldChange

    @JvmInline
    value class StreetAddress(val value: String) : FieldChange

    @JvmInline
    value class ZipOrPostalCode(val value: String) : FieldChange

    @JvmInline
    value class City(val value: String) : FieldChange

    @JvmInline
    value class StateOrProvince(val value: String) : FieldChange

    @JvmInline
    value class CountryOrRegion(val value: String) : FieldChange

    @JvmInline
    value class SocialSecurityNumber(val value: String) : FieldChange

    @JvmInline
    value class PassportNumber(val value: String) : FieldChange

    @JvmInline
    value class LicenseNumber(val value: String) : FieldChange

    @JvmInline
    value class Website(val value: String) : FieldChange

    @JvmInline
    value class XHandle(val value: String) : FieldChange

    @JvmInline
    value class SecondPhoneNumber(val value: String) : FieldChange

    @JvmInline
    value class Company(val value: String) : FieldChange

    @JvmInline
    value class JobTitle(val value: String) : FieldChange

    @JvmInline
    value class County(val value: String) : FieldChange

    @JvmInline
    value class Facebook(val value: String) : FieldChange

    @JvmInline
    value class Floor(val value: String) : FieldChange

    @JvmInline
    value class Instagram(val value: String) : FieldChange

    @JvmInline
    value class Linkedin(val value: String) : FieldChange

    @JvmInline
    value class PersonalWebsite(val value: String) : FieldChange

    @JvmInline
    value class WorkEmail(val value: String) : FieldChange

    @JvmInline
    value class WorkPhoneNumber(val value: String) : FieldChange

    @JvmInline
    value class Yahoo(val value: String) : FieldChange

    @JvmInline
    value class Reddit(val value: String) : FieldChange

    data class CustomField(
        val sectionType: IdentitySectionType,
        val customFieldType: CustomFieldType,
        val index: Int,
        val value: String
    ) : FieldChange
}


