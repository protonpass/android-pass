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

package proton.android.pass.features.itemcreate.common

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.some
import proton.android.pass.features.itemcreate.custom.createupdate.ui.CustomField
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomItemFieldDraftRepositoryImpl @Inject constructor() : CustomItemFieldDraftRepository {

    private val customFieldsStateFlow = MutableStateFlow<Set<CustomField>>(emptySet())
    private val lastAddedCustomFieldStateFlow = MutableStateFlow<Option<CustomField>>(None)
    private val lastAddedCustomFieldIndexStateFlow = MutableStateFlow<Option<Int>>(None)

    override fun observeCustomFields(): Flow<Set<CustomField>> = customFieldsStateFlow.asStateFlow()

    override fun observeLastAddedCustomField(): Flow<Option<FocusedField>> = combine(
        lastAddedCustomFieldStateFlow,
        lastAddedCustomFieldIndexStateFlow
    ) { field, index ->
        when {
            field is Some && index is Some ->
                FocusedField(index.value, field.value.sectionIndex).some()

            else -> None
        }
    }

    override fun resetLastAddedCustomField() {
        lastAddedCustomFieldStateFlow.update { None }
        lastAddedCustomFieldIndexStateFlow.update { None }
    }

    override fun addField(extraField: CustomField, focus: Boolean) {
        customFieldsStateFlow.update { it + extraField }
        if (focus) {
            lastAddedCustomFieldStateFlow.update { extraField.some() }
        }
    }

    override fun addCustomFieldIndex(index: Int) {
        lastAddedCustomFieldIndexStateFlow.update { index.some() }
    }

    override fun clearAddedFields() {
        customFieldsStateFlow.update { emptySet() }
        lastAddedCustomFieldStateFlow.update { None }
        lastAddedCustomFieldIndexStateFlow.update { None }
    }
}
