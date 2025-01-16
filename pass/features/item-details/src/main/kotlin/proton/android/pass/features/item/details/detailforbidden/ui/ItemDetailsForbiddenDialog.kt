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

package proton.android.pass.features.item.details.detailforbidden.ui

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import proton.android.pass.features.item.details.detailforbidden.presentation.ItemDetailsForbiddenViewModel
import proton.android.pass.features.item.details.shared.navigation.ItemDetailsNavDestination

@Composable
fun ItemDetailsForbiddenDialog(
    onNavigated: (ItemDetailsNavDestination) -> Unit,
    viewModel: ItemDetailsForbiddenViewModel = hiltViewModel()
) = with(viewModel) {
    ItemDetailsForbiddenContent(
        state = state,
        onEvent = { uiEvent ->
            when (uiEvent) {
                ItemDetailsForbiddenUiEvent.OnCancel,
                ItemDetailsForbiddenUiEvent.OnDismiss -> ItemDetailsNavDestination.CloseScreen

                ItemDetailsForbiddenUiEvent.OnUpgrade -> ItemDetailsNavDestination.Upgrade
            }.also(onNavigated)
        }
    )

}
