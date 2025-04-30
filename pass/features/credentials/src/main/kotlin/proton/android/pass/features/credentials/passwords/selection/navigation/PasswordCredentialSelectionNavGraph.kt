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

package proton.android.pass.features.credentials.passwords.selection.navigation

import androidx.navigation.NavGraphBuilder
import proton.android.pass.commonui.impl.ui.bottomsheet.itemoptions.navigation.ItemOptionsBottomSheetNavItem
import proton.android.pass.commonui.impl.ui.bottomsheet.itemoptions.navigation.ItemOptionsNavDestination
import proton.android.pass.commonui.impl.ui.bottomsheet.itemoptions.navigation.itemOptionsNavGraph
import proton.android.pass.features.auth.AuthNavigation
import proton.android.pass.features.auth.EnterPin
import proton.android.pass.features.auth.authGraph
import proton.android.pass.features.credentials.passwords.selection.presentation.PasswordCredentialSelectionActionAfterAuth
import proton.android.pass.features.credentials.passwords.selection.presentation.PasswordCredentialSelectionEvent
import proton.android.pass.features.searchoptions.SearchOptionsNavigation
import proton.android.pass.features.searchoptions.SortingBottomsheetNavItem
import proton.android.pass.features.searchoptions.SortingLocation
import proton.android.pass.features.searchoptions.searchOptionsGraph
import proton.android.pass.features.selectitem.navigation.SelectItem
import proton.android.pass.features.selectitem.navigation.SelectItemNavigation
import proton.android.pass.features.selectitem.navigation.SelectItemState
import proton.android.pass.features.selectitem.navigation.selectItemGraph
import proton.android.pass.navigation.api.AppNavigator

@Suppress("LongMethod", "LongParameterList", "ReturnCount", "ThrowsCount")
internal fun NavGraphBuilder.passwordCredentialSelectionNavGraph(
    appNavigator: AppNavigator,
    actionAfterAuth: PasswordCredentialSelectionActionAfterAuth,
    selectItemState: SelectItemState,
    onNavigate: (PasswordCredentialSelectionNavEvent) -> Unit,
    onEvent: (PasswordCredentialSelectionEvent) -> Unit,
    dismissBottomSheet: (() -> Unit) -> Unit
) {
    authGraph(
        canLogout = false,
        navigation = { destination ->
            when (destination) {
                AuthNavigation.CloseBottomsheet -> {
                    dismissBottomSheet {}
                }

                is AuthNavigation.CloseScreen,
                AuthNavigation.Dismissed,
                AuthNavigation.Failed -> {
                    onNavigate(PasswordCredentialSelectionNavEvent.Cancel)
                }

                is AuthNavigation.EnterPin -> {
                    appNavigator.navigate(
                        destination = EnterPin,
                        route = EnterPin.buildRoute(origin = destination.origin)
                    )
                }

                is AuthNavigation.ForceSignOut -> {
                    PasswordCredentialSelectionNavEvent.ForceSignOut(
                        userId = destination.userId
                    ).also(onNavigate)
                }

                is AuthNavigation.Success -> {
                    dismissBottomSheet {
                        when (actionAfterAuth) {
                            PasswordCredentialSelectionActionAfterAuth.ProceedWithRequest -> {
                                onEvent(PasswordCredentialSelectionEvent.OnAuthPerformed)
                            }

                            PasswordCredentialSelectionActionAfterAuth.SelectItem -> {
                                appNavigator.navigate(SelectItem)
                            }
                        }
                    }
                }

                is AuthNavigation.SignOut,
                AuthNavigation.ForceSignOutAllUsers -> Unit
            }
        }
    )

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
                    throw IllegalStateException("Cannot Filter on PasswordCredentialSelection")
                }

                SearchOptionsNavigation.Sorting -> {
                    throw IllegalStateException("Cannot change Sorting on PasswordCredentialSelection")
                }

                SearchOptionsNavigation.BulkActions -> {
                    throw IllegalStateException("Cannot perform bulk actions on PasswordCredentialSelection")
                }

                SearchOptionsNavigation.ResetFilters -> {
                    throw IllegalStateException("Cannot reset filters on PasswordCredentialSelection")
                }
            }
        }
    )

    selectItemGraph(
        state = selectItemState,
        onScreenShown = {
            onEvent(PasswordCredentialSelectionEvent.OnSelectScreenShown)
        },
        onNavigate = { destination ->
            when (destination) {
                SelectItemNavigation.AddItem -> Unit

                SelectItemNavigation.Cancel -> {
                    onNavigate(PasswordCredentialSelectionNavEvent.Cancel)
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
                    PasswordCredentialSelectionEvent.OnItemSelected(
                        itemUiModel = destination.item
                    ).also(onEvent)
                }

                SelectItemNavigation.SelectAccount -> {
                    throw IllegalStateException("Cannot select account on PasswordCredentialSelection")
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
                    PasswordCredentialSelectionEvent.OnItemSelected(
                        itemUiModel = destination.item
                    ).also(onEvent)
                }

                SelectItemNavigation.Upgrade -> {
                    onNavigate(PasswordCredentialSelectionNavEvent.Upgrade)
                }
            }
        }
    )
}
