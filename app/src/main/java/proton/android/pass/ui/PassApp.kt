package proton.android.pass.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.insets.ProvideWindowInsets
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.isNightMode
import proton.android.pass.common.api.Some
import proton.android.pass.composecomponents.impl.messages.OfflineIndicator
import proton.android.pass.composecomponents.impl.messages.PassSnackbarHost
import proton.android.pass.composecomponents.impl.messages.rememberPassSnackbarHostState
import proton.android.pass.featurehome.impl.HomeFilterMode
import proton.android.pass.navigation.api.rememberAppNavigator
import proton.android.pass.network.api.NetworkStatus
import proton.android.pass.preferences.ThemePreference
import proton.android.pass.presentation.navigation.CoreNavigation
import proton.android.pass.presentation.navigation.drawer.HomeSection
import proton.android.pass.presentation.navigation.drawer.NavDrawerNavigation
import proton.android.pass.presentation.navigation.drawer.NavigationDrawerSection
import proton.android.pass.ui.internal.InternalDrawerState
import proton.android.pass.ui.internal.InternalDrawerValue
import proton.android.pass.ui.internal.rememberInternalDrawerState
import proton.android.pass.ui.navigation.AppNavItem

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun PassApp(
    modifier: Modifier = Modifier,
    coreNavigation: CoreNavigation,
    finishActivity: () -> Unit,
    appViewModel: AppViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        appViewModel.onStart()
    }

    val appUiState by appViewModel.appUiState.collectAsStateWithLifecycle()

    val isDark = when (appUiState.theme) {
        ThemePreference.Dark -> true
        ThemePreference.Light -> false
        ThemePreference.System -> isNightMode()
    }
    ProtonTheme(isDark = isDark) {
        ProvideWindowInsets {
            PassAppContent(
                modifier = modifier,
                appUiState = appUiState,
                coreNavigation = coreNavigation,
                onDrawerSectionChanged = { appViewModel.onDrawerSectionChanged(it) },
                onSnackbarMessageDelivered = { appViewModel.onSnackbarMessageDelivered() },
                finishActivity = finishActivity
            )
        }
    }
}

@Composable
fun PassAppContent(
    modifier: Modifier = Modifier,
    appUiState: AppUiState,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    internalDrawerState: InternalDrawerState = rememberInternalDrawerState(InternalDrawerValue.Closed),
    coreNavigation: CoreNavigation,
    onDrawerSectionChanged: (NavigationDrawerSection) -> Unit,
    onSnackbarMessageDelivered: () -> Unit,
    finishActivity: () -> Unit
) {
    val appNavigator = rememberAppNavigator()
    val scaffoldState = rememberScaffoldState()
    val passSnackbarHostState = rememberPassSnackbarHostState(scaffoldState.snackbarHostState)
    var homeFilterState by remember { mutableStateOf(HomeFilterMode.AllItems) }
    when (appNavigator.currentDestination?.route) {
        AppNavItem.Home.route -> {
            when (homeFilterState) {
                HomeFilterMode.AllItems -> onDrawerSectionChanged(NavigationDrawerSection.Items)
                HomeFilterMode.Logins -> onDrawerSectionChanged(NavigationDrawerSection.Logins)
                HomeFilterMode.Aliases -> onDrawerSectionChanged(NavigationDrawerSection.Aliases)
                HomeFilterMode.Notes -> onDrawerSectionChanged(NavigationDrawerSection.Notes)
            }
        }
        AppNavItem.Settings.route -> onDrawerSectionChanged(NavigationDrawerSection.Settings)
        AppNavItem.Trash.route -> onDrawerSectionChanged(NavigationDrawerSection.Trash)
        else -> {}
    }
    val navDrawerNavigation = NavDrawerNavigation(
        onNavHome = { section ->
            homeFilterState = when (section) {
                HomeSection.Items -> HomeFilterMode.AllItems
                HomeSection.Logins -> HomeFilterMode.Logins
                HomeSection.Aliases -> HomeFilterMode.Aliases
                HomeSection.Notes -> HomeFilterMode.Notes
            }
            appNavigator.navigate(destination = AppNavItem.Home)
        },
        onNavSettings = { appNavigator.navigate(AppNavItem.Settings) },
        onNavTrash = { appNavigator.navigate(AppNavItem.Trash) },
        onBugReport = { coreNavigation.onReport() },
        onInternalDrawerClick = { coroutineScope.launch { internalDrawerState.open() } }
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


    Scaffold(
        modifier = modifier,
        scaffoldState = scaffoldState,
        snackbarHost = { PassSnackbarHost(snackbarHostState = passSnackbarHostState) }
    ) { contentPadding ->
        InternalDrawer(
            drawerState = internalDrawerState,
            onOpenVault = {
                coroutineScope.launch {
                    internalDrawerState.close()
                }
                appNavigator.navigate(AppNavItem.VaultList)
            },
            content = {
                Column(modifier = Modifier.padding(contentPadding)) {
                    PassNavHost(
                        modifier = Modifier.weight(1f),
                        drawerUiState = appUiState.drawerUiState,
                        appNavigator = appNavigator,
                        homeFilterMode = homeFilterState,
                        navDrawerNavigation = navDrawerNavigation,
                        coreNavigation = coreNavigation,
                        finishActivity = finishActivity
                    )

                    AnimatedVisibility(visible = appUiState.networkStatus == NetworkStatus.Offline) {
                        OfflineIndicator()
                    }
                }
            }
        )
    }
}
