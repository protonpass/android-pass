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
        PersonalDetailsField::class.java to PersonalDetailsField.entries.toSet(),
        AddressDetailsField::class.java to AddressDetailsField.entries.toSet(),
        ContactDetailsField::class.java to ContactDetailsField.entries.toSet(),
        WorkDetailsField::class.java to WorkDetailsField.entries.toSet()
    )

    private val extraFieldsStateFlow = MutableStateFlow<Set<ExtraField>>(emptySet())

    override fun <T : ExtraField> getSectionFields(clazz: Class<T>): Set<T> {
        @Suppress("UNCHECKED_CAST")
        return (availableFieldsMap[clazz] as? Set<T> ?: emptySet()) -
            extraFieldsStateFlow.value.filterIsInstance(clazz).toSet()
    }

    override fun observeExtraFields(): Flow<Set<ExtraField>> = extraFieldsStateFlow.asStateFlow()

    override fun addField(extraField: ExtraField) {
        extraFieldsStateFlow.value += extraField
    }

    override fun clearAddedFields() {
        extraFieldsStateFlow.value = emptySet()
    }
}
