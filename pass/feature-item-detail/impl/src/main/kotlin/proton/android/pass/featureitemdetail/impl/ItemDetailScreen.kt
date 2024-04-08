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

package proton.android.pass.featureitemdetail.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ItemDetailScreen(
    modifier: Modifier = Modifier,
    onNavigate: (ItemDetailNavigation) -> Unit,
    viewModel: ItemDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.event) {
        when (uiState.event) {
            ItemDetailScreenEvent.Close -> onNavigate(ItemDetailNavigation.Back)
            else -> {}
        }
        viewModel.clearEvent()
    }

    LaunchedEffect(uiState.itemTypeUiState) {
        viewModel.sendItemReadEvent(uiState.itemTypeUiState)
    }

    ItemDetailContent(
        modifier = modifier,
        uiState = uiState,
        onNavigate = onNavigate
    )
}

