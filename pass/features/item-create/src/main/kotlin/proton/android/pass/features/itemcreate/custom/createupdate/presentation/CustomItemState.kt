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

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentSetOf
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.some
import proton.android.pass.commonuimodels.api.attachments.AttachmentsState
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.Vault
import proton.android.pass.features.itemcreate.ItemSavedState
import proton.android.pass.features.itemcreate.common.ShareUiState
import proton.android.pass.features.itemcreate.common.ValidationError
import proton.android.pass.features.itemcreate.common.customfields.CustomFieldIdentifier

sealed interface CustomItemState : ItemSharedProperties {
    @Immutable
    data object NotInitialised : CustomItemState {
        override val sharedState: ItemSharedUiState = EMPTY_SHARED_STATE
    }

    @Immutable
    data object Loading : CustomItemState {
        override val sharedState: ItemSharedUiState = EMPTY_SHARED_STATE
    }

    @Immutable
    data object Error : CustomItemState {
        override val sharedState: ItemSharedUiState = EMPTY_SHARED_STATE
    }

    @Immutable
    data class CreateCustomItemState(
        val shareUiState: ShareUiState,
        override val sharedState: ItemSharedUiState,
        override val canDisplayVaultSharedWarningDialog: Boolean
    ) : CustomItemState {

        override val shouldShowVaultSelector: Boolean
            get() = shareUiState is ShareUiState.Success && shareUiState.vaultList.size > 1

        override val selectedVault: Option<Vault>
            get() = if (shareUiState is ShareUiState.Success) {
                shareUiState.currentVault.vault.some()
            } else {
                None
            }

        override val selectedShareId: Option<ShareId>
            get() = selectedVault.map { it.shareId }
    }

    @Immutable
    data class UpdateCustomItemState(
        override val selectedShareId: Option<ShareId>,
        override val sharedState: ItemSharedUiState,
        override val canDisplayVaultSharedWarningDialog: Boolean,
        override val canDisplaySharedItemWarningDialog: Boolean
    ) : CustomItemState
}

interface ItemSharedProperties {
    val sharedState: ItemSharedUiState

    val hasUserEdited: Boolean
        get() = sharedState.hasUserEditedContent

    val shouldShowVaultSelector: Boolean
        get() = false

    val selectedVault: Option<Vault>
        get() = None

    val selectedShareId: Option<ShareId>
        get() = None

    val canDisplayVaultSharedWarningDialog: Boolean
        get() = false

    val canDisplaySharedItemWarningDialog: Boolean
        get() = false

    val itemSavedState: ItemSavedState
        get() = sharedState.isItemSaved

    val isFormEnabled: Boolean
        get() = !isLoading

    val isLoading: Boolean
        get() = sharedState.isLoadingState.value()

    val validationErrors: PersistentSet<ValidationError>
        get() = sharedState.validationErrors

    val focusedField: Option<CustomFieldIdentifier>
        get() = sharedState.focusedField

    val canCreateItem: Boolean
        get() = sharedState.canCreateItem

    val showFileAttachmentsBanner: Boolean
        get() = sharedState.showFileAttachmentsBanner

    val attachmentsState: AttachmentsState
        get() = sharedState.attachmentsState
}

val EMPTY_SHARED_STATE = ItemSharedUiState(
    hasUserEditedContent = false,
    isItemSaved = ItemSavedState.Unknown,
    isLoadingState = IsLoadingState.NotLoading,
    validationErrors = persistentSetOf(),
    canCreateItem = false,
    displayFileAttachmentsOnboarding = false,
    focusedField = None,
    attachmentsState = AttachmentsState.Initial
)
