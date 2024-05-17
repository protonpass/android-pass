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

package proton.android.pass.featurehome.impl

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import proton.android.pass.common.api.Option
import proton.android.pass.commonuimodels.api.ItemTypeUiState
import proton.android.pass.composecomponents.impl.item.EmptySearchResults
import proton.android.pass.domain.ShareId
import proton.android.pass.featurehome.impl.empty.EmptyReadOnly
import proton.android.pass.featurehome.impl.empty.HomeEmptyList
import proton.android.pass.featurehome.impl.trash.EmptyTrashContent

@Composable
fun HomeEmptyContent(
    isTrashMode: Boolean,
    inSearchMode: Boolean,
    shareId: Option<ShareId>,
    onEvent: (HomeUiEvent) -> Unit,
    readOnly: Boolean
) {
    when {
        inSearchMode -> EmptySearchResults()
        isTrashMode -> EmptyTrashContent()
        readOnly -> EmptyReadOnly()
        else -> HomeEmptyList(
            modifier = Modifier.fillMaxHeight(),
            onCreateLoginClick = {
                onEvent(
                    HomeUiEvent.AddItemClick(
                        shareId,
                        ItemTypeUiState.Login
                    )
                )
            },
            onCreateAliasClick = {
                onEvent(
                    HomeUiEvent.AddItemClick(
                        shareId,
                        ItemTypeUiState.Alias
                    )
                )
            },
            onCreateNoteClick = { onEvent(HomeUiEvent.AddItemClick(shareId, ItemTypeUiState.Note)) },
            onCreateCreditCardClick = {
                onEvent(HomeUiEvent.AddItemClick(shareId, ItemTypeUiState.CreditCard))
            }
        )
    }
}
