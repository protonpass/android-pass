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

sealed interface ItemDetailsFieldType {

    sealed interface Plain : ItemDetailsFieldType {

        data object Alias : Plain

        data object BirthDate : Plain

        data object CardNumber : Plain

        data object City : Plain

        data object Company : Plain

        data object CountryOrRegion : Plain

        data object County : Plain

        data object CustomField : Plain

        data object Email : Plain

        data object Facebook : Plain

        data object FirstName : Plain

        data object Floor : Plain

        data object FullName : Plain

        data object Gender : Plain

        data object Instagram : Plain

        data object LastName : Plain

        data object LicenseNumber : Plain

        data object LinkedIn : Plain

        data object MiddleName : Plain

        data object Occupation : Plain

        data object Organization : Plain

        data object PassportNumber : Plain

        data object PhoneNumber : Plain

        data object Reddit : Plain

        data object StateOrProvince : Plain

        data object StreetAddress : Plain

        data object TotpCode : Plain

        data object Username : Plain

        data object Website : Plain

        data object XHandle : Plain

        data object Yahoo : Plain

        data object ZipOrPostalCode : Plain

        data object PublicKey : Plain

        data object SSID : Plain
    }

    sealed interface Hidden : ItemDetailsFieldType {

        @JvmInline
        value class CustomField(val index: Int) : Hidden

        data object Cvv : Hidden

        data object Password : Hidden

        data object Pin : Hidden

        data object PrivateKey : Hidden

        data object SocialSecurityNumber : Hidden

    }

}
