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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarResult
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import proton.android.pass.R
import proton.android.pass.composecomponents.impl.bottomsheet.PassModalBottomSheetLayout
import proton.android.pass.composecomponents.impl.messages.OfflineIndicator
import proton.android.pass.composecomponents.impl.messages.PassSnackbarHost
import proton.android.pass.composecomponents.impl.messages.PassSnackbarHostState
import proton.android.pass.composecomponents.impl.messages.rememberPassSnackbarHostState
import proton.android.pass.featurefeatureflags.impl.FeatureFlagRoute
import proton.android.pass.inappupdates.api.InAppUpdateState
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.rememberAppNavigator
import proton.android.pass.navigation.api.rememberBottomSheetNavigator
import proton.android.pass.network.api.NetworkStatus
import proton.android.pass.notifications.api.SnackbarMessage
import proton.android.pass.notifications.api.SnackbarType
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
    onSnackbarMessageDelivered: () -> Unit,
    onCompleteUpdate: () -> Unit
) {
    val coroutineScope: CoroutineScope = rememberCoroutineScope()

    val bottomSheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )

    val bottomSheetNavigator = rememberBottomSheetNavigator(bottomSheetState)
    val appNavigator = rememberAppNavigator(bottomSheetNavigator)

    val backStack by appNavigator.navController.currentBackStack.collectAsStateWithLifecycle()
    LaunchedEffect(backStack) {
        if (backStack.isNotEmpty()) {
            PassLogger.i(
                TAG,
                "NavigationBackStack: ${backStack.map { it.destination.route }.joinToString()}"
            )
        }
    }

    val scaffoldState = rememberScaffoldState()
    val passSnackbarHostState = rememberPassSnackbarHostState(scaffoldState.snackbarHostState)

    SnackBarLaunchedEffect(
        appUiState.snackbarMessage.value(),
        passSnackbarHostState,
        onSnackbarMessageDelivered
    )

    if (appUiState.inAppUpdateState is InAppUpdateState.Downloaded) {
        val snackbarMessage = stringResource(R.string.restart_to_complete_the_update)
        val snackbarAction = stringResource(R.string.action_restart)
        LaunchedEffect(Unit) {
            val result = passSnackbarHostState.showSnackbar(
                message = snackbarMessage,
                actionLabel = snackbarAction,
                type = SnackbarType.NORM,
                duration = SnackbarDuration.Indefinite
            )
            when (result) {
                SnackbarResult.ActionPerformed -> onCompleteUpdate()
                SnackbarResult.Dismissed -> {}
            }
        }
    }
    val internalDrawerState: InternalDrawerState =
        rememberInternalDrawerState(InternalDrawerValue.Closed)
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
            onAppNavigation = onNavigate,
            content = {
                Column(modifier = Modifier.padding(contentPadding)) {
                    AnimatedVisibility(
                        visible = appUiState.networkStatus == NetworkStatus.Offline,
                        label = "PassAppContent-OfflineIndicator"
                    ) {
                        OfflineIndicator()
                    }
                    AnimatedVisibility(
                        visible = appUiState.inAppUpdateState is InAppUpdateState.Downloading,
                        label = "PassAppContent-InAppUpdateIndicator"
                    ) {
                        if (appUiState.inAppUpdateState !is InAppUpdateState.Downloading) return@AnimatedVisibility
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            progress = appUiState.inAppUpdateState.progress
                        )
                    }
                    if (appUiState.needsAuth) {
                        val unAuthBottomSheetState = rememberModalBottomSheetState(
                            initialValue = ModalBottomSheetValue.Hidden,
                            skipHalfExpanded = true
                        )

                        val unAuthBottomSheetNavigator =
                            rememberBottomSheetNavigator(unAuthBottomSheetState)
                        val unAuthAppNavigator = rememberAppNavigator(unAuthBottomSheetNavigator)
                        PassModalBottomSheetLayout(unAuthAppNavigator.passBottomSheetNavigator) {
                            PassUnAuthNavHost(
                                appNavigator = unAuthAppNavigator,
                                onNavigate = onNavigate
                            )
                        }
                    } else {
                        PassModalBottomSheetLayout(appNavigator.passBottomSheetNavigator) {
                            PassNavHost(
                                modifier = Modifier.weight(1f),
                                appNavigator = appNavigator,
                                onNavigate = onNavigate,
                                dismissBottomSheet = { block ->
                                    onBottomSheetDismissed(
                                        coroutineScope = coroutineScope,
                                        modalBottomSheetState = bottomSheetState,
                                        block = block
                                    )
                                }
                            )
                        }
                    }
                }
            }
        )
    }
}

private const val TAG = "PassAppContent"

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

@OptIn(ExperimentalMaterialApi::class)
private fun onBottomSheetDismissed(
    coroutineScope: CoroutineScope,
    modalBottomSheetState: ModalBottomSheetState,
    block: () -> Unit
) {
    coroutineScope.launch {
        if (modalBottomSheetState.isVisible) {
            try {
                modalBottomSheetState.hide()
            } catch (e: CancellationException) {
                PassLogger.d(TAG, e, "Bottom sheet hidden animation interrupted")
            }
        }
        block()
    }
}
