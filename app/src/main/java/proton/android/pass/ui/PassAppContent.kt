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

package proton.android.pass.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import proton.android.pass.composecomponents.impl.bottomsheet.PassModalBottomSheetLayout
import proton.android.pass.composecomponents.impl.messages.OfflineIndicator
import proton.android.pass.composecomponents.impl.messages.PassSnackbarHost
import proton.android.pass.composecomponents.impl.messages.PassSnackbarHostState
import proton.android.pass.composecomponents.impl.messages.rememberPassSnackbarHostState
import proton.android.pass.featureauth.impl.AuthNavigation
import proton.android.pass.featureauth.impl.AuthScreen
import proton.android.pass.featurefeatureflags.impl.FeatureFlagRoute
import proton.android.pass.navigation.api.rememberAppNavigator
import proton.android.pass.navigation.api.rememberBottomSheetNavigator
import proton.android.pass.network.api.NetworkStatus
import proton.android.pass.notifications.api.SnackbarMessage
import proton.android.pass.ui.internal.InternalDrawerState
import proton.android.pass.ui.internal.InternalDrawerValue
import proton.android.pass.ui.internal.rememberInternalDrawerState

@OptIn(
    ExperimentalMaterialNavigationApi::class,
    ExperimentalMaterialApi::class
)
@Composable
fun PassAppContent(
    modifier: Modifier = Modifier,
    appUiState: AppUiState,
    onNavigate: (AppNavigation) -> Unit,
    onSnackbarMessageDelivered: () -> Unit
) {
    val bottomSheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )

    val bottomSheetNavigator = rememberBottomSheetNavigator(bottomSheetState)
    val appNavigator = rememberAppNavigator(bottomSheetNavigator)
    val scaffoldState = rememberScaffoldState()
    val passSnackbarHostState = rememberPassSnackbarHostState(scaffoldState.snackbarHostState)

    SnackBarLaunchedEffect(
        appUiState.snackbarMessage.value(),
        passSnackbarHostState,
        onSnackbarMessageDelivered
    )
    val internalDrawerState: InternalDrawerState =
        rememberInternalDrawerState(InternalDrawerValue.Closed)
    val coroutineScope: CoroutineScope = rememberCoroutineScope()
    Scaffold(
        modifier = modifier,
        scaffoldState = scaffoldState,
        snackbarHost = { PassSnackbarHost(snackbarHostState = passSnackbarHostState) }
    ) { contentPadding ->
        InternalDrawer(
            drawerState = internalDrawerState,
            onOpenFeatureFlag = {
                appNavigator.navigate(FeatureFlagRoute)
                coroutineScope.launch { internalDrawerState.close() }
            },
            content = {
                Column(modifier = Modifier.padding(contentPadding)) {
                    AnimatedVisibility(visible = appUiState.networkStatus == NetworkStatus.Offline) {
                        OfflineIndicator()
                    }
                    PassModalBottomSheetLayout(appNavigator.bottomSheetNavigator) {
                        if (appUiState.needsAuth) {
                            BackHandler { onNavigate(AppNavigation.Finish) }
                            AuthScreen(
                                navigation = {
                                    when (it) {
                                        AuthNavigation.Dismissed,
                                        AuthNavigation.Back -> onNavigate(AppNavigation.Finish)
                                        AuthNavigation.Success,
                                        AuthNavigation.Failed -> {}
                                    }
                                }
                            )
                        } else {
                            PassNavHost(
                                modifier = Modifier.weight(1f),
                                appNavigator = appNavigator,
                                startingRoute = Root.route,
                                onNavigate = onNavigate,
                                dismissBottomSheet = { callback ->
                                    coroutineScope.launch {
                                        bottomSheetState.hide()
                                        callback()
                                    }
                                }
                            )
                        }
                    }
                }
            }
        )
    }
}

@Composable
private fun SnackBarLaunchedEffect(
    snackBarMessage: SnackbarMessage?,
    passSnackBarHostState: PassSnackbarHostState,
    onSnackBarMessageDelivered: () -> Unit
) {
    snackBarMessage ?: return
    val snackBarMessageLocale = stringResource(id = snackBarMessage.id)
    LaunchedEffect(snackBarMessage) {
        passSnackBarHostState.showSnackbar(
            snackBarMessage.type,
            snackBarMessageLocale
        )
        onSnackBarMessageDelivered()
    }
}
