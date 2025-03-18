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

package proton.android.pass.features.passkeys.select.navigation

import androidx.navigation.NavGraphBuilder
import proton.android.pass.commonui.impl.ui.bottomsheet.itemoptions.navigation.ItemOptionsBottomSheetNavItem
import proton.android.pass.commonui.impl.ui.bottomsheet.itemoptions.navigation.ItemOptionsNavDestination
import proton.android.pass.commonui.impl.ui.bottomsheet.itemoptions.navigation.itemOptionsNavGraph
import proton.android.pass.features.auth.AuthNavigation
import proton.android.pass.features.auth.EnterPin
import proton.android.pass.features.auth.authGraph
import proton.android.pass.features.passkeys.select.presentation.SelectPasskeyActionAfterAuth
import proton.android.pass.features.passkeys.select.ui.app.SelectPasskeyEvent
import proton.android.pass.features.passkeys.select.ui.bottomsheet.selectpasskey.selectPasskeyBottomsheetGraph
import proton.android.pass.features.searchoptions.SearchOptionsNavigation
import proton.android.pass.features.searchoptions.SortingBottomsheetNavItem
import proton.android.pass.features.searchoptions.SortingLocation
import proton.android.pass.features.searchoptions.searchOptionsGraph
import proton.android.pass.features.selectitem.navigation.SelectItem
import proton.android.pass.features.selectitem.navigation.SelectItemNavigation
import proton.android.pass.features.selectitem.navigation.SelectItemState
import proton.android.pass.features.selectitem.navigation.selectItemGraph
import proton.android.pass.navigation.api.AppNavigator

@Suppress(
    "CyclomaticComplexMethod",
    "ComplexMethod",
    "LongMethod",
    "LongParameterList",
    "ThrowsCount"
)
fun NavGraphBuilder.selectPasskeyActivityGraph(
    appNavigator: AppNavigator,
    domain: String,
    actionAfterAuth: SelectPasskeyActionAfterAuth,
    onEvent: (SelectPasskeyEvent) -> Unit,
    onNavigate: (SelectPasskeyNavigation) -> Unit,
    dismissBottomSheet: (() -> Unit) -> Unit
) {
    authGraph(
        canLogout = false,
        navigation = {
            when (it) {
                is AuthNavigation.CloseScreen -> onNavigate(SelectPasskeyNavigation.Cancel)
                is AuthNavigation.Success -> dismissBottomSheet {
                    when (actionAfterAuth) {
                        SelectPasskeyActionAfterAuth.SelectItem -> appNavigator.navigate(SelectItem)
                        SelectPasskeyActionAfterAuth.EmitEvent -> {
                            onEvent(SelectPasskeyEvent.OnAuthPerformed)
                        }
                    }
                }

                AuthNavigation.Dismissed -> onNavigate(SelectPasskeyNavigation.Cancel)
                AuthNavigation.Failed -> onNavigate(SelectPasskeyNavigation.Cancel)
                is AuthNavigation.ForceSignOut ->
                    onNavigate(SelectPasskeyNavigation.ForceSignOut(it.userId))
                is AuthNavigation.EnterPin -> appNavigator.navigate(
                    destination = EnterPin,
                    route = EnterPin.buildRoute(it.origin)
                )

                is AuthNavigation.SignOut,
                AuthNavigation.ForceSignOutAllUsers -> {}

                AuthNavigation.CloseBottomsheet -> dismissBottomSheet {}
            }
        }
    )

    selectItemGraph(
        state = SelectItemState.Passkey.Select(domain),
        onScreenShown = {
            onEvent(SelectPasskeyEvent.OnSelectScreenShown)
        },
        onNavigate = {
            when (it) {
                SelectItemNavigation.AddItem -> {}
                SelectItemNavigation.Cancel -> {
                    onNavigate(SelectPasskeyNavigation.Cancel)
                }

                is SelectItemNavigation.ItemSelected -> {
                    onEvent(SelectPasskeyEvent.OnItemSelected(it.item))
                }

                is SelectItemNavigation.SuggestionSelected -> {
                    onEvent(SelectPasskeyEvent.OnItemSelected(it.item))
                }

                is SelectItemNavigation.SortingBottomsheet ->
                    appNavigator.navigate(
                        SortingBottomsheetNavItem,
                        SortingBottomsheetNavItem.createNavRoute(
                            location = SortingLocation.Autofill
                        )
                    )

                is SelectItemNavigation.ItemOptions -> {
                    appNavigator.navigate(
                        destination = ItemOptionsBottomSheetNavItem,
                        route = ItemOptionsBottomSheetNavItem.createRoute(it.userId, it.shareId, it.itemId)
                    )
                }

                SelectItemNavigation.Upgrade -> {
                    onNavigate(SelectPasskeyNavigation.Upgrade)
                }

                SelectItemNavigation.SelectAccount ->
                    throw IllegalStateException("Cannot select account on SelectPasskey")
            }
        }
    )

    searchOptionsGraph(
        onNavigateEvent = {
            when (it) {
                is SearchOptionsNavigation.SelectSorting -> dismissBottomSheet {}

                SearchOptionsNavigation.Filter -> {
                    throw IllegalStateException("Cannot Filter on SelectPasskey")
                }

                SearchOptionsNavigation.Sorting -> {
                    throw IllegalStateException("Cannot change Sorting on SelectPasskey")
                }

                SearchOptionsNavigation.BulkActions -> {
                    throw IllegalStateException("Cannot perform bulk actions on SelectPasskey")
                }

                SearchOptionsNavigation.ResetFilters -> {
                    throw IllegalStateException("Cannot reset filters on SelectPasskey")
                }
            }
        }
    )

    selectPasskeyBottomsheetGraph(
        onPasskeySelected = {
            onEvent(SelectPasskeyEvent.OnPasskeySelected(it))
        },
        onDismiss = {
            dismissBottomSheet {}
        }
    )

    itemOptionsNavGraph { destination ->
        when (destination) {
            ItemOptionsNavDestination.Dismiss -> dismissBottomSheet {}
        }
    }
}
