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

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import proton.android.pass.domain.CustomFieldType
import proton.android.pass.features.itemcreate.identity.presentation.IdentityField
import proton.android.pass.features.itemcreate.identity.presentation.section
import proton.android.pass.features.itemcreate.identity.ui.IdentitySectionType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IdentityFieldDraftRepositoryImpl @Inject constructor() : IdentityFieldDraftRepository {

    private val extraFieldsStateFlow: MutableStateFlow<Set<IdentityField>> =
        MutableStateFlow<Set<IdentityField>>(emptySet())

    private fun getAvailableFieldsMap(sectionIndex: Int): Map<IdentitySectionType, Set<IdentityField>> = mapOf(
        IdentitySectionType.PersonalDetails to setOf(
            IdentityField.FirstName,
            IdentityField.MiddleName,
            IdentityField.LastName,
            IdentityField.Birthdate,
            IdentityField.Gender,
            IdentityField.CustomField(
                sectionType = IdentitySectionType.PersonalDetails,
                customFieldType = CustomFieldType.Text, // Placeholder type
                index = 0 // Placeholder index
            )
        ),
        IdentitySectionType.AddressDetails to setOf(
            IdentityField.Floor,
            IdentityField.County,
            IdentityField.CustomField(
                sectionType = IdentitySectionType.AddressDetails,
                customFieldType = CustomFieldType.Text, // Placeholder type
                index = 0 // Placeholder index
            )
        ),
        IdentitySectionType.ContactDetails to setOf(
            IdentityField.Linkedin,
            IdentityField.Reddit,
            IdentityField.Facebook,
            IdentityField.Yahoo,
            IdentityField.Instagram,
            IdentityField.CustomField(
                sectionType = IdentitySectionType.ContactDetails,
                customFieldType = CustomFieldType.Text, // Placeholder type
                index = 0 // Placeholder index
            )
        ),
        IdentitySectionType.WorkDetails to setOf(
            IdentityField.PersonalWebsite,
            IdentityField.WorkPhoneNumber,
            IdentityField.WorkEmail,
            IdentityField.CustomField(
                sectionType = IdentitySectionType.WorkDetails,
                customFieldType = CustomFieldType.Text, // Placeholder type
                index = 0 // Placeholder index
            )
        ),
        IdentitySectionType.ExtraSection(sectionIndex) to setOf(
            IdentityField.CustomField(
                sectionType = IdentitySectionType.ExtraSection(sectionIndex),
                customFieldType = CustomFieldType.Text, // Placeholder type
                index = 0 // Placeholder index
            )
        )
    )

    override fun observeSectionFields(section: IdentitySectionType, sectionIndex: Int): Flow<Set<IdentityField>> =
        combine(
            flowOf(getAvailableFieldsMap(sectionIndex)).map { it[section] ?: emptySet() },
            extraFieldsStateFlow.map { it.filter { it.section() == section } }
        ) { available, selected ->
            val selectedWithoutCustomField =
                selected - available.filter { it is IdentityField.CustomField }.toSet()
            available - selectedWithoutCustomField
        }

    override fun observeExtraFields(): Flow<Set<IdentityField>> = extraFieldsStateFlow.asStateFlow()

    override fun addField(extraField: IdentityField, focus: Boolean) {
        extraFieldsStateFlow.update { it + extraField }
    }

    override fun clearAddedFields() {
        extraFieldsStateFlow.update { emptySet() }
    }
}
