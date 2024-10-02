/*
 * Copyright (c) 2023 Proton AG
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

package proton.android.pass.features.vault.bottomsheet.select

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlinx.collections.immutable.ImmutableList
import proton.android.pass.domain.VaultWithItemCount

@Stable
sealed interface VaultStatus {
    @Stable
    data object Selectable : VaultStatus

    @Stable
    data class Disabled(val reason: Reason) : VaultStatus

    enum class Reason {
        ReadOnly,
        Downgraded
    }
}

@Stable
data class VaultWithStatus(
    val vault: VaultWithItemCount,
    val status: VaultStatus
)

sealed interface SelectVaultUiState {
    data object Uninitialised : SelectVaultUiState
    data object Loading : SelectVaultUiState
    data object Error : SelectVaultUiState

    @Immutable
    data class Success(
        val vaults: ImmutableList<VaultWithStatus>,
        val selected: VaultWithItemCount,
        val showUpgradeMessage: Boolean
    ) : SelectVaultUiState
}
