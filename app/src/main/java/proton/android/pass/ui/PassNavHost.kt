package proton.android.pass.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.DrawerState
import androidx.compose.material.DrawerValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import com.google.accompanist.navigation.animation.AnimatedNavHost
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import proton.android.pass.featurehome.impl.Home
import proton.android.pass.featurehome.impl.HomeItemTypeSelection
import proton.android.pass.featurehome.impl.HomeVaultSelection
import proton.android.pass.featuresettings.impl.SignOutDialog
import proton.android.pass.navigation.api.AppNavigator
import proton.android.pass.presentation.navigation.CoreNavigation
import proton.android.pass.presentation.navigation.drawer.DrawerUiState
import proton.android.pass.presentation.navigation.drawer.ModalNavigationDrawer
import proton.android.pass.presentation.navigation.drawer.NavDrawerNavigation
import proton.android.pass.ui.navigation.appGraph

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
    homeItemTypeSelection: HomeItemTypeSelection,
    homeVaultSelection: HomeVaultSelection,
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
        startDestination = Home.route
    ) {
        appGraph(
            appNavigator = appNavigator,
            homeItemTypeSelection = homeItemTypeSelection,
            homeVaultSelection = homeVaultSelection,
            navigationDrawer = { content ->
                ModalNavigationDrawer(
                    drawerUiState = drawerUiState,
                    drawerState = drawerState,
                    navDrawerNavigation = navDrawerNavigation,
                    coreNavigation = coreNavigation,
                    onSignOutClick = { appNavigator.navigate(SignOutDialog) },
                    signOutDialog = {},
                    content = content
                )
            },
            onDrawerIconClick = { coroutineScope.launch { drawerState.open() } },
            finishActivity = finishActivity,
            onLogout = { coreNavigation.onRemove(null) }
        )
    }

}
