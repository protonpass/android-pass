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

package proton.android.pass.featureitemcreate.impl.identity.presentation.bottomsheets

import proton.android.pass.featureitemcreate.impl.bottomsheets.customfield.CustomFieldType

sealed interface ExtraField

sealed interface CustomField

sealed interface PersonalDetailsField : ExtraField

data object FirstName : PersonalDetailsField

data object MiddleName : PersonalDetailsField

data object LastName : PersonalDetailsField

data object Birthdate : PersonalDetailsField

data object Gender : PersonalDetailsField

data class PersonalCustomField(val list: List<CustomFieldType>) : PersonalDetailsField, CustomField

sealed interface AddressDetailsField : ExtraField

data object Floor : AddressDetailsField

data object County : AddressDetailsField

data class AddressCustomField(val list: List<CustomFieldType>) : AddressDetailsField, CustomField

sealed interface ContactDetailsField : ExtraField

data object Linkedin : ContactDetailsField

data object Reddit : ContactDetailsField

data object Facebook : ContactDetailsField

data object Yahoo : ContactDetailsField

data object Instagram : ContactDetailsField

data class ContactCustomField(val list: List<CustomFieldType>) : ContactDetailsField, CustomField

sealed interface WorkDetailsField : ExtraField

data object PersonalWebsite : WorkDetailsField

data object WorkPhoneNumber : WorkDetailsField

data object WorkEmail : WorkDetailsField

data class WorkCustomField(val list: List<CustomFieldType>) : WorkDetailsField, CustomField
