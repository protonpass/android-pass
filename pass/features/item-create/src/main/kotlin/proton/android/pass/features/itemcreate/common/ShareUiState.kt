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

package proton.android.pass.features.itemcreate.common

import proton.android.pass.domain.VaultWithItemCount

sealed class ShareUiState {
    data object NotInitialised : ShareUiState()
    data object Loading : ShareUiState()
    data class Error(val shareError: ShareError) : ShareUiState()
    data class Success(
        val vaultList: List<VaultWithItemCount>,
        val currentVault: VaultWithItemCount
    ) : ShareUiState()
}

enum class ShareError {
    EmptyShareList,
    UpgradeInfoNotAvailable,
    SharesNotAvailable,
    NoPrimaryVault
}
