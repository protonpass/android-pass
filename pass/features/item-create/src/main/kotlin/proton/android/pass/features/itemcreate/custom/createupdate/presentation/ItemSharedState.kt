/*
 * Copyright (c) 2025 Proton AG
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

package proton.android.pass.features.itemcreate.custom.createupdate.presentation

import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentSetOf
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.commonuimodels.api.attachments.AttachmentsState
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.features.itemcreate.ItemSavedState
import proton.android.pass.features.itemcreate.common.ValidationError
import proton.android.pass.features.itemcreate.common.customfields.CustomFieldIdentifier

data class ItemSharedUiState(
    val isLoadingState: IsLoadingState,
    val hasUserEditedContent: Boolean,
    val validationErrors: PersistentSet<ValidationError>,
    val isItemSaved: ItemSavedState,
    val focusedField: Option<CustomFieldIdentifier>,
    val canCreateItem: Boolean,
    val displayFileAttachmentsOnboarding: Boolean,
    val attachmentsState: AttachmentsState
) {

    val showFileAttachmentsBanner = displayFileAttachmentsOnboarding

    companion object {
        val Initial = ItemSharedUiState(
            isLoadingState = IsLoadingState.NotLoading,
            hasUserEditedContent = false,
            validationErrors = persistentSetOf(),
            isItemSaved = ItemSavedState.Unknown,
            focusedField = None,
            canCreateItem = false,
            displayFileAttachmentsOnboarding = false,
            attachmentsState = AttachmentsState.Initial
        )
    }
}
