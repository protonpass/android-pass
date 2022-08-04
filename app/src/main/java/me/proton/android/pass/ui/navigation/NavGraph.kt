package me.proton.android.pass.ui.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import me.proton.android.pass.ui.create.login.CreateLoginView
import me.proton.android.pass.ui.detail.ItemDetailScreen
import me.proton.android.pass.ui.home.HomeScreenNavigation
import me.proton.android.pass.ui.launcher.LauncherScreen
import me.proton.android.pass.ui.launcher.LauncherViewModel
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.pass.domain.ItemId
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
            LauncherScreen(
                onDrawerStateChanged = onDrawerStateChanged,
                viewModel = launcherViewModel,
                homeScreenNavigation = object : HomeScreenNavigation {
                    override val toCreateItem = { shareId: ShareId ->
                        navController.navigate(NavItem.CreateLogin.createNavRoute(shareId))
                    }
                    override val toItemDetail = { args: Pair<ShareId, ItemId> ->
                        navController.navigate(NavItem.ViewItem.createNavRoute(args.first, args.second))
                    }
                }
            )
        }

        composable(NavItem.CreateLogin) {
            val shareId = ShareId(it.findArg(NavArg.ShareId))
            CreateLoginView(
                onUpClick = onUpClick,
                shareId = shareId,
                onSuccess = { itemId ->
                    navController.navigate(NavItem.ViewItem.createNavRoute(shareId, itemId)) {
                        popUpTo(NavItem.Launcher.route)
                    }
                }
            )
        }
        composable(NavItem.ViewItem) {
            ItemDetailScreen(
                onUpClick = onUpClick,
                shareId = it.findArg(NavArg.ShareId),
                itemId = it.findArg(NavArg.ItemId)
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
