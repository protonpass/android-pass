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

package proton.android.pass.features.credentials.passkeys.selection.ui

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import kotlinx.coroutines.Job
import proton.android.pass.commonui.api.onBottomSheetDismissed
import proton.android.pass.composecomponents.impl.bottomsheet.PassModalBottomSheetLayout
import proton.android.pass.features.auth.AUTH_GRAPH
import proton.android.pass.features.auth.AuthNavigation
import proton.android.pass.features.auth.EnterPin
import proton.android.pass.features.auth.authGraph
import proton.android.pass.features.credentials.passkeys.selection.navigation.PasskeyCredentialSelectionNavEvent
import proton.android.pass.features.credentials.passkeys.selection.navigation.passkeyCredentialSelectionNavGraph
import proton.android.pass.features.credentials.passkeys.selection.presentation.PasskeyCredentialSelectionActionAfterAuth
import proton.android.pass.features.credentials.passkeys.selection.presentation.PasskeyCredentialSelectionEvent
import proton.android.pass.features.credentials.passkeys.selection.presentation.PasskeyCredentialSelectionState
import proton.android.pass.features.credentials.passkeys.selection.presentation.PasskeyCredentialSelectionStateEvent
import proton.android.pass.features.passkeys.select.navigation.SelectPasskeyBottomsheet
import proton.android.pass.features.selectitem.navigation.SelectItem
import proton.android.pass.navigation.api.rememberAppNavigator
import proton.android.pass.navigation.api.rememberBottomSheetNavigator

@[Composable OptIn(ExperimentalMaterialNavigationApi::class)]
internal fun PasskeyCredentialSelectionContent(
    modifier: Modifier = Modifier,
    state: PasskeyCredentialSelectionState.Ready,
    onNavigate: (PasskeyCredentialSelectionNavEvent) -> Unit,
    onEvent: (PasskeyCredentialSelectionEvent) -> Unit
) = with(state) {

    val bottomSheetJob: MutableState<Job?> = remember { mutableStateOf(null) }

    val coroutineScope = rememberCoroutineScope()

    if (isBiometricAuthRequired) {
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
                    navigation = { destination ->
                        when (destination) {
                            AuthNavigation.CloseBottomsheet -> onBottomSheetDismissed(
                                coroutineScope = coroutineScope,
                                modalBottomSheetState = bottomSheetState,
                                dismissJob = bottomSheetJob,
                                block = {}
                            )

                            is AuthNavigation.CloseScreen,
                            AuthNavigation.Dismissed,
                            AuthNavigation.Failed -> {
                                onNavigate(PasskeyCredentialSelectionNavEvent.Cancel)
                            }

                            is AuthNavigation.EnterPin -> {
                                appNavigator.navigate(
                                    destination = EnterPin,
                                    route = EnterPin.buildRoute(origin = destination.origin)
                                )
                            }

                            is AuthNavigation.ForceSignOut -> {
                                PasskeyCredentialSelectionNavEvent.ForceSignOut(
                                    userId = destination.userId
                                ).also(onNavigate)
                            }

                            is AuthNavigation.Success -> onBottomSheetDismissed(
                                coroutineScope = coroutineScope,
                                modalBottomSheetState = bottomSheetState,
                                dismissJob = bottomSheetJob,
                                block = {
                                    when (actionAfterAuth) {
                                        PasskeyCredentialSelectionActionAfterAuth.SelectItem -> {
                                            appNavigator.navigate(SelectItem)
                                        }

                                        PasskeyCredentialSelectionActionAfterAuth.EmitEvent -> {
                                            onEvent(PasskeyCredentialSelectionEvent.OnAuthPerformed)
                                        }
                                    }
                                }
                            )

                            is AuthNavigation.SignOut,
                            AuthNavigation.ForceSignOutAllUsers -> Unit
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
        LaunchedEffect(key1 = state.event) {
            when (val event = state.event) {
                PasskeyCredentialSelectionStateEvent.Idle -> Unit
                PasskeyCredentialSelectionStateEvent.Cancel -> {
                    onNavigate(PasskeyCredentialSelectionNavEvent.Cancel)
                }

                is PasskeyCredentialSelectionStateEvent.SelectPasskeyFromItem -> {
                    appNavigator.navigate(
                        destination = SelectPasskeyBottomsheet,
                        route = SelectPasskeyBottomsheet.buildRoute(
                            shareId = event.item.shareId,
                            itemId = event.item.id
                        )
                    )
                }

                is PasskeyCredentialSelectionStateEvent.SendResponse -> {
                    PasskeyCredentialSelectionNavEvent.SendResponse(
                        response = event.response
                    ).also(onNavigate)
                }
            }

            onEvent(PasskeyCredentialSelectionEvent.OnEventConsumed(event))
        }

        PassModalBottomSheetLayout(bottomSheetNavigator = appNavigator.passBottomSheetNavigator) {
            NavHost(
                modifier = modifier.defaultMinSize(minHeight = 200.dp),
                navController = appNavigator.navController,
                startDestination = SelectItem.route
            ) {
                passkeyCredentialSelectionNavGraph(
                    appNavigator = appNavigator,
                    passkeyDomain = request.requestOrigin,
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
