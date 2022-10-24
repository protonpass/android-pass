package me.proton.android.pass.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material.DrawerState
import androidx.compose.material.DrawerValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.rememberDrawerState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.navigation.animation.AnimatedNavHost
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.proton.android.pass.ui.internal.InternalDrawerState
import me.proton.android.pass.ui.internal.InternalDrawerValue
import me.proton.android.pass.ui.internal.rememberInternalDrawerState
import me.proton.android.pass.ui.navigation.NavItem
import me.proton.android.pass.ui.navigation.appGraph
import me.proton.android.pass.ui.navigation.rememberAnimatedNavController
import me.proton.android.pass.ui.navigation.rememberAppNavigator
import me.proton.android.pass.ui.shared.ConfirmSignOutDialog
import me.proton.core.compose.theme.ProtonTheme
import me.proton.pass.common.api.Some
import me.proton.pass.presentation.components.common.PassSnackbarHost
import me.proton.pass.presentation.components.common.rememberPassSnackbarHostState
import me.proton.pass.presentation.components.navigation.AuthNavigation
import me.proton.pass.presentation.components.navigation.drawer.ModalNavigationDrawer
import me.proton.pass.presentation.components.navigation.drawer.NavDrawerNavigation
import me.proton.pass.presentation.components.navigation.drawer.NavigationDrawerSection

@Composable
fun PassApp(
    modifier: Modifier = Modifier,
    authNavigation: AuthNavigation,
    appViewModel: AppViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        appViewModel.onStart()
    }

    ProtonTheme {
        ProvideWindowInsets {
            val appUiState by appViewModel.appUiState.collectAsStateWithLifecycle()
            PassAppContent(
                modifier = modifier,
                appUiState = appUiState,
                authNavigation = authNavigation,
                onDrawerSectionChanged = { appViewModel.onDrawerSectionChanged(it) },
                onSnackbarMessageDelivered = { appViewModel.onSnackbarMessageDelivered() }
            )
        }
    }
}


@OptIn(
    ExperimentalAnimationApi::class, ExperimentalMaterialApi::class,
    ExperimentalComposeUiApi::class
)
@Composable
fun PassAppContent(
    modifier: Modifier = Modifier,
    appUiState: AppUiState,
    startDestination: String = NavItem.Home.route,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
    authNavigation: AuthNavigation,
    onDrawerSectionChanged: (NavigationDrawerSection) -> Unit,
    onSnackbarMessageDelivered: () -> Unit
) {
    val navController = rememberAnimatedNavController()
    val appNavigator = rememberAppNavigator(navController)
    val scaffoldState: ScaffoldState = rememberScaffoldState()
    val passSnackbarHostState =
        rememberPassSnackbarHostState(scaffoldState.snackbarHostState)
    val navDrawerNavigation = NavDrawerNavigation(
        onNavHome = {
            onDrawerSectionChanged(NavigationDrawerSection.Items)
            appNavigator.navigate(NavItem.Home)
        },
        onNavSettings = {
            onDrawerSectionChanged(NavigationDrawerSection.Settings)
            appNavigator.navigate(NavItem.Settings)
        },
        onNavTrash = {
            onDrawerSectionChanged(NavigationDrawerSection.Trash)
            appNavigator.navigate(NavItem.Trash)
        },
        onNavHelp = {
            onDrawerSectionChanged(NavigationDrawerSection.Help)
            appNavigator.navigate(NavItem.Help)
        }
    )
    if (appUiState.snackbarMessage is Some) {
        val snackbarMessage = stringResource(id = appUiState.snackbarMessage.value.id)
        LaunchedEffect(appUiState.snackbarMessage.value) {
            passSnackbarHostState.showSnackbar(
                appUiState.snackbarMessage.value.type,
                snackbarMessage
            )
            onSnackbarMessageDelivered()
        }
    }
    BackHandler(drawerState.isOpen) { coroutineScope.launch { drawerState.close() } }
    Scaffold(
        modifier = modifier,
        scaffoldState = scaffoldState,
        snackbarHost = { PassSnackbarHost(snackbarHostState = passSnackbarHostState) }
    ) { contentPadding ->
        val internalDrawerState: InternalDrawerState =
            rememberInternalDrawerState(initialValue = InternalDrawerValue.Closed)
        InternalDrawer(
            drawerState = internalDrawerState
        ) {
            AnimatedNavHost(
                modifier = Modifier.padding(contentPadding),
                navController = navController,
                startDestination = startDestination
            ) {
                appGraph(
                    appNavigation = appNavigator,
                    navigationDrawer = { content ->
                        var showSignOutDialog by remember { mutableStateOf(false) }

                        ModalNavigationDrawer(
                            drawerUiState = appUiState.drawerUiState,
                            drawerState = drawerState,
                            navDrawerNavigation = navDrawerNavigation,
                            authNavigation = authNavigation,
                            onSignOutClick = { showSignOutDialog = true },
                            signOutDialog = {
                                if (showSignOutDialog) {
                                    ConfirmSignOutDialog(
                                        state = showSignOutDialog,
                                        onDismiss = { showSignOutDialog = false },
                                        onConfirm = { authNavigation.onRemove(null) }
                                    )
                                }
                            },
                            onInternalDrawerClick = { coroutineScope.launch { internalDrawerState.open() } },
                            content = content
                        )
                    },
                    onDrawerIconClick = { coroutineScope.launch { drawerState.open() } }
                )
            }
        }
    }
}
