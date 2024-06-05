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

package proton.android.pass.features.item.details.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.commonui.api.BrowserUtils
import proton.android.pass.features.item.details.navigation.ItemDetailsNavDestination
import proton.android.pass.features.item.details.presentation.ItemDetailsViewModel

@Composable
fun ItemDetailsScreen(
    onNavigated: (ItemDetailsNavDestination) -> Unit,
    viewModel: ItemDetailsViewModel = hiltViewModel()
) = with(viewModel) {
    val state by state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    ItemDetailsContent(
        state = state,
        onEvent = { uiEvent ->
            when (uiEvent) {
                ItemDetailsUiEvent.OnNavigateBack -> ItemDetailsNavDestination.Back
                    .also(onNavigated)

                is ItemDetailsUiEvent.OnEditClicked -> ItemDetailsNavDestination.EditItem(
                    shareId = uiEvent.shareId,
                    itemId = uiEvent.itemId,
                    itemCategory = uiEvent.itemCategory
                ).also(onNavigated)

                is ItemDetailsUiEvent.OnPasskeyClicked -> ItemDetailsNavDestination.PasskeyDetails(
                    passkeyContent = uiEvent.passkeyContent
                ).also(onNavigated)

                is ItemDetailsUiEvent.OnFieldClicked -> onItemFieldClicked(
                    text = uiEvent.text,
                    plainFieldType = uiEvent.field
                )

                is ItemDetailsUiEvent.OnHiddenFieldClicked -> onItemHiddenFieldClicked(
                    hiddenState = uiEvent.state,
                    hiddenFieldType = uiEvent.field
                )

                is ItemDetailsUiEvent.OnHiddenFieldToggled -> onItemHiddenFieldToggled(
                    isVisible = uiEvent.isVisible,
                    hiddenState = uiEvent.state,
                    hiddenFieldType = uiEvent.field
                )

                is ItemDetailsUiEvent.OnLinkClicked -> BrowserUtils.openWebsite(
                    context = context,
                    website = uiEvent.link
                )
            }
        }
    )
}
