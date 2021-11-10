package me.proton.android.pass.ui.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NamedNavArgument
import androidx.navigation.compose.navArgument
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import me.proton.android.pass.ui.home.HomeScreen
import me.proton.android.pass.ui.launcher.AccountViewModel
import me.proton.android.pass.ui.launcher.LauncherScreen
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.domain.entity.UserId

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialApi::class)
@Composable
fun AppNavGraph(
    accountViewModel: AccountViewModel,
    keyStoreCrypto: KeyStoreCrypto,
    onDrawerStateChanged: (Boolean) -> Unit,
) {
    val navController = rememberAnimatedNavController(keyStoreCrypto)
    AnimatedNavHost(
        navController = navController,
        startDestination = LauncherScreen.route
    ) {
        addLauncher(navController)
        addHome(navController, accountViewModel, onDrawerStateChanged)
    }
}

@ExperimentalMaterialApi
@ExperimentalAnimationApi
fun NavGraphBuilder.addLauncher(navController: NavHostController) = composable(
    LauncherScreen.route
) {
    LauncherScreen.view(
        navigateToHomeScreen = { userId ->
            navController.navigate(HomeScreen(userId)) {
                popUpTo(LauncherScreen.route) { inclusive = true }
            }
        }
    )
}

@ExperimentalMaterialApi
@ExperimentalAnimationApi
fun NavGraphBuilder.addHome(
    navController: NavHostController,
    accountViewModel: AccountViewModel,
    onDrawerStateChanged: (Boolean) -> Unit,
    route: String = HomeScreen.route,
    arguments: List<NamedNavArgument> = listOf(
        navArgument(HomeScreen.userId) {
            type = NavType.StringType
        }
    )
) = composable(route, arguments) { navBackStackEntry ->
    val bundleArgs = requireNotNull(navBackStackEntry.arguments) { "arguments bundle is null" }
    val userId = UserId(
        requireNotNull(bundleArgs.getString(HomeScreen.userId)) {
            "userId is required"
        }
    )
    HomeScreen.view(
        userId,
        navController,
        onDrawerStateChanged = onDrawerStateChanged,
        accountViewModel = accountViewModel,
    )
}
