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

package proton.android.pass.autofill.ui.autosave

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import kotlinx.coroutines.Job
import me.proton.core.domain.entity.UserId
import proton.android.pass.commonui.api.onBottomSheetDismissed
import proton.android.pass.composecomponents.impl.bottomsheet.PassModalBottomSheetLayout
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.features.auth.AUTH_GRAPH
import proton.android.pass.features.auth.AuthNavigation
import proton.android.pass.features.auth.EnterPin
import proton.android.pass.features.auth.authGraph
import proton.android.pass.features.itemcreate.login.CREATE_LOGIN_GRAPH
import proton.android.pass.features.itemcreate.login.CreateLoginNavItem
import proton.android.pass.features.itemcreate.login.EDIT_LOGIN_GRAPH
import proton.android.pass.features.report.navigation.AccountSwitchNavItem
import proton.android.pass.features.report.navigation.AccountSwitchNavigation
import proton.android.pass.features.report.navigation.accountSwitchNavGraph
import proton.android.pass.features.searchoptions.SearchOptionsNavigation
import proton.android.pass.features.searchoptions.SortingBottomsheetNavItem
import proton.android.pass.features.searchoptions.SortingLocation
import proton.android.pass.features.searchoptions.searchOptionsGraph
import proton.android.pass.features.selectitem.navigation.SelectItem
import proton.android.pass.features.selectitem.navigation.SelectItemNavigation
import proton.android.pass.features.selectitem.navigation.SelectItemState
import proton.android.pass.features.selectitem.navigation.selectItemGraph
import proton.android.pass.navigation.api.rememberAppNavigator
import proton.android.pass.navigation.api.rememberBottomSheetNavigator

