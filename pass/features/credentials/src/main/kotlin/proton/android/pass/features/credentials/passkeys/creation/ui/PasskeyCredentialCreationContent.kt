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

package proton.android.pass.features.credentials.passkeys.creation.ui

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
import proton.android.pass.features.auth.AuthNavigation
import proton.android.pass.features.auth.EnterPin
import proton.android.pass.features.auth.authGraph
import proton.android.pass.features.credentials.passkeys.creation.navigation.PasskeyCredentialCreationNavEvent
import proton.android.pass.features.credentials.passkeys.creation.navigation.passkeyCredentialCreationNavGraph
import proton.android.pass.features.credentials.passkeys.creation.presentation.PasskeyCredentialCreationEvent
import proton.android.pass.features.credentials.passkeys.creation.presentation.PasskeyCredentialCreationState
import proton.android.pass.features.selectitem.navigation.SelectItem
import proton.android.pass.navigation.api.AppNavigator
import proton.android.pass.navigation.api.rememberBottomSheetNavigator

@[Composable OptIn(ExperimentalMaterialNavigationApi::class)]
internal fun PasskeyCredentialCreationContent(
    modifier: Modifier = Modifier,
    state: PasskeyCredentialCreationState.Ready,
    onNavigate: (PasskeyCredentialCreationNavEvent) -> Unit,
    onEvent: (PasskeyCredentialCreationEvent) -> Unit
) = with(state) {

    val bottomSheetJob: MutableState<Job?> = remember { mutableStateOf(null) }

    val coroutineScope = rememberCoroutineScope()

    if (isBiometricAuthRequired) {
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
                authGraph(
                    canLogout = false,
                    navigation = { destination ->
                        when (destination) {
                            is AuthNavigation.CloseScreen,
                            AuthNavigation.Dismissed,
                            AuthNavigation.Failed -> onNavigate(PasskeyCredentialCreationNavEvent.Cancel)

                            is AuthNavigation.Success -> onBottomSheetDismissed(
                                coroutineScope = coroutineScope,
                                modalBottomSheetState = bottomSheetState,
                                dismissJob = bottomSheetJob,
                                block = {}
                            )

                            is AuthNavigation.ForceSignOut -> {
                                PasskeyCredentialCreationNavEvent.ForceSignOut(
                                    userId = destination.userId
                                ).also(onNavigate)
                            }

                            is AuthNavigation.EnterPin -> appNavigator.navigate(
                                destination = EnterPin,
                                route = EnterPin.buildRoute(
                                    origin = destination.origin
                                )
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
        val bottomSheetNavigator = rememberBottomSheetNavigator(bottomSheetState)
        val navController = rememberNavController(bottomSheetNavigator)
        val appNavigator = remember(navController, bottomSheetNavigator) {
            AppNavigator(navController, bottomSheetNavigator)
        }

        PassModalBottomSheetLayout(bottomSheetNavigator = appNavigator.passBottomSheetNavigator) {
            NavHost(
                modifier = modifier.defaultMinSize(minHeight = 200.dp),
                navController = appNavigator.navController,
                startDestination = SelectItem.route
            ) {
                passkeyCredentialCreationNavGraph(
                    appNavigator = appNavigator,
                    initialCreateLoginUiState = initialCreateLoginUiState,
                    selectItemState = selectItemState,
                    onNavigate = onNavigate,
                    onEvent = onEvent,
                    dismissBottomSheet = { block ->
                        onBottomSheetDismissed(
                            coroutineScope = coroutineScope,
                            modalBottomSheetState = bottomSheetState,
                            dismissJob = bottomSheetJob,
                            block = block
                        )
                    }
                )
            }
        }
    }

}
