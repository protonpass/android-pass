/*
 * Copyright (c) 2023-2026 Proton AG
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
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.proton.core.domain.entity.UserId
import proton.android.pass.R
import proton.android.pass.commonpresentation.api.bars.bottom.home.presentation.BottomBarSelection
import proton.android.pass.commonpresentation.api.bars.bottom.home.presentation.HomeBottomBarEvent
import proton.android.pass.commonui.api.BrowserUtils
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.applyIf
import proton.android.pass.commonui.api.onBottomSheetDismissed
import proton.android.pass.composecomponents.impl.bottombar.PassHomeBottomBar
import proton.android.pass.composecomponents.impl.bottomsheet.PassModalBottomSheetLayout
import proton.android.pass.composecomponents.impl.messages.OfflineIndicator
import proton.android.pass.composecomponents.impl.messages.PassSnackbarHost
import proton.android.pass.composecomponents.impl.messages.rememberPassSnackbarHostState
import proton.android.pass.composecomponents.impl.snackbar.SnackBarLaunchedEffect
import proton.android.pass.domain.inappmessages.InAppMessage
import proton.android.pass.domain.inappmessages.InAppMessageId
import proton.android.pass.domain.inappmessages.InAppMessageKey
import proton.android.pass.features.auth.AuthOrigin
import proton.android.pass.features.home.localinappmessages.LocalInAppMessageBanner
import proton.android.pass.features.home.localinappmessages.LocalInAppMessagesEvent
import proton.android.pass.features.home.localinappmessages.NotificationPermissionLaunchedEffect
import proton.android.pass.features.sl.sync.settings.navigation.SimpleLoginSyncSettingsNavItem
import proton.android.pass.features.explore.navigation.ExploreNavItem
import proton.android.pass.features.featureflags.FeatureFlagRoute
import proton.android.pass.features.home.HomeNavItem
import proton.android.pass.features.home.HomeSnackbarMessageWithAction
import proton.android.pass.features.inappmessages.banner.ui.InAppMessageBanner
import proton.android.pass.features.itemcreate.bottomsheets.createitem.CreateItemBottomSheetMode
import proton.android.pass.features.itemcreate.bottomsheets.createitem.CreateItemBottomsheetNavItem
import proton.android.pass.features.profile.ProfileNavItem
import proton.android.pass.features.searchoptions.FilterBottomsheetNavItem
import proton.android.pass.features.searchoptions.SearchOptionsBottomsheetNavItem
import proton.android.pass.features.searchoptions.SortingBottomsheetNavItem
import proton.android.pass.features.security.center.home.navigation.SecurityCenterHomeNavItem
import proton.android.pass.features.upsell.v2.navigation.UpsellV2NavItem
import proton.android.pass.inappupdates.api.InAppUpdateState
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.AppNavigator
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.rememberAppNavigator
import proton.android.pass.navigation.api.rememberBottomSheetNavigator
import proton.android.pass.network.api.NetworkStatus
import proton.android.pass.notifications.api.SnackbarType
import proton.android.pass.ui.internal.InternalDrawerState
import proton.android.pass.ui.internal.InternalDrawerValue
import proton.android.pass.ui.internal.rememberInternalDrawerState
import proton.android.pass.ui.navigation.UN_AUTH_GRAPH
import proton.android.pass.ui.navigation.appGraph
import proton.android.pass.ui.navigation.unAuthGraph

private const val PROTON_RECOVER_URL = "https://proton.me/support/recover-encrypted-messages-files"
private const val BANNER_ANIM_DURATION_MS = 300

private fun InAppMessage.Banner.stableId(): String = when (this) {
    is InAppMessage.Local.Autofill -> "local:autofill"
    is InAppMessage.Local.NotificationPermission -> "local:notification"
    is InAppMessage.Local.SLSync -> "local:slsync"
    is InAppMessage.Remote.Banner -> id.value
}


@OptIn(ExperimentalMaterialNavigationApi::class)
@Composable
fun PassAppContent(
    modifier: Modifier = Modifier,
    appUiState: AppUiState,
    onNavigate: (AppNavigation) -> Unit,
    onSnackbarMessageDelivered: () -> Unit,
    onInAppMessageBannerRead: (UserId, InAppMessageId, InAppMessageKey) -> Unit,
    onInAppMessageBannerDisplayed: (InAppMessageKey) -> Unit,
    onInAppMessageBannerCTAClicked: (InAppMessageKey) -> Unit,
    onCompleteUpdate: () -> Unit,
    onLocalInAppMessageDismiss: (InAppMessage.Local) -> Unit,
    onLocalInAppMessageClick: (InAppMessage.Local) -> Unit,
    onLocalInAppMessageEventConsumed: () -> Unit,
    onNotificationPermissionChanged: (Boolean) -> Unit,
    needsAuth: Boolean,
    supportPayment: Boolean
) {
    val context = LocalContext.current
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
    val isSnackbarVisible by remember { derivedStateOf { scaffoldState.snackbarHostState.currentSnackbarData != null } }
    val bannerBottomPadding by animateDpAsState(
        targetValue = if (isSnackbarVisible) 72.dp else Spacing.medium,
        animationSpec = tween(),
        label = "BannerBottomPadding"
    )
    val localInAppMessageEvent = appUiState.localInAppMessageEvent

    SnackBarLaunchedEffect(
        snackBarMessage = appUiState.snackbarMessage.value(),
        passSnackBarHostState = passSnackbarHostState,
        onSnackBarMessageDelivered = {
            if (it == SnackbarResult.ActionPerformed) {
                when (appUiState.snackbarMessage.value()) {
                    HomeSnackbarMessageWithAction.InactiveVaultError -> {
                        BrowserUtils.openWebsite(
                            context = context,
                            website = PROTON_RECOVER_URL
                        )
                    }

                    else -> {}
                }
            }

            onSnackbarMessageDelivered()
        }
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
    val bottomBarSelected = remember(appNavigator.currentRoute) {
        determineBottomBarSelection(appNavigator.currentRoute)
    }
    val shouldShowBottomBar = !needsAuth && bottomBarSelected != BottomBarSelection.None
    val bottomSheetJob: MutableState<Job?> = remember { mutableStateOf(null) }

    var shouldRequestNotificationPermission by remember { mutableStateOf(false) }

    NotificationPermissionLaunchedEffect(
        shouldRequestPermissions = shouldRequestNotificationPermission,
        onPermissionRequested = {
            shouldRequestNotificationPermission = false
            onLocalInAppMessageEventConsumed()
        },
        onPermissionChanged = onNotificationPermissionChanged
    )

    LaunchedEffect(localInAppMessageEvent) {
        when (val event = localInAppMessageEvent) {
            is LocalInAppMessagesEvent.OpenSLSyncSettings -> {
                appNavigator.navigate(
                    destination = SimpleLoginSyncSettingsNavItem,
                    route = SimpleLoginSyncSettingsNavItem.createNavRoute(event.shareId)
                )
                onLocalInAppMessageEventConsumed()
            }
            LocalInAppMessagesEvent.RequestNotificationPermission -> {
                shouldRequestNotificationPermission = true
            }
            LocalInAppMessagesEvent.Unknown -> {}
        }
    }

    Scaffold(
        modifier = modifier,
        scaffoldState = scaffoldState,
        snackbarHost = {
            PassSnackbarHost(
                modifier = Modifier.applyIf(
                    condition = !shouldShowBottomBar,
                    ifTrue = {
                        Modifier.navigationBarsPadding()
                    }
                ),
                snackbarHostState = passSnackbarHostState
            )
        },
        bottomBar = {
            AnimatedVisibility(
                visible = shouldShowBottomBar,
                enter = slideInVertically { it }, // Slide in from the bottom
                exit = slideOutVertically { it } // Slide out to the bottom
            ) {
                PassHomeBottomBar(
                    selection = bottomBarSelected,
                    showExplore = appUiState.showExplore,
                    onEvent = {
                        handleBottomBarEvent(
                            event = it,
                            appNavigator = appNavigator,
                            coroutineScope = coroutineScope,
                            bottomSheetState = bottomSheetState,
                            currentRoute = appNavigator.currentRoute,
                            bottomSheetJob = bottomSheetJob
                        )
                    }
                )
            }
        }
    ) { contentPadding ->
        InternalDrawer(
            bottomPadding = contentPadding.calculateBottomPadding(),
            drawerState = internalDrawerState,
            onOpenFeatureFlag = {
                appNavigator.navigate(FeatureFlagRoute)
                coroutineScope.launch { internalDrawerState.close() }
            },
            onAppNavigation = onNavigate,
            content = {
                Box(
                    modifier = Modifier
                        .padding(
                            bottom = if (shouldShowBottomBar) contentPadding.calculateBottomPadding() else 0.dp
                        )
                        .animateContentSize()
                ) {
                    Column {
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
                        if (needsAuth) {
                            val unAuthBottomSheetState = rememberModalBottomSheetState(
                                initialValue = ModalBottomSheetValue.Hidden,
                                skipHalfExpanded = true
                            )

                            val unAuthBottomSheetNavigator =
                                rememberBottomSheetNavigator(unAuthBottomSheetState)
                            val unAuthAppNavigator =
                                rememberAppNavigator(unAuthBottomSheetNavigator)
                            PassModalBottomSheetLayout(unAuthAppNavigator.passBottomSheetNavigator) {
                                PassNavHost(
                                    modifier = Modifier.weight(1f),
                                    appNavigator = unAuthAppNavigator,
                                    startDestination = UN_AUTH_GRAPH,
                                    graph = {
                                        unAuthGraph(
                                            appNavigator = unAuthAppNavigator,
                                            onNavigate = onNavigate,
                                            origin = AuthOrigin.AUTO_LOCK,
                                            dismissBottomSheet = { block ->
                                                onBottomSheetDismissed(
                                                    coroutineScope = coroutineScope,
                                                    modalBottomSheetState = unAuthBottomSheetState,
                                                    dismissJob = bottomSheetJob,
                                                    block = block
                                                )
                                            }
                                        )
                                    }
                                )
                            }
                        } else {
                            BackHandler { appNavigator.navigateBack() }

                            PassModalBottomSheetLayout(appNavigator.passBottomSheetNavigator) {
                                PassNavHost(
                                    modifier = Modifier.weight(1f),
                                    appNavigator = appNavigator,
                                    graph = {
                                        appGraph(
                                            appNavigator = appNavigator,
                                            onNavigate = {
                                                when (it) {
                                                    is AppNavigation.Upgrade -> {
                                                        if (supportPayment) {
                                                            appNavigator.navigate(
                                                                destination = UpsellV2NavItem,
                                                                force = true,
                                                                route = UpsellV2NavItem.createRoute(
                                                                    manualDisplay = true
                                                                )
                                                            )
                                                        } else {
                                                            onNavigate(it)
                                                        }
                                                    }

                                                    else -> {
                                                        onNavigate(it)
                                                    }
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
                                    }
                                )
                            }
                        }
                    }

                    // Local display list: mirrors ViewModel but keeps items alive during exit animation
                    var displayMessages by remember { mutableStateOf(emptyList<InAppMessage.Banner>()) }
                    var dismissingIds by remember { mutableStateOf(emptySet<String>()) }

                    LaunchedEffect(appUiState.inAppMessages) {
                        val incomingIds = appUiState.inAppMessages.map { it.stableId() }.toSet()
                        val kept = displayMessages.filter { msg ->
                            msg.stableId() in incomingIds || msg.stableId() in dismissingIds
                        }
                        val arrivals = appUiState.inAppMessages.filterNot { msg ->
                            kept.any { it.stableId() == msg.stableId() }
                        }
                        displayMessages = kept + arrivals
                    }

                    val animateDismiss: (String, () -> Unit) -> Unit = remember(coroutineScope) {
                        { id, realDismiss ->
                            dismissingIds = dismissingIds + id
                            coroutineScope.launch {
                                delay(BANNER_ANIM_DURATION_MS.toLong())
                                displayMessages = displayMessages.filterNot { it.stableId() == id }
                                realDismiss()
                                dismissingIds = dismissingIds - id
                            }
                        }
                    }

                    AnimatedVisibility(
                        modifier = Modifier.align(Alignment.BottomCenter),
                        visible = displayMessages.isNotEmpty() &&
                            appNavigator.currentRoute == HomeNavItem.route,
                        enter = EnterTransition.None,
                        exit = ExitTransition.None
                    ) {
                        Column(
                            modifier = Modifier.padding(bottom = bannerBottomPadding),
                            verticalArrangement = Arrangement.spacedBy(Spacing.small)
                        ) {
                            displayMessages.forEach { message ->
                                val msgId = message.stableId()

                                key(msgId) {
                                    var entered by remember { mutableStateOf(false) }
                                    LaunchedEffect(Unit) { entered = true }

                                    AnimatedVisibility(
                                        visible = entered && msgId !in dismissingIds,
                                        enter = fadeIn(tween(BANNER_ANIM_DURATION_MS)) +
                                            expandVertically(tween(BANNER_ANIM_DURATION_MS)) +
                                            slideInVertically(tween(BANNER_ANIM_DURATION_MS)) { it / 2 },
                                        exit = fadeOut(tween(BANNER_ANIM_DURATION_MS)) +
                                            shrinkVertically(tween(BANNER_ANIM_DURATION_MS)) +
                                            slideOutVertically(tween(BANNER_ANIM_DURATION_MS)) { it / 2 }
                                    ) {
                                        when (message) {
                                            is InAppMessage.Local -> LocalInAppMessageBanner(
                                                message = message,
                                                onClick = { onLocalInAppMessageClick(message) },
                                                onDismiss = {
                                                    animateDismiss(msgId) {
                                                        onLocalInAppMessageDismiss(message)
                                                    }
                                                }
                                            )
                                            is InAppMessage.Remote.Banner -> InAppMessageBanner(
                                                inAppMessage = message,
                                                onDismiss = { userId, id, key ->
                                                    animateDismiss(msgId) {
                                                        onInAppMessageBannerRead(userId, id, key)
                                                    }
                                                },
                                                onInternalCTAClick = { userId, id, key, value ->
                                                    animateDismiss(msgId) {
                                                        onInAppMessageBannerRead(userId, id, key)
                                                    }
                                                    onInAppMessageBannerCTAClicked(key)
                                                    appNavigator.navigateToDeeplink(value)
                                                },
                                                onExternalCTAClick = { userId, id, key, value ->
                                                    animateDismiss(msgId) {
                                                        onInAppMessageBannerRead(userId, id, key)
                                                    }
                                                    onInAppMessageBannerCTAClicked(key)
                                                    BrowserUtils.openWebsite(context, value)
                                                },
                                                onDisplay = { key -> onInAppMessageBannerDisplayed(key) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        )
    }
}

private fun determineBottomBarSelection(route: String?): BottomBarSelection = when (route) {
    HomeNavItem.route,
    SortingBottomsheetNavItem.route,
    SearchOptionsBottomsheetNavItem.route,
    FilterBottomsheetNavItem.route -> BottomBarSelection.Home

    ProfileNavItem.route -> BottomBarSelection.Profile
    SecurityCenterHomeNavItem.route -> BottomBarSelection.SecurityCenter
    ExploreNavItem.route -> BottomBarSelection.Explore
    CreateItemBottomsheetNavItem.route -> BottomBarSelection.ItemCreate
    else -> BottomBarSelection.None
}

@Suppress("LongParameterList")
private fun handleBottomBarEvent(
    event: HomeBottomBarEvent,
    appNavigator: AppNavigator,
    coroutineScope: CoroutineScope,
    bottomSheetState: ModalBottomSheetState,
    currentRoute: String?,
    bottomSheetJob: MutableState<Job?>
) {
    val (destination, route) = when (event) {
        HomeBottomBarEvent.OnHomeSelected -> HomeNavItem to null
        HomeBottomBarEvent.OnNewItemSelected ->
            CreateItemBottomsheetNavItem to
                CreateItemBottomsheetNavItem.createNavRoute(CreateItemBottomSheetMode.HomeFull)

        HomeBottomBarEvent.OnProfileSelected -> ProfileNavItem to null
        HomeBottomBarEvent.OnSecurityCenterSelected -> SecurityCenterHomeNavItem to null
        HomeBottomBarEvent.OnExploreSelected -> ExploreNavItem to null
    }

    if (event == HomeBottomBarEvent.OnNewItemSelected && currentRoute == CreateItemBottomsheetNavItem.route) return

    navigateWithDismiss(
        destination = destination,
        route = route,
        appNavigator = appNavigator,
        coroutineScope = coroutineScope,
        bottomSheetState = bottomSheetState,
        bottomSheetJob = bottomSheetJob
    )
}

@Suppress("LongParameterList")
private fun navigateWithDismiss(
    destination: NavItem,
    route: String?,
    appNavigator: AppNavigator,
    coroutineScope: CoroutineScope,
    bottomSheetState: ModalBottomSheetState,
    bottomSheetJob: MutableState<Job?>
) {
    onBottomSheetDismissed(
        coroutineScope = coroutineScope,
        modalBottomSheetState = bottomSheetState,
        dismissJob = bottomSheetJob
    ) {
        val backDestination = if (destination == CreateItemBottomsheetNavItem) {
            appNavigator.findCloserDestination(
                HomeNavItem,
                ProfileNavItem,
                SecurityCenterHomeNavItem,
                ExploreNavItem
            )
        } else null

        appNavigator.navigate(destination, route, backDestination)
    }
}

private const val TAG = "PassAppContent"
