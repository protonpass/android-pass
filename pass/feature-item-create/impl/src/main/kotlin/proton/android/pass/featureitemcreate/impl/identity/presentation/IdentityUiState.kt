/*
 * Copyright (c) 2023-2024 Proton AG
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
import proton.android.pass.featureitemcreate.impl.ItemSavedState
import proton.android.pass.featureitemcreate.impl.common.ShareUiState
import proton.android.pass.featureitemcreate.impl.identity.presentation.bottomsheets.ExtraField
import proton.android.pass.featureitemcreate.impl.identity.presentation.bottomsheets.FocusedField

sealed interface IdentityUiState {
    @Immutable
    data object NotInitialised : IdentityUiState

    @Immutable
    data object Loading : IdentityUiState

    @Immutable
    data object Error : IdentityUiState

    @Immutable
    data class CreateIdentity(
        val shareUiState: ShareUiState,
        val sharedState: IdentitySharedUiState
    ) : IdentityUiState

    @Immutable
    data class UpdateIdentity(
        val selectedShareId: ShareId,
        val sharedState: IdentitySharedUiState,
        val hasReceivedItem: Boolean
    ) : IdentityUiState

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

    fun getItemSavedState(): ItemSavedState = when (this) {
        is CreateIdentity -> sharedState.isItemSaved
        is UpdateIdentity -> sharedState.isItemSaved
        else -> ItemSavedState.Unknown
    }

    fun getSubmitLoadingState(): IsLoadingState = when (this) {
        is Loading -> IsLoadingState.Loading
        is CreateIdentity -> sharedState.isLoadingState
        is UpdateIdentity -> sharedState.isLoadingState
        else -> IsLoadingState.NotLoading
    }

    fun getValidationErrors(): PersistentSet<IdentityValidationErrors> = when (this) {
        is CreateIdentity -> sharedState.validationErrors
        is UpdateIdentity -> sharedState.validationErrors
        else -> persistentSetOf()
    }

    fun getExtraFields(): PersistentSet<ExtraField> = when (this) {
        is CreateIdentity -> sharedState.extraFields
        is UpdateIdentity -> sharedState.extraFields
        else -> persistentSetOf()
    }

    fun getFocusedField(): Option<FocusedField> = when (this) {
        is CreateIdentity -> sharedState.focusedField
        is UpdateIdentity -> sharedState.focusedField
        else -> None
    }

    fun getCanUseCustomFields(): Boolean = when (this) {
        is CreateIdentity -> sharedState.canUseCustomFields
        is UpdateIdentity -> sharedState.canUseCustomFields
        else -> false
    }

    fun hasReceivedItem(): Boolean = when (this) {
        is UpdateIdentity -> hasReceivedItem
        else -> false
    }

    fun showAddPersonalDetailsButton(): Boolean = when (this) {
        is CreateIdentity -> sharedState.showAddPersonalDetailsButton
        is UpdateIdentity -> sharedState.showAddPersonalDetailsButton
        else -> false
    }

    fun showAddAddressDetailsButton(): Boolean = when (this) {
        is CreateIdentity -> sharedState.showAddAddressDetailsButton
        is UpdateIdentity -> sharedState.showAddAddressDetailsButton
        else -> false
    }

    fun showAddContactDetailsButton(): Boolean = when (this) {
        is CreateIdentity -> sharedState.showAddContactDetailsButton
        is UpdateIdentity -> sharedState.showAddContactDetailsButton
        else -> false
    }

    fun showAddWorkDetailsButton(): Boolean = when (this) {
        is CreateIdentity -> sharedState.showAddWorkDetailsButton
        is UpdateIdentity -> sharedState.showAddWorkDetailsButton
        else -> false
    }

    fun showFileAttachments(): Boolean = when (this) {
        is CreateIdentity -> sharedState.showFileAttachments
        is UpdateIdentity -> sharedState.showFileAttachments
        else -> false
    }

    fun getAttachmentsState(): AttachmentsState = when (this) {
        is CreateIdentity -> sharedState.attachmentsState
        is UpdateIdentity -> sharedState.attachmentsState
        else -> AttachmentsState.Initial
    }
}
