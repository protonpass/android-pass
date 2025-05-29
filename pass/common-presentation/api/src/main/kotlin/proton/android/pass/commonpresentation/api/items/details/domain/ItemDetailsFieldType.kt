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

package proton.android.pass.commonpresentation.api.items.details.domain

import proton.android.pass.domain.HiddenState

sealed interface ItemDetailsFieldType {

    sealed interface Copyable : ItemDetailsFieldType {

        val text: String

        data class Alias(override val text: String) : Copyable

        data class BirthDate(override val text: String) : Copyable

        data class CardNumber(override val text: String) : Copyable

        data class City(override val text: String) : Copyable

        data class Company(override val text: String) : Copyable

        data class CountryOrRegion(override val text: String) : Copyable

        data class County(override val text: String) : Copyable

        data class CustomField(override val text: String) : Copyable

        data class Email(override val text: String) : Copyable

        data class Facebook(override val text: String) : Copyable

        data class FirstName(override val text: String) : Copyable

        data class Floor(override val text: String) : Copyable

        data class FullName(override val text: String) : Copyable

        data class Gender(override val text: String) : Copyable

        data class Instagram(override val text: String) : Copyable

        data class LastName(override val text: String) : Copyable

        data class LicenseNumber(override val text: String) : Copyable

        data class LinkedIn(override val text: String) : Copyable

        data class MiddleName(override val text: String) : Copyable

        data class Occupation(override val text: String) : Copyable

        data class Organization(override val text: String) : Copyable

        data class PassportNumber(override val text: String) : Copyable

        data class PhoneNumber(override val text: String) : Copyable

        data class Reddit(override val text: String) : Copyable

        data class StateOrProvince(override val text: String) : Copyable

        data class StreetAddress(override val text: String) : Copyable

        data class TotpCode(override val text: String) : Copyable

        data class Username(override val text: String) : Copyable

        data class Website(override val text: String) : Copyable

        data class XHandle(override val text: String) : Copyable

        data class Yahoo(override val text: String) : Copyable

        data class ZipOrPostalCode(override val text: String) : Copyable

        data class PublicKey(override val text: String) : Copyable

        data class SSID(override val text: String) : Copyable
    }

    sealed interface Hidden : ItemDetailsFieldType {

        val hiddenState: HiddenState

        data class CustomField(override val hiddenState: HiddenState, val index: Int) : Hidden

        data class Cvv(override val hiddenState: HiddenState) : Hidden

        data class Password(override val hiddenState: HiddenState) : Hidden

        data class Pin(override val hiddenState: HiddenState) : Hidden

        data class PrivateKey(override val hiddenState: HiddenState) : Hidden

        data class SocialSecurityNumber(override val hiddenState: HiddenState) : Hidden

    }

}
