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

package proton.android.pass.features.itemcreate.identity.presentation.bottomsheets

sealed interface ExtraField

sealed interface CustomExtraField

sealed interface PersonalDetailsField : ExtraField

data object FirstName : PersonalDetailsField

data object MiddleName : PersonalDetailsField

data object LastName : PersonalDetailsField

data object Birthdate : PersonalDetailsField

data object Gender : PersonalDetailsField

data object PersonalCustomField : PersonalDetailsField, CustomExtraField

sealed interface AddressDetailsField : ExtraField

data object Floor : AddressDetailsField

data object County : AddressDetailsField

data object AddressCustomField : AddressDetailsField, CustomExtraField

sealed interface ContactDetailsField : ExtraField

data object Linkedin : ContactDetailsField

data object Reddit : ContactDetailsField

data object Facebook : ContactDetailsField

data object Yahoo : ContactDetailsField

data object Instagram : ContactDetailsField

data object ContactCustomField : ContactDetailsField, CustomExtraField

sealed interface WorkDetailsField : ExtraField

data object PersonalWebsite : WorkDetailsField

data object WorkPhoneNumber : WorkDetailsField

data object WorkEmail : WorkDetailsField

data object WorkCustomField : WorkDetailsField, CustomExtraField

sealed interface ExtraSectionField : ExtraField

data class ExtraSectionCustomField(val index: Int) : ExtraSectionField, CustomExtraField
