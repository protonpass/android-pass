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

import proton.android.pass.common.api.Option
import proton.android.pass.commonuimodels.api.ItemTypeUiState
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.domain.ShareId
import proton.android.pass.featuresearchoptions.api.SearchFilterType

sealed interface HomeUiEvent {
    object EnterSearch : HomeUiEvent
    object StopSearch : HomeUiEvent
    object DrawerIconClick : HomeUiEvent
    object SortingOptionsClick : HomeUiEvent
    object ClearRecentSearchClick : HomeUiEvent
    object Refresh : HomeUiEvent
    object ScrollToTop : HomeUiEvent
    object ProfileClick : HomeUiEvent
    object ActionsClick : HomeUiEvent
    object MoveItemsActionClick : HomeUiEvent
    object MoveToTrashItemsActionClick : HomeUiEvent
    object RestoreItemsActionClick : HomeUiEvent
    object PermanentlyDeleteItemsActionClick : HomeUiEvent
    object StopBulk : HomeUiEvent
    object SeeAllPinned : HomeUiEvent
    object StopSeeAllPinned : HomeUiEvent
    object PinItemsActionClick : HomeUiEvent
    object UnpinItemsActionClick : HomeUiEvent

    @JvmInline
    value class ItemClick(val item: ItemUiModel) : HomeUiEvent

    @JvmInline
    value class ItemMenuClick(val item: ItemUiModel) : HomeUiEvent

    @JvmInline
    value class SearchQueryChange(val query: String) : HomeUiEvent

    @JvmInline
    value class ItemTypeSelected(val searchFilterType: SearchFilterType) : HomeUiEvent

    @JvmInline
    value class SelectItem(val item: ItemUiModel) : HomeUiEvent

    data class AddItemClick(val shareId: Option<ShareId>, val state: ItemTypeUiState) : HomeUiEvent
}