@OptIn(ExperimentalMaterialNavigationApi::class)
@Composable
fun AutosaveAppContent(
    modifier: Modifier = Modifier,
    arguments: AutoSaveArguments,
    state: AutoSaveAppViewState.Ready,
    onItemSelectedForUpdate: (ShareId, ItemId, UserId) -> Unit,
    onClearSelectedItemForUpdate: () -> Unit,
    onNavigate: (AutosaveNavigation) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    val bottomSheetJob: MutableState<Job?> = remember { mutableStateOf(null) }

    if (state.needsAuth) {
        val bottomSheetState = rememberModalBottomSheetState(
            initialValue = ModalBottomSheetValue.Hidden,
            skipHalfExpanded = true
        )
        val appNavigator = rememberAppNavigator(
            bottomSheetNavigator = rememberBottomSheetNavigator(bottomSheetState)
        )
        PassModalBottomSheetLayout(bottomSheetNavigator = appNavigator.passBottomSheetNavigator) {
            NavHost(
                modifier = modifier.defaultMinSize(minHeight = 200.dp),
                navController = appNavigator.navController,
                startDestination = AUTH_GRAPH
            ) {
                authGraph(
                    canLogout = false,
                    navigation = {
                        when (it) {
                            is AuthNavigation.CloseScreen -> onNavigate(AutosaveNavigation.Cancel)
                            is AuthNavigation.Success -> onBottomSheetDismissed(
                                coroutineScope = coroutineScope,
                                modalBottomSheetState = bottomSheetState,
                                dismissJob = bottomSheetJob,
                                block = {}
                            )

                            AuthNavigation.Dismissed -> onNavigate(AutosaveNavigation.Cancel)
                            AuthNavigation.Failed -> onNavigate(AutosaveNavigation.Cancel)
                            is AuthNavigation.ForceSignOut ->
                                onNavigate(AutosaveNavigation.ForceSignOut(it.userId))

                            is AuthNavigation.EnterPin -> appNavigator.navigate(
                                destination = EnterPin,
                                route = EnterPin.buildRoute(it.origin)
                            )

                            is AuthNavigation.SignOut,
                            AuthNavigation.ForceSignOutAllUsers -> Unit

                            AuthNavigation.CloseBottomsheet -> onBottomSheetDismissed(
                                coroutineScope = coroutineScope,
                                modalBottomSheetState = bottomSheetState,
                                dismissJob = bottomSheetJob,
                                block = {}
                            )
                        }
                    }
                )
            }
        }
    } else {
        val bottomSheetState = rememberModalBottomSheetState(
            initialValue = ModalBottomSheetValue.Hidden,
            skipHalfExpanded = true
        )
        val appNavigator = rememberAppNavigator(
            bottomSheetNavigator = rememberBottomSheetNavigator(bottomSheetState)
        )

        val dismissBottomSheet: (() -> Unit) -> Unit = { block ->
            onBottomSheetDismissed(
                coroutineScope = coroutineScope,
                modalBottomSheetState = bottomSheetState,
                dismissJob = bottomSheetJob,
                block = block
            )
        }

        PassModalBottomSheetLayout(bottomSheetNavigator = appNavigator.passBottomSheetNavigator) {

            when (state.autosaveMode) {
                is AutosaveMode.Create -> {
                    NavHost(
                        modifier = modifier.defaultMinSize(minHeight = 200.dp),
                        navController = appNavigator.navController,
                        startDestination = CREATE_LOGIN_GRAPH
                    ) {
                        autosaveActivityGraph(
                            appNavigator = appNavigator,
                            arguments = arguments,
                            onNavigate = onNavigate,
                            dismissBottomSheet = dismissBottomSheet
                        )
                    }
                }

                is AutosaveMode.CreateOrUpdate -> {
                    val selectedUpdateState = state.autosaveMode.selectedUpdateState

                    if (selectedUpdateState == null) {
                        NavHost(
                            modifier = modifier.defaultMinSize(minHeight = 200.dp),
                            navController = appNavigator.navController,
                            startDestination = SelectItem.route
                        ) {
                            selectItemGraph(
                                state = SelectItemState.Autosave.Login(
                                    title = arguments.title,
                                    usernameFilter = state.autosaveMode.username,
                                    websiteFilter = state.autosaveMode.website,
                                    packageNameFilter = state.autosaveMode.packageName,
                                    updateFound = state.autosaveMode.updateFound
                                ),
                                onScreenShown = {},
                                onNavigate = {
                                    when (it) {
                                        is SelectItemNavigation.ItemSelected ->
                                            onItemSelectedForUpdate(
                                                it.item.shareId,
                                                it.item.id,
                                                it.item.userId
                                            )

                                        is SelectItemNavigation.SuggestionSelected ->
                                            onItemSelectedForUpdate(
                                                it.item.shareId,
                                                it.item.id,
                                                it.item.userId
                                            )

                                        SelectItemNavigation.AddItem -> appNavigator.navigate(
                                            destination = CreateLoginNavItem,
                                            route = CreateLoginNavItem.createNavRoute()
                                        )

                                        SelectItemNavigation.Cancel ->
                                            onNavigate(AutosaveNavigation.Cancel)

                                        is SelectItemNavigation.SortingBottomsheet ->
                                            appNavigator.navigate(
                                                SortingBottomsheetNavItem,
                                                SortingBottomsheetNavItem.createNavRoute(
                                                    location = SortingLocation.Autofill
                                                )
                                            )

                                        SelectItemNavigation.Upgrade ->
                                            onNavigate(AutosaveNavigation.Upgrade)

                                        SelectItemNavigation.SelectAccount ->
                                            appNavigator.navigate(AccountSwitchNavItem)

                                        is SelectItemNavigation.ItemOptions -> {}
                                    }
                                }
                            )
                            autosaveActivityGraph(
                                appNavigator = appNavigator,
                                arguments = arguments,
                                onNavigate = onNavigate,
                                dismissBottomSheet = dismissBottomSheet
                            )
                            searchOptionsGraph(
                                onNavigateEvent = {
                                    when (it) {
                                        is SearchOptionsNavigation.SelectSorting ->
                                            dismissBottomSheet {}

                                        SearchOptionsNavigation.ResetFilters,
                                        SearchOptionsNavigation.Filter,
                                        SearchOptionsNavigation.Sorting,
                                        SearchOptionsNavigation.BulkActions,
                                        is SearchOptionsNavigation.ManageFolder ->
                                            throw IllegalStateException("Action not supported")
                                    }
                                }
                            )
                            accountSwitchNavGraph {
                                when (it) {
                                    AccountSwitchNavigation.CreateItem -> dismissBottomSheet {
                                        appNavigator.navigate(
                                            destination = CreateLoginNavItem,
                                            route = CreateLoginNavItem.createNavRoute()
                                        )
                                    }

                                    AccountSwitchNavigation.CannotCreateItem ->
                                        dismissBottomSheet {}
                                }
                            }
                        }
                    } else {
                        NavHost(
                            modifier = modifier.defaultMinSize(minHeight = 200.dp),
                            navController = appNavigator.navController,
                            startDestination = EDIT_LOGIN_GRAPH
                        ) {
                            autosaveActivityGraph(
                                appNavigator = appNavigator,
                                arguments = arguments,
                                initialUpdateLoginUiState = selectedUpdateState,
                                onNavigate = onNavigate,
                                dismissBottomSheet = dismissBottomSheet,
                                onCloseEditScreen = onClearSelectedItemForUpdate
                            )
                        }
                    }
                }

                is AutosaveMode.Update -> {
                    NavHost(
                        modifier = modifier.defaultMinSize(minHeight = 200.dp),
                        navController = appNavigator.navController,
                        startDestination = EDIT_LOGIN_GRAPH
                    ) {
                        autosaveActivityGraph(
                            appNavigator = appNavigator,
                            arguments = arguments,
                            initialUpdateLoginUiState = state.autosaveMode.initialUpdateLoginUiState,
                            onNavigate = onNavigate,
                            dismissBottomSheet = dismissBottomSheet,
                            onCloseEditScreen = { onNavigate(AutosaveNavigation.Cancel) }
                        )
                    }
                }
            }
        }
    }
}
