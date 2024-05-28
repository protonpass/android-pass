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

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IdentityFieldDraftRepositoryImpl @Inject constructor() : IdentityFieldDraftRepository {

    private val availableFieldsMap: Map<Class<out ExtraField>, Set<ExtraField>> = mapOf(
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
        )
    )

    private val extraFieldsStateFlow = MutableStateFlow<Set<ExtraField>>(emptySet())

    @Suppress("UNCHECKED_CAST")
    override fun <T : ExtraField> getSectionFields(clazz: Class<T>): Set<T> {
        val available = availableFieldsMap[clazz] as? Set<T> ?: emptySet()
        val selected = extraFieldsStateFlow.value.filterIsInstance(clazz).toSet()
        val selectedWithoutCustomField =
            selected - available.filter { it is CustomExtraField }.toSet()
        return available - selectedWithoutCustomField
    }

    override fun observeExtraFields(): Flow<Set<ExtraField>> = extraFieldsStateFlow.asStateFlow()

    override fun addField(extraField: ExtraField) {
        extraFieldsStateFlow.value += extraField
    }

    override fun clearAddedFields() {
        extraFieldsStateFlow.value = emptySet()
    }
}
