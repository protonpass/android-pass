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
import proton.android.pass.domain.VaultWithItemCount
import proton.android.pass.featureitemcreate.impl.common.ShareUiState

sealed interface IdentityUiState {
    @Immutable
    data object NotInitialised : IdentityUiState

    @Immutable
    data object Loading : IdentityUiState

    @Immutable
    data object Error : IdentityUiState

    @Immutable
    data class Success(
        val shareUiState: ShareUiState
    ) : IdentityUiState

    fun shouldShowVaultSelector(): Boolean =
        if (this is Success && shareUiState is ShareUiState.Success) shareUiState.vaultList.size > 1 else false

    fun getSelectedVault(): VaultWithItemCount? =
        if (this is Success && shareUiState is ShareUiState.Success) shareUiState.currentVault else null
}
