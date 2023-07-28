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

package proton.android.pass.featurevault.impl.leave

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState

class LeaveVaultDialogPreviewProvider : PreviewParameterProvider<LeaveVaultUiState> {
    override val values: Sequence<LeaveVaultUiState>
        get() = sequence {
            for (loading in listOf(IsLoadingState.Loading, IsLoadingState.NotLoading)) {
                yield(
                    LeaveVaultUiState(
                        vaultName = "Vault name",
                        event = LeaveVaultEvent.Unknown,
                        isLoadingState = loading
                    )
                )
            }
        }
}
