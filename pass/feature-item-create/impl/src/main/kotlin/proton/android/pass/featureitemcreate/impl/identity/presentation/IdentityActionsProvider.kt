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

package proton.android.pass.featureitemcreate.impl.identity.presentation

import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.domain.Item
import proton.android.pass.featureitemcreate.impl.ItemSavedState
import proton.android.pass.featureitemcreate.impl.common.CustomFieldIndexTitle
import proton.android.pass.featureitemcreate.impl.identity.presentation.bottomsheets.CustomExtraField
import proton.android.pass.featureitemcreate.impl.identity.presentation.bottomsheets.ExtraField
import proton.android.pass.featureitemcreate.impl.identity.presentation.bottomsheets.FocusedField

@Suppress("ComplexInterface", "TooManyFunctions")
interface IdentityFormActions {
    fun onFieldChange(field: FieldChange)
    fun observeDraftChanges(coroutineScope: CoroutineScope)
    fun onRenameCustomField(value: CustomFieldIndexTitle, customExtraField: CustomExtraField)
    fun getFormState(): IdentityItemFormState
    fun isFormStateValid(): Boolean
    fun clearState()
}

interface IdentityActionsProvider : IdentityFormActions {
    fun observeSharedState(): Flow<IdentitySharedUiState>
    fun updateLoadingState(loadingState: IsLoadingState)
    fun onItemSavedState(item: Item)
    fun updateSelectedSection(customExtraField: CustomExtraField)
    fun onItemReceivedState(item: Item)
    fun getReceivedItem(): Item
    fun resetLastAddedFieldFocus()
}

data class IdentitySharedUiState(
    val isLoadingState: IsLoadingState,
    val hasUserEditedContent: Boolean,
    val validationErrors: PersistentSet<IdentityValidationErrors>,
    val isItemSaved: ItemSavedState,
    val extraFields: PersistentSet<ExtraField>,
    val focusedField: Option<FocusedField>
) {
    companion object {
        val Initial = IdentitySharedUiState(
            isLoadingState = IsLoadingState.NotLoading,
            hasUserEditedContent = false,
            validationErrors = persistentSetOf(),
            isItemSaved = ItemSavedState.Unknown,
            extraFields = persistentSetOf(),
            focusedField = None
        )
    }
}
