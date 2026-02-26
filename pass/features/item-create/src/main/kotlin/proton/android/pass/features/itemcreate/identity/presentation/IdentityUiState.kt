/*
 * Copyright (c) 2023-2026 Proton AG
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

package proton.android.pass.features.itemcreate.identity.presentation

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentSetOf
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.some
import proton.android.pass.common.api.toOption
import proton.android.pass.commonuimodels.api.attachments.AttachmentsState
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.domain.FolderId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.Vault
import proton.android.pass.features.itemcreate.ItemSavedState
import proton.android.pass.features.itemcreate.common.ShareUiState
import proton.android.pass.features.itemcreate.common.ValidationError

interface IdentitySharedStateAccessor {
    fun getItemSavedState(): ItemSavedState
    fun getSubmitLoadingState(): IsLoadingState
    fun getValidationErrors(): PersistentSet<ValidationError>
    fun getExtraFields(): PersistentSet<IdentityField>
    fun getFocusedField(): Option<IdentityField>
    fun getCanUseCustomFields(): Boolean
    fun showAddPersonalDetailsButton(): Boolean
    fun showAddAddressDetailsButton(): Boolean
    fun showAddContactDetailsButton(): Boolean
    fun showAddWorkDetailsButton(): Boolean
    fun showFileAttachmentsBanner(): Boolean
    fun getAttachmentsState(): AttachmentsState
}

sealed interface IdentityUiState : IdentitySharedStateAccessor {
    @Immutable
    data object NotInitialised : IdentityUiState {
        override fun getItemSavedState() = ItemSavedState.Unknown
        override fun getSubmitLoadingState() = IsLoadingState.NotLoading
        override fun getValidationErrors(): PersistentSet<ValidationError> = persistentSetOf()
        override fun getExtraFields(): PersistentSet<IdentityField> = persistentSetOf()
        override fun getFocusedField(): Option<IdentityField> = None
        override fun getCanUseCustomFields() = false
        override fun showAddPersonalDetailsButton() = false
        override fun showAddAddressDetailsButton() = false
        override fun showAddContactDetailsButton() = false
        override fun showAddWorkDetailsButton() = false
        override fun showFileAttachmentsBanner() = false
        override fun getAttachmentsState() = AttachmentsState.Initial
    }

    @Immutable
    data object Loading : IdentityUiState {
        override fun getItemSavedState() = ItemSavedState.Unknown
        override fun getSubmitLoadingState() = IsLoadingState.Loading
        override fun getValidationErrors(): PersistentSet<ValidationError> = persistentSetOf()
        override fun getExtraFields(): PersistentSet<IdentityField> = persistentSetOf()
        override fun getFocusedField(): Option<IdentityField> = None
        override fun getCanUseCustomFields() = false
        override fun showAddPersonalDetailsButton() = false
        override fun showAddAddressDetailsButton() = false
        override fun showAddContactDetailsButton() = false
        override fun showAddWorkDetailsButton() = false
        override fun showFileAttachmentsBanner() = false
        override fun getAttachmentsState() = AttachmentsState.Initial
    }

    @Immutable
    data object Error : IdentityUiState {
        override fun getItemSavedState() = ItemSavedState.Unknown
        override fun getSubmitLoadingState() = IsLoadingState.NotLoading
        override fun getValidationErrors(): PersistentSet<ValidationError> = persistentSetOf()
        override fun getExtraFields(): PersistentSet<IdentityField> = persistentSetOf()
        override fun getFocusedField(): Option<IdentityField> = None
        override fun getCanUseCustomFields() = false
        override fun showAddPersonalDetailsButton() = false
        override fun showAddAddressDetailsButton() = false
        override fun showAddContactDetailsButton() = false
        override fun showAddWorkDetailsButton() = false
        override fun showFileAttachmentsBanner() = false
        override fun getAttachmentsState() = AttachmentsState.Initial
    }

    @Immutable
    data class CreateIdentity(
        val shareUiState: ShareUiState,
        val sharedState: IdentitySharedUiState,
        val isCloned: Boolean,
        val canDisplayVaultSharedWarningDialog: Boolean
    ) : IdentityUiState {
        override fun getItemSavedState() = sharedState.isItemSaved
        override fun getSubmitLoadingState() = sharedState.isLoadingState
        override fun getValidationErrors() = sharedState.validationErrors
        override fun getExtraFields() = sharedState.extraFields
        override fun getFocusedField() = sharedState.focusedField
        override fun getCanUseCustomFields() = sharedState.canUseCustomFields
        override fun showAddPersonalDetailsButton() = sharedState.showAddPersonalDetailsButton
        override fun showAddAddressDetailsButton() = sharedState.showAddAddressDetailsButton
        override fun showAddContactDetailsButton() = sharedState.showAddContactDetailsButton
        override fun showAddWorkDetailsButton() = sharedState.showAddWorkDetailsButton
        override fun showFileAttachmentsBanner() = sharedState.showFileAttachmentsBanner
        override fun getAttachmentsState() = sharedState.attachmentsState
    }

    @Immutable
    data class UpdateIdentity(
        val selectedShareId: ShareId,
        val sharedState: IdentitySharedUiState,
        val hasReceivedItem: Boolean,
        val canDisplayWarningVaultSharedDialog: Boolean,
        val canDisplaySharedItemWarningDialog: Boolean
    ) : IdentityUiState {
        override fun getItemSavedState() = sharedState.isItemSaved
        override fun getSubmitLoadingState() = sharedState.isLoadingState
        override fun getValidationErrors() = sharedState.validationErrors
        override fun getExtraFields() = sharedState.extraFields
        override fun getFocusedField() = sharedState.focusedField
        override fun getCanUseCustomFields() = sharedState.canUseCustomFields
        override fun showAddPersonalDetailsButton() = sharedState.showAddPersonalDetailsButton
        override fun showAddAddressDetailsButton() = sharedState.showAddAddressDetailsButton
        override fun showAddContactDetailsButton() = sharedState.showAddContactDetailsButton
        override fun showAddWorkDetailsButton() = sharedState.showAddWorkDetailsButton
        override fun showFileAttachmentsBanner() = sharedState.showFileAttachmentsBanner
        override fun getAttachmentsState() = sharedState.attachmentsState
    }

    val canDisplayWarningVaultSharedDialogLocal: Boolean
        get() = when (this) {
            is CreateIdentity -> this.canDisplayVaultSharedWarningDialog
            is UpdateIdentity -> this.canDisplayWarningVaultSharedDialog
            else -> false
        }

    val canDisplaySharedItemWarningDialogLocal: Boolean
        get() = when (this) {
            is CreateIdentity -> false
            is UpdateIdentity -> this.canDisplaySharedItemWarningDialog
            else -> false
        }

    val hasUserEdited: Boolean
        get() = when (this) {
            is CreateIdentity -> sharedState.hasUserEditedContent
            is UpdateIdentity -> sharedState.hasUserEditedContent
            else -> false
        }

    fun shouldShowVaultSelector(): Boolean = when {
        this is CreateIdentity && shareUiState is ShareUiState.Success -> shareUiState.vaultList.size > 1
        else -> false
    }

    fun getSelectedVault(): Option<Vault> = when {
        this is CreateIdentity && shareUiState is ShareUiState.Success ->
            shareUiState.currentVault.vault.some()

        else -> None
    }

    fun getSelectedShareId(): Option<ShareId> = when {
        this is CreateIdentity && shareUiState is ShareUiState.Success ->
            shareUiState.currentVault.vault.some().map { it.shareId }

        this is UpdateIdentity -> selectedShareId.some()
        else -> None
    }

    fun getSelectedFolderName(): Option<String> = when {
        this is CreateIdentity && shareUiState is ShareUiState.Success ->
            shareUiState.selectedFolder?.name?.takeIf { it.isNotBlank() }.toOption()
        else -> None
    }

    fun getSelectedFolderId(): Option<FolderId> = when {
        this is CreateIdentity && shareUiState is ShareUiState.Success ->
            shareUiState.selectedFolder?.id.toOption()
        else -> None
    }

    fun performActionOnContentReceived(): Boolean = when (this) {
        is CreateIdentity -> isCloned
        is UpdateIdentity -> hasReceivedItem
        else -> false
    }
}
