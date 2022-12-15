package me.proton.android.pass.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.insets.ProvideWindowInsets
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.proton.android.pass.navigation.api.rememberAppNavigator
import me.proton.android.pass.preferences.ThemePreference
import me.proton.android.pass.ui.internal.InternalDrawerState
import me.proton.android.pass.ui.internal.InternalDrawerValue
import me.proton.android.pass.ui.internal.rememberInternalDrawerState
import me.proton.android.pass.ui.navigation.AppNavItem
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.isNightMode
import me.proton.pass.common.api.Some
import me.proton.pass.presentation.components.common.PassSnackbarHost
import me.proton.pass.presentation.components.common.rememberPassSnackbarHostState
import me.proton.pass.presentation.components.navigation.CoreNavigation
import me.proton.pass.presentation.components.navigation.drawer.HomeSection
import me.proton.pass.presentation.components.navigation.drawer.NavDrawerNavigation
import me.proton.pass.presentation.components.navigation.drawer.NavigationDrawerSection
import me.proton.pass.presentation.home.HomeFilterMode

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
    val (homeFilterState, setHomeFilterState) = remember { mutableStateOf(HomeFilterMode.AllItems) }
    val navDrawerNavigation = NavDrawerNavigation(
        onNavHome = { section ->
            when (section) {
                HomeSection.Items -> {
                    setHomeFilterState(HomeFilterMode.AllItems)
                    onDrawerSectionChanged(NavigationDrawerSection.Items)
                }
                HomeSection.Logins -> {
                    setHomeFilterState(HomeFilterMode.Logins)
                    onDrawerSectionChanged(NavigationDrawerSection.Logins)
                }
                HomeSection.Aliases -> {
                    setHomeFilterState(HomeFilterMode.Aliases)
                    onDrawerSectionChanged(NavigationDrawerSection.Aliases)

                }
                HomeSection.Notes -> {
                    setHomeFilterState(HomeFilterMode.Notes)
                    onDrawerSectionChanged(NavigationDrawerSection.Notes)
                }
            }
            appNavigator.navigate(destination = AppNavItem.Home)
        },
        onNavSettings = {
            onDrawerSectionChanged(NavigationDrawerSection.Settings)
            appNavigator.navigate(AppNavItem.Settings)
        },
        onNavTrash = {
            onDrawerSectionChanged(NavigationDrawerSection.Trash)
            appNavigator.navigate(AppNavItem.Trash)
        },
        onBugReport = {
            coreNavigation.onReport()
        },
        onInternalDrawerClick = {
            coroutineScope.launch { internalDrawerState.open() }
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
    Scaffold(
        modifier = modifier,
        scaffoldState = scaffoldState,
        snackbarHost = { PassSnackbarHost(snackbarHostState = passSnackbarHostState) }
    ) { contentPadding ->
        InternalDrawer(drawerState = internalDrawerState) {
            PassNavHost(
                modifier = Modifier.padding(contentPadding),
                drawerUiState = appUiState.drawerUiState,
                appNavigator = appNavigator,
                homeFilterMode = homeFilterState,
                navDrawerNavigation = navDrawerNavigation,
                coreNavigation = coreNavigation,
                finishActivity = finishActivity
            )
        }
    }
}
