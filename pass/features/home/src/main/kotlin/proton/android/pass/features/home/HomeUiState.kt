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

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.commonui.api.GroupedItemList
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.composecomponents.impl.bottombar.AccountType
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemAction
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.composecomponents.impl.uievents.IsProcessingSearchState
import proton.android.pass.composecomponents.impl.uievents.IsRefreshingState
import proton.android.pass.crypto.api.extensions.toVault
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.Share
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.Vault
import proton.android.pass.preferences.AliasTrashDialogStatusPreference
import proton.android.pass.searchoptions.api.SearchFilterType
import proton.android.pass.searchoptions.api.SearchSortingType
import proton.android.pass.searchoptions.api.VaultSelectionOption

internal enum class LoadingDialog {
    DisableAlias,
    Other
}

internal sealed interface ActionState {
    data object Done : ActionState

    @JvmInline
    value class Loading(val loadingDialog: LoadingDialog = LoadingDialog.Other) : ActionState
    data object Unknown : ActionState
}

internal sealed interface HomeNavEvent {

    data class ItemHistory(val shareId: ShareId, val itemId: ItemId) : HomeNavEvent

    data object ShowBulkMoveToVault : HomeNavEvent

    data object Unknown : HomeNavEvent

    data object UpgradeDialog : HomeNavEvent

    data object OnBulkMigrationSharedWarning : HomeNavEvent

}

@Immutable
internal data class HomeUiState(
    val homeListUiState: HomeListUiState,
    val searchUiState: SearchUiState,
    val pinningUiState: PinningUiState,
    val accountType: AccountType,
    val navEvent: HomeNavEvent,
    val action: BottomSheetItemAction,
    val isFreePlan: Boolean,
    val canCreateItems: Boolean,
    val hasShares: Boolean,
    private val aliasTrashDialogStatusPreference: AliasTrashDialogStatusPreference
) {

    internal val sharedTrashedItemsCount: Int = homeListUiState.items
        .sumOf { groupedItemList ->
            groupedItemList.items
                .filter { item -> item.isInTrash() && item.isShared }
                .size
        }

    internal val hasSharedTrashedItems: Boolean = sharedTrashedItemsCount > 0

    internal val isTopBarAvailable: Boolean = hasShares

    internal val isDrawerAvailable: Boolean = hasShares

    internal fun shouldShowRecentSearchHeader() =
        homeListUiState.items.isNotEmpty() && searchUiState.inSearchMode && searchUiState.isInSuggestionsMode

    internal fun shouldShowItemListHeader() = homeListUiState.items.isNotEmpty() &&
        homeListUiState.isLoading == IsLoadingState.NotLoading &&
        !searchUiState.isInSuggestionsMode &&
        !searchUiState.isProcessingSearch.value() &&
        (searchUiState.inSearchMode || pinningUiState.inPinningMode)

    internal fun isSelectedVaultReadOnly() = when (val selection = homeListUiState.homeVaultSelection) {
        is VaultSelectionOption.Vault ->
            homeListUiState
                .shares[selection.shareId]
                ?.isViewer == true

        VaultSelectionOption.AllVaults,
        VaultSelectionOption.SharedByMe,
        VaultSelectionOption.SharedWithMe,
        is VaultSelectionOption.Trash -> false
    }

    internal fun shouldDisplayTrashAliasDialog(itemUiModel: ItemUiModel): Boolean = when {
        aliasTrashDialogStatusPreference.value -> false
        else -> (itemUiModel.contents as? ItemContents.Alias)?.isEnabled == true
    }

    internal companion object {

        internal val Loading = HomeUiState(
            homeListUiState = HomeListUiState.Loading,
            searchUiState = SearchUiState.Initial,
            pinningUiState = PinningUiState.Initial,
            accountType = AccountType.Free,
            navEvent = HomeNavEvent.Unknown,
            action = BottomSheetItemAction.None,
            isFreePlan = true,
            canCreateItems = false,
            aliasTrashDialogStatusPreference = AliasTrashDialogStatusPreference.Disabled,
            hasShares = false
        )

    }

}

@Immutable
internal data class SelectionTopBarState(
    val isTrash: Boolean,
    val selectedItemCount: Int,
    val pinningState: PinningState,
    val aliasState: AliasState,
    val actionsEnabled: Boolean
) {
    internal companion object {
        internal val Initial = SelectionTopBarState(
            isTrash = false,
            selectedItemCount = 0,
            pinningState = PinningState.Initial,
            aliasState = AliasState.Initial,
            actionsEnabled = true
        )
    }
}

