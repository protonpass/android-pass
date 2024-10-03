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

package proton.android.pass.commonui.impl.ui.bottomsheet.itemoptions.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.commonui.impl.ui.bottomsheet.itemoptions.navigation.ItemOptionsNavDestination
import proton.android.pass.commonui.impl.ui.bottomsheet.itemoptions.presentation.ItemOptionsEvent
import proton.android.pass.commonui.impl.ui.bottomsheet.itemoptions.presentation.ItemOptionsViewModel

@Composable
fun ItemOptionsBottomSheet(
    modifier: Modifier = Modifier,
    onNavigate: (ItemOptionsNavDestination) -> Unit,
    viewModel: ItemOptionsViewModel = hiltViewModel()
) = with(viewModel) {
    val state by stateFlow.collectAsStateWithLifecycle()

    LaunchedEffect(state.event) {
        when (state.event) {
            ItemOptionsEvent.Idle -> {}
            ItemOptionsEvent.Close -> onNavigate(ItemOptionsNavDestination.Dismiss)
        }
    }

    ItemOptionsBottomSheetContent(
        modifier = modifier,
        state = state,
        onUiEvent = { uiEvent ->
            when (uiEvent) {
                ItemOptionsBottomSheetUiEvent.OnCopyEmailClicked -> onCopyEmail()
                ItemOptionsBottomSheetUiEvent.OnCopyPasswordClicked -> onCopyPassword()
                ItemOptionsBottomSheetUiEvent.OnCopyUsernameClicked -> onCopyUsername()
                ItemOptionsBottomSheetUiEvent.OnMoveToTrashClicked -> onMoveToTrash()
            }
        }
    )
}
