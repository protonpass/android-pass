package proton.android.pass.navigation.api

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import proton.android.pass.log.api.PassLogger

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun rememberAppNavigator(
    navController: NavHostController = rememberAnimatedNavController()
): AppNavigator = remember(navController) { AppNavigator(navController) }

@Stable
class AppNavigator(
    val navController: NavHostController
) {
    val currentDestination: NavDestination?
        @Composable get() = navController
            .currentBackStackEntryAsState()
            .value
            ?.destination

    fun navigate(destination: NavItem, route: String? = null, backDestination: NavItem? = null) {
        val destinationRoute = route ?: destination.route
        PassLogger.i(TAG, "Navigating to $destinationRoute")

        if (destination.isTopLevel) {
            navController.navigate(destinationRoute) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        } else {
            navController.navigate(destinationRoute) {
                if (backDestination != null) {
                    popUpTo(backDestination.route)
                }
            }
        }
    }

    fun popUpTo(destination: NavItem) {
        navController.popBackStack(route = destination.route, inclusive = false, saveState = false)
    }

    fun onBackClick() {
        navController.popBackStack()
    }

    fun navigateUpWithResult(key: String, value: Any) {
        navController.previousBackStackEntry
            ?.savedStateHandle
            ?.set(key, value)
        navController.popBackStack()
    }

    fun <T> navState(key: String, default: T?): StateFlow<T?> = MutableStateFlow(
        navController.currentBackStackEntry
            ?.savedStateHandle
            ?.remove<T>(key)
            ?: default
    )

    companion object {
        private const val TAG = "AppNavigator"
    }
}
