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
import androidx.compose.ui.Modifier
import com.google.accompanist.navigation.animation.AnimatedNavHost
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.proton.android.pass.ui.navigation.AppNavigator
import me.proton.android.pass.ui.navigation.NavItem
import me.proton.android.pass.ui.navigation.appGraph
import me.proton.android.pass.ui.shared.ConfirmSignOutDialog
import me.proton.pass.presentation.components.navigation.CoreNavigation
import me.proton.pass.presentation.components.navigation.drawer.DrawerUiState
import me.proton.pass.presentation.components.navigation.drawer.ModalNavigationDrawer
import me.proton.pass.presentation.components.navigation.drawer.NavDrawerNavigation

@OptIn(
    ExperimentalAnimationApi::class,
    ExperimentalMaterialApi::class
)
@Composable
fun PassNavHost(
    modifier: Modifier = Modifier,
    drawerUiState: DrawerUiState,
    drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
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
        startDestination = NavItem.Home.route
    ) {
        appGraph(
            appNavigator = appNavigator,
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
