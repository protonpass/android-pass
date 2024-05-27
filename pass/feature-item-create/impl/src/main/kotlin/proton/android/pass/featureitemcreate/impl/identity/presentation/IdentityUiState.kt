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
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.domain.Vault
import proton.android.pass.featureitemcreate.impl.ItemSavedState
import proton.android.pass.featureitemcreate.impl.common.ShareUiState
import proton.android.pass.featureitemcreate.impl.identity.presentation.bottomsheets.ExtraField

sealed interface IdentityUiState {
    @Immutable
    data object NotInitialised : IdentityUiState

    @Immutable
    data object Loading : IdentityUiState

    @Immutable
    data object Error : IdentityUiState

    @Immutable
    data class Success(
        val shareUiState: ShareUiState,
        val sharedState: IdentitySharedUiState
    ) : IdentityUiState

    val hasUserEdited: Boolean
        get() = if (this is Success) sharedState.hasUserEditedContent else false

    fun shouldShowVaultSelector(): Boolean =
        if (this is Success && shareUiState is ShareUiState.Success) shareUiState.vaultList.size > 1 else false

    fun getSelectedVault(): Option<Vault> =
        if (this is Success && shareUiState is ShareUiState.Success) shareUiState.currentVault.vault.some() else None

    fun getItemSavedState(): ItemSavedState = if (this is Success) sharedState.isItemSaved else ItemSavedState.Unknown

    fun getSubmitLoadingState(): IsLoadingState = when (this) {
        is Loading -> IsLoadingState.Loading
        is Success -> sharedState.isLoadingState
        else -> IsLoadingState.NotLoading
    }

    fun getValidationErrors(): PersistentSet<IdentityValidationErrors> =
        if (this is Success) sharedState.validationErrors else persistentSetOf()

    fun getExtraFields(): PersistentSet<ExtraField> =
        if (this is Success) sharedState.extraFields else persistentSetOf()
}
