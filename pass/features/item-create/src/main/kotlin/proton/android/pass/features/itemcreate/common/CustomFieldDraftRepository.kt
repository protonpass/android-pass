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
import proton.android.pass.common.api.Option
import proton.android.pass.domain.CustomFieldType

sealed interface DraftFormEvent

sealed interface DraftFormSectionEvent : DraftFormEvent {
    data class SectionAdded(val label: String) : DraftFormSectionEvent
    data class SectionRemoved(val index: Int) : DraftFormSectionEvent
    data class SectionRenamed(val index: Int, val newLabel: String) : DraftFormSectionEvent
}

sealed interface DraftFormFieldEvent : DraftFormEvent {
    data class FieldAdded(
        val sectionIndex: Option<Int>,
        val label: String,
        val type: CustomFieldType
    ) : DraftFormFieldEvent
    data class FieldRemoved(val sectionIndex: Option<Int>, val index: Int) : DraftFormFieldEvent
    data class FieldRenamed(
        val sectionIndex: Option<Int>,
        val index: Int,
        val newLabel: String
    ) : DraftFormFieldEvent
}

interface CustomFieldDraftRepository {
    fun observeAllEvents(): Flow<DraftFormEvent>
    fun observeCustomFieldEvents(): Flow<DraftFormFieldEvent>
    suspend fun emit(event: DraftFormEvent)
}
