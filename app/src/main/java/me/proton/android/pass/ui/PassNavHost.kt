package me.proton.android.pass.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.DrawerState
import androidx.compose.material.DrawerValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import com.google.accompanist.navigation.animation.AnimatedNavHost
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.proton.android.pass.composecomponents.impl.dialogs.ConfirmSignOutDialog
import me.proton.android.pass.navigation.api.AppNavigator
import me.proton.android.pass.ui.navigation.AppNavItem
import me.proton.android.pass.ui.navigation.appGraph
import me.proton.pass.presentation.home.HomeFilterMode
import me.proton.pass.presentation.navigation.CoreNavigation
import me.proton.pass.presentation.navigation.drawer.DrawerUiState
import me.proton.pass.presentation.navigation.drawer.ModalNavigationDrawer
import me.proton.pass.presentation.navigation.drawer.NavDrawerNavigation

@OptIn(
    ExperimentalAnimationApi::class,
    ExperimentalMaterialApi::class,
    ExperimentalComposeUiApi::class
)
@Composable
fun PassNavHost(
    modifier: Modifier = Modifier,
    drawerUiState: DrawerUiState,
    drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
    homeFilterMode: HomeFilterMode,
    appNavigator: AppNavigator,
    navDrawerNavigation: NavDrawerNavigation,
    coreNavigation: CoreNavigation,
    finishActivity: () -> Unit,
    coroutineScope: CoroutineScope = rememberCoroutineScope()
) {
    BackHandler(drawerState.isOpen) { coroutineScope.launch { drawerState.close() } }
    AnimatedNavHost(
        modifier = modifier,
        navController = appNavigator.navController,
        startDestination = AppNavItem.Home.route
    ) {
        appGraph(
            appNavigator = appNavigator,
            homeFilterMode = homeFilterMode,
            navigationDrawer = { content ->
                val (isSignOutDialogShown, setShowSignOutDialog) =
                    remember { mutableStateOf(false) }
                ModalNavigationDrawer(
                    drawerUiState = drawerUiState,
                    drawerState = drawerState,
                    navDrawerNavigation = navDrawerNavigation,
                    coreNavigation = coreNavigation,
                    onSignOutClick = { setShowSignOutDialog(true) },
                    signOutDialog = {
                        if (isSignOutDialogShown) {
                            ConfirmSignOutDialog(
                                state = isSignOutDialogShown,
                                onDismiss = { setShowSignOutDialog(false) },
                                onConfirm = { coreNavigation.onRemove(null) }
                            )
                        }
                    },
                    content = content
                )
            },
            onDrawerIconClick = { coroutineScope.launch { drawerState.open() } },
            finishActivity = finishActivity
        )
    }
}
