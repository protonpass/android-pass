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
import kotlinx.coroutines.flow.update
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.some
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IdentityFieldDraftRepositoryImpl @Inject constructor() : IdentityFieldDraftRepository {

    private val extraFieldsStateFlow = MutableStateFlow<Set<ExtraField>>(emptySet())
    private val lastAddedExtraFieldStateFlow = MutableStateFlow<Option<ExtraField>>(None)
    private val lastAddedExtraFieldIndexStateFlow = MutableStateFlow<Option<Int>>(None)

    private fun getAvailableFieldsMap(sectionIndex: Int): Map<Class<out ExtraField>, Set<ExtraField>> = mapOf(
        PersonalDetailsField::class.java to setOf(
            FirstName,
            MiddleName,
            LastName,
            Birthdate,
            Gender,
            PersonalCustomField
        ),
        AddressDetailsField::class.java to setOf(
            Floor,
            County,
            AddressCustomField
        ),
        ContactDetailsField::class.java to setOf(
            Linkedin,
            Reddit,
            Facebook,
            Yahoo,
            Instagram,
            ContactCustomField
        ),
        WorkDetailsField::class.java to setOf(
            PersonalWebsite,
            WorkPhoneNumber,
            WorkEmail,
            WorkCustomField
        ),
        ExtraSectionField::class.java to setOf(
            ExtraSectionCustomField(sectionIndex)
        )
    )

    @Suppress("UNCHECKED_CAST")
    override fun <T : ExtraField> getSectionFields(clazz: Class<T>, extraSectionIndex: Int): Set<T> {
        val available = getAvailableFieldsMap(extraSectionIndex)[clazz] as? Set<T> ?: emptySet()
        val selected = extraFieldsStateFlow.value.filterIsInstance(clazz).toSet()
        val selectedWithoutCustomField =
            selected - available.filter { it is CustomExtraField }.toSet()
        return available - selectedWithoutCustomField
    }

    override fun observeExtraFields(): Flow<Set<ExtraField>> = extraFieldsStateFlow.asStateFlow()

    override fun observeLastAddedExtraField(): Flow<Option<FocusedField>> = combine(
        lastAddedExtraFieldStateFlow,
        lastAddedExtraFieldIndexStateFlow
    ) { field, index ->
        when {
            field is Some && index is Some -> FocusedField(index.value, field.value).some()
            field is Some && field.value !is CustomExtraField -> FocusedField(0, field.value).some()
            else -> None
        }
    }

    override fun resetLastAddedExtraField() {
        lastAddedExtraFieldStateFlow.update { None }
        lastAddedExtraFieldIndexStateFlow.update { None }
    }

    override fun addField(extraField: ExtraField, focus: Boolean) {
        extraFieldsStateFlow.update { it + extraField }
        if (focus) {
            lastAddedExtraFieldStateFlow.update { extraField.some() }
        }
    }

    override fun addCustomFieldIndex(index: Int) {
        lastAddedExtraFieldIndexStateFlow.update { index.some() }
    }

    override fun clearAddedFields() {
        extraFieldsStateFlow.update { emptySet() }
        lastAddedExtraFieldStateFlow.update { None }
        lastAddedExtraFieldIndexStateFlow.update { None }
    }
}