@Immutable
internal data class PinningState(
    val areAllSelectedPinned: Boolean,
    val pinningLoadingState: IsLoadingState
) {
    internal companion object {
        internal val Initial = PinningState(
            areAllSelectedPinned = false,
            pinningLoadingState = IsLoadingState.NotLoading
        )
    }
}

@Immutable
internal data class AliasState(
    val areAllSelectedAliases: Boolean,
    val areAllSelectedDisabled: Boolean,
    val aliasLoadingState: IsLoadingState
) {
    internal companion object {
        internal val Initial = AliasState(
            areAllSelectedAliases = false,
            areAllSelectedDisabled = false,
            aliasLoadingState = IsLoadingState.NotLoading
        )
    }
}

@Immutable
internal data class HomeSelectionState(
    val selectedItems: ImmutableList<ItemUiModel>,
    val isInSelectMode: Boolean,
    val topBarState: SelectionTopBarState
) {

    private val selectedSharedItems = selectedItems.filter { it.isShared }

    internal val selectedSharedItemsCount = selectedSharedItems.size

    internal val hasSelectedSharedItems = selectedSharedItems.isNotEmpty()

    internal companion object {

        internal val Initial = HomeSelectionState(
            selectedItems = persistentListOf(),
            isInSelectMode = false,
            topBarState = SelectionTopBarState.Initial
        )

    }

}

@Immutable
internal data class HomeListUiState(
    val isLoading: IsLoadingState,
    val isRefreshing: IsRefreshingState,
    val shouldScrollToTop: Boolean,
    val canLoadExternalImages: Boolean,
    val actionState: ActionState = ActionState.Unknown,
    val items: ImmutableList<GroupedItemList>,
    val selectedShare: Option<Share> = None,
    val shares: ImmutableMap<ShareId, Share>,
    val homeVaultSelection: VaultSelectionOption = VaultSelectionOption.AllVaults,
    val searchFilterType: SearchFilterType = SearchFilterType.All,
    val sortingType: SearchSortingType = SearchSortingType.MostRecent,
    val selectionState: HomeSelectionState,
    val showNeedsUpdate: Boolean
) {

    internal val selectedVaultOption: Option<Vault> = selectedShare.flatMap { share ->
        share.toVault()
    }

    internal val selectedVaultName: String = selectedVaultOption.value()
        ?.name
        .orEmpty()

    internal fun isItemSelectable(item: ItemUiModel): Boolean = shares[item.shareId]
        ?.canBeSelected == true

    fun checkCanUpdate(shareId: ShareId): Boolean = shares[shareId]?.canBeUpdated == true

    fun checkCanClone(shareId: ShareId): Boolean = shares[shareId]?.canBeCloned == true

    fun canViewHistory(shareId: ShareId): Boolean = shares[shareId]?.canBeHistoryViewed == true

    fun canBeDeleted(shareId: ShareId): Boolean = shares[shareId]?.canBeDeleted == true

    internal companion object {

        internal val Loading = HomeListUiState(
            isLoading = IsLoadingState.Loading,
            isRefreshing = IsRefreshingState.NotRefreshing,
            shouldScrollToTop = false,
            canLoadExternalImages = false,
            items = persistentListOf(),
            shares = persistentMapOf(),
            selectionState = HomeSelectionState.Initial,
            showNeedsUpdate = false
        )

    }

}

@Immutable
internal data class SearchUiState(
    val searchQuery: String,
    val isProcessingSearch: IsProcessingSearchState,
    val inSearchMode: Boolean,
    val isInSuggestionsMode: Boolean,
    val itemTypeCount: ItemTypeCount
) {

    internal companion object {

        internal val Initial = SearchUiState(
            searchQuery = "",
            isProcessingSearch = IsProcessingSearchState.NotLoading,
            inSearchMode = false,
            isInSuggestionsMode = false,
            itemTypeCount = ItemTypeCount.Initial
        )

    }

}

@Immutable
internal data class PinningUiState(
    val inPinningMode: Boolean,
    val filteredItems: ImmutableList<GroupedItemList>,
    val itemTypeCount: ItemTypeCount,
    val unFilteredItems: PersistentList<ItemUiModel>
) {

    internal companion object {

        internal val Initial = PinningUiState(
            inPinningMode = false,
            filteredItems = persistentListOf(),
            unFilteredItems = persistentListOf(),
            itemTypeCount = ItemTypeCount.Initial
        )

    }

}
