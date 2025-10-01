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

package proton.android.pass.features.home

import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.Option
import proton.android.pass.commonuimodels.api.ItemTypeUiState
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.inappmessages.InAppMessageId
import proton.android.pass.searchoptions.api.SearchFilterType

sealed interface HomeUiEvent {

    data object ActionsClick : HomeUiEvent

    data class AddItemClick(val shareId: Option<ShareId>, val state: ItemTypeUiState) : HomeUiEvent

    data object ClearRecentSearchClick : HomeUiEvent

    data object DrawerIconClick : HomeUiEvent

    data object EnterSearch : HomeUiEvent

    @JvmInline
    value class ItemClick(val item: ItemUiModel) : HomeUiEvent

    @JvmInline
    value class ItemMenuClick(val item: ItemUiModel) : HomeUiEvent

    @JvmInline
    value class ItemTypeSelected(val searchFilterType: SearchFilterType) : HomeUiEvent

    data object MoveItemsActionClick : HomeUiEvent

    data object MoveToTrashItemsActionClick : HomeUiEvent

    data object PermanentlyDeleteItemsActionClick : HomeUiEvent

    data object ProfileClick : HomeUiEvent

    data object Refresh : HomeUiEvent

    data object RestoreItemsActionClick : HomeUiEvent

    @JvmInline
    value class SearchQueryChange(val query: String) : HomeUiEvent

    data object ScrollToTop : HomeUiEvent

    data object SecurityCenterClick : HomeUiEvent

    data object SeeAllPinned : HomeUiEvent

    @JvmInline
    value class SelectItem(val item: ItemUiModel) : HomeUiEvent

    data object SortingOptionsClick : HomeUiEvent

    data object StopBulk : HomeUiEvent

    data object StopSearch : HomeUiEvent

    data object StopSeeAllPinned : HomeUiEvent

    data object PinItemsActionClick : HomeUiEvent

    data object UnpinItemsActionClick : HomeUiEvent

    data object EnableAliasItemsActionClick : HomeUiEvent

    data object DisableAliasItemsActionClick : HomeUiEvent

    data class PromoInAppMessageClick(val userId: UserId, val inAppMessageId: InAppMessageId) : HomeUiEvent

    data object OnCreateVaultClick : HomeUiEvent

    data object OnUpgradeClick : HomeUiEvent
}
