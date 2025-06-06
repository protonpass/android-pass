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
import proton.android.pass.common.api.Option

data class FocusedField(val index: Int, val extraField: ExtraField)

interface IdentityFieldDraftRepository {
    fun <T : ExtraField> getSectionFields(clazz: Class<T>, extraSectionIndex: Int): Set<T>
    fun observeExtraFields(): Flow<Set<ExtraField>>
    fun addField(extraField: ExtraField, focus: Boolean)
    fun clearAddedFields()
    fun observeLastAddedExtraField(): Flow<Option<FocusedField>>
    fun addCustomFieldIndex(index: Int)
}
