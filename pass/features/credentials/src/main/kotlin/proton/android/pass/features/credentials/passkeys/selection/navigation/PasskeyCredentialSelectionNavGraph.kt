/*
 * Copyright (c) 2025 Proton AG
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

package proton.android.pass.features.credentials.passkeys.selection.navigation

import androidx.navigation.NavGraphBuilder
import proton.android.pass.commonui.impl.ui.bottomsheet.itemoptions.navigation.ItemOptionsBottomSheetNavItem
import proton.android.pass.commonui.impl.ui.bottomsheet.itemoptions.navigation.ItemOptionsNavDestination
import proton.android.pass.commonui.impl.ui.bottomsheet.itemoptions.navigation.itemOptionsNavGraph
import proton.android.pass.features.credentials.passkeys.selection.presentation.PasskeyCredentialSelectionEvent
import proton.android.pass.features.passkeys.select.navigation.selectPasskeyBottomsheetGraph
import proton.android.pass.features.searchoptions.SearchOptionsNavigation
import proton.android.pass.features.searchoptions.SortingBottomsheetNavItem
import proton.android.pass.features.searchoptions.SortingLocation
import proton.android.pass.features.searchoptions.searchOptionsGraph
import proton.android.pass.features.selectitem.navigation.SelectItemNavigation
import proton.android.pass.features.selectitem.navigation.SelectItemState
import proton.android.pass.features.selectitem.navigation.selectItemGraph
import proton.android.pass.navigation.api.AppNavigator

@Suppress("LongMethod", "LongParameterList", "ReturnCount", "ThrowsCount")
internal fun NavGraphBuilder.passkeyCredentialSelectionNavGraph(
    appNavigator: AppNavigator,
    passkeyDomain: String,
    onNavigate: (PasskeyCredentialSelectionNavEvent) -> Unit,
    onEvent: (PasskeyCredentialSelectionEvent) -> Unit,
    dismissBottomSheet: (() -> Unit) -> Unit
) {
    itemOptionsNavGraph { destination ->
        when (destination) {
            ItemOptionsNavDestination.Dismiss -> dismissBottomSheet {}
        }
    }

    searchOptionsGraph(
        onNavigateEvent = { destination ->
            when (destination) {
                is SearchOptionsNavigation.SelectSorting -> dismissBottomSheet {}

                SearchOptionsNavigation.Filter -> {
                    throw IllegalStateException("Cannot Filter on PasskeyCredentialSelection")
                }

                SearchOptionsNavigation.Sorting -> {
                    throw IllegalStateException("Cannot change Sorting on PasskeyCredentialSelection")
                }

                SearchOptionsNavigation.BulkActions -> {
                    throw IllegalStateException("Cannot perform bulk actions on PasskeyCredentialSelection")
                }

                SearchOptionsNavigation.ResetFilters -> {
                    throw IllegalStateException("Cannot reset filters on PasskeyCredentialSelection")
                }
            }
        }
    )

    selectItemGraph(
        state = SelectItemState.Passkey.Select(title = passkeyDomain),
        onScreenShown = {
            onEvent(PasskeyCredentialSelectionEvent.OnSelectScreenShown)
        },
        onNavigate = { destination ->
            when (destination) {
                SelectItemNavigation.AddItem -> Unit

                SelectItemNavigation.Cancel -> {
                    onNavigate(PasskeyCredentialSelectionNavEvent.Cancel)
                }

                is SelectItemNavigation.ItemOptions -> {
                    appNavigator.navigate(
                        destination = ItemOptionsBottomSheetNavItem,
                        route = ItemOptionsBottomSheetNavItem.createRoute(
                            userId = destination.userId,
                            shareId = destination.shareId,
                            itemId = destination.itemId
                        )
                    )
                }

                is SelectItemNavigation.ItemSelected -> {
                    PasskeyCredentialSelectionEvent.OnItemSelected(
                        itemUiModel = destination.item
                    ).also(onEvent)
                }

                SelectItemNavigation.SelectAccount -> {
                    throw IllegalStateException("Cannot select account on PasskeyCredentialSelection")
                }

                SelectItemNavigation.SortingBottomsheet -> {
                    appNavigator.navigate(
                        destination = SortingBottomsheetNavItem,
                        route = SortingBottomsheetNavItem.createNavRoute(
                            location = SortingLocation.Autofill
                        )
                    )
                }

                is SelectItemNavigation.SuggestionSelected -> {
                    PasskeyCredentialSelectionEvent.OnItemSelected(
                        itemUiModel = destination.item
                    ).also(onEvent)
                }

                SelectItemNavigation.Upgrade -> {
                    onNavigate(PasskeyCredentialSelectionNavEvent.Upgrade)
                }
            }
        }
    )

    selectPasskeyBottomsheetGraph(
        onPasskeySelected = { passkey ->
            PasskeyCredentialSelectionEvent.OnPasskeySelected(
                passkey = passkey
            ).also(onEvent)
        },
        onDismiss = {
            dismissBottomSheet {}
        }
    )
}
