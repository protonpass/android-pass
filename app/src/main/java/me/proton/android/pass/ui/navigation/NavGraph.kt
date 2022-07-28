package me.proton.android.pass.ui.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import me.proton.android.pass.ui.create.item.CreateLoginView
import me.proton.android.pass.ui.home.HomeScreenNavigation
import me.proton.android.pass.ui.launcher.LauncherScreen
import me.proton.android.pass.ui.launcher.LauncherViewModel
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.pass.domain.ShareId

@ExperimentalAnimationApi
@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@Composable
fun AppNavGraph(
    keyStoreCrypto: KeyStoreCrypto,
    launcherViewModel: LauncherViewModel,
    onDrawerStateChanged: (Boolean) -> Unit,
) {
    val navController = rememberAnimatedNavController(keyStoreCrypto)
    AnimatedNavHost(
        navController = navController,
        startDestination = NavItem.Launcher.route
    ) {
        val onUpClick: () -> Unit = { navController.popBackStack() }
        composable(NavItem.Launcher) {
            LauncherScreen.View(
                onDrawerStateChanged = onDrawerStateChanged,
                viewModel = launcherViewModel,
                homeScreenNavigation = object : HomeScreenNavigation {
                    override val toCreateItem = { shareId: ShareId ->
                        navController.navigate(NavItem.CreateLogin.createNavRoute(shareId))
                    }
                }
            )
        }
        composable(NavItem.CreateLogin) {
            CreateLoginView(
                onUpClick = onUpClick,
                shareId = it.findArg(NavArg.ShareId),
                onSuccess = { itemId ->
                    // TODO: Navigate to a item detail view
                    onUpClick()
                }
            )
        }
    }
}

@ExperimentalAnimationApi
private fun NavGraphBuilder.composable(
    navItem: NavItem,
    content: @Composable (NavBackStackEntry) -> Unit
) {
    composable(
        route = navItem.route,
        arguments = navItem.args
    ) {
        content(it)
    }
}

private inline fun <reified T> NavBackStackEntry.findArg(arg: NavArg): T {
    val value = arguments?.get(arg.key)
    requireNotNull(value)
    return value as T
}
