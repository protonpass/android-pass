/*
 * Copyright (c) 2025-2026 Proton AG
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

package proton.android.pass.features.credentials.passwords.creation.ui

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
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import kotlinx.coroutines.Job
import proton.android.pass.commonui.api.onBottomSheetDismissed
import proton.android.pass.composecomponents.impl.bottomsheet.PassModalBottomSheetLayout
import proton.android.pass.features.auth.AUTH_GRAPH
import proton.android.pass.features.credentials.passwords.creation.navigation.PasswordCredentialCreationNavEvent
import proton.android.pass.features.credentials.passwords.creation.navigation.passwordCredentialCreationNavGraph
import proton.android.pass.features.credentials.passwords.creation.presentation.PasswordCredentialCreationState
import proton.android.pass.features.itemcreate.login.CREATE_LOGIN_GRAPH
import proton.android.pass.features.upsell.v2.navigation.UpsellV2NavItem
import proton.android.pass.features.upsell.v2.navigation.upsellV2NavGraph
import proton.android.pass.features.selectitem.navigation.SelectItem
import proton.android.pass.navigation.api.AppNavigator
import proton.android.pass.navigation.api.rememberBottomSheetNavigator

@[Composable OptIn(ExperimentalMaterialNavigationApi::class)]
internal fun PasswordCredentialCreationContent(
    modifier: Modifier = Modifier,
    state: PasswordCredentialCreationState.Ready,
    onNavigate: (PasswordCredentialCreationNavEvent) -> Unit
) = with(state) {
    val coroutineScope = rememberCoroutineScope()

    if (isBiometricAuthRequired) {
        val bottomSheetJob: MutableState<Job?> = remember { mutableStateOf(null) }
        val bottomSheetState = rememberModalBottomSheetState(
            initialValue = ModalBottomSheetValue.Hidden,
            skipHalfExpanded = true
        )
        val bottomSheetNavigator = rememberBottomSheetNavigator(bottomSheetState)
        val navController = rememberNavController(bottomSheetNavigator)
        val appNavigator = remember(navController, bottomSheetNavigator) {
            AppNavigator(navController, bottomSheetNavigator)
        }

        PassModalBottomSheetLayout(bottomSheetNavigator = appNavigator.passBottomSheetNavigator) {
            NavHost(
                modifier = modifier.defaultMinSize(minHeight = 200.dp),
                navController = appNavigator.navController,
                startDestination = AUTH_GRAPH
            ) {
                passwordCredentialCreationNavGraph(
                    appNavigator = appNavigator,
                    initialCreateLoginUiState = initialCreateLoginUiState,
                    selectItemState = selectItemState,
                    onNavigate = { nav ->
                        if (nav == PasswordCredentialCreationNavEvent.Upgrade && supportPayment) {
                            appNavigator.navigate(
                                destination = UpsellV2NavItem,
                                force = true,
                                route = UpsellV2NavItem.createRoute(manualDisplay = true)
                            )
                        } else {
                            onNavigate(nav)
                        }
                    },
                    dismissBottomSheet = { block ->
                        onBottomSheetDismissed(
                            coroutineScope = coroutineScope,
                            modalBottomSheetState = bottomSheetState,
                            dismissJob = bottomSheetJob,
                            block = block
                        )
                    }
                )
                upsellV2NavGraph(
                    onNextScreen = { _ -> appNavigator.navigateBack() },
                    onSkip = { _ -> appNavigator.navigateBack() },
                    onNavigateBack = { _ -> appNavigator.navigateBack() }
                )
            }
        }
    } else {
        val startDestination = if (hasSingleAccount) CREATE_LOGIN_GRAPH else SelectItem.route
        val bottomSheetJob: MutableState<Job?> = remember { mutableStateOf(null) }
        val bottomSheetState = rememberModalBottomSheetState(
            initialValue = ModalBottomSheetValue.Hidden,
            skipHalfExpanded = true
        )
        val bottomSheetNavigator = rememberBottomSheetNavigator(bottomSheetState)
        val navController = rememberNavController(bottomSheetNavigator)
        val appNavigator = remember(navController, bottomSheetNavigator) {
            AppNavigator(navController, bottomSheetNavigator)
        }

        PassModalBottomSheetLayout(bottomSheetNavigator = appNavigator.passBottomSheetNavigator) {
            NavHost(
                modifier = modifier.defaultMinSize(minHeight = 200.dp),
                navController = appNavigator.navController,
                startDestination = startDestination
            ) {
                passwordCredentialCreationNavGraph(
                    appNavigator = appNavigator,
                    initialCreateLoginUiState = initialCreateLoginUiState,
                    selectItemState = selectItemState,
                    onNavigate = { nav ->
                        if (nav == PasswordCredentialCreationNavEvent.Upgrade && supportPayment) {
                            appNavigator.navigate(
                                destination = UpsellV2NavItem,
                                force = true,
                                route = UpsellV2NavItem.createRoute(manualDisplay = true)
                            )
                        } else {
                            onNavigate(nav)
                        }
                    },
                    dismissBottomSheet = { block ->
                        onBottomSheetDismissed(
                            coroutineScope = coroutineScope,
                            modalBottomSheetState = bottomSheetState,
                            dismissJob = bottomSheetJob,
                            block = block
                        )
                    }
                )
                upsellV2NavGraph(
                    onNextScreen = { _ -> appNavigator.navigateBack() },
                    onSkip = { _ -> appNavigator.navigateBack() },
                    onNavigateBack = { _ -> appNavigator.navigateBack() }
                )
            }
        }
    }
}
