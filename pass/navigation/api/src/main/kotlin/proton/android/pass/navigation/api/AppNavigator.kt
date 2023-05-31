package proton.android.pass.navigation.api

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.accompanist.navigation.material.BottomSheetNavigator
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.rememberBottomSheetNavigator
import proton.android.pass.log.api.PassLogger

@OptIn(ExperimentalMaterialNavigationApi::class)
@Composable
fun rememberAppNavigator(
    bottomSheetNavigator: BottomSheetNavigator = rememberBottomSheetNavigator(),
    navController: NavHostController = rememberAnimatedNavController(bottomSheetNavigator),
): AppNavigator = remember(navController) { AppNavigator(navController, bottomSheetNavigator) }

@OptIn(ExperimentalMaterialNavigationApi::class)
@Stable
class AppNavigator(
    val navController: NavHostController,
    val bottomSheetNavigator: BottomSheetNavigator
) {
    val currentDestination: NavDestination?
        @Composable get() = navController
            .currentBackStackEntryAsState()
            .value
            ?.destination

    fun navigate(destination: NavItem, route: String? = null, backDestination: NavItem? = null) {
        // Discard duplicated nav events
        if (navController.currentBackStackEntry?.lifecycleIsResumed() != true) return

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
                    println("CarlosLog: navigating back to ${backDestination.route}")
                    popUpTo(backDestination.route)
                }
            }
        }
    }

    fun hasDestinationInStack(destination: NavItem): Boolean =
        navController.backQueue.map { it.destination.route }
            .contains(destination.route)

    fun popUpTo(destination: NavItem) {
        navController.popBackStack(route = destination.route, inclusive = false, saveState = false)
    }

    fun onBackClick() {
        println("CarlosLog: OnBackClick called")
        navController.popBackStack()
    }

    fun navigateUpWithResult(key: String, value: Any) {
        navController.previousBackStackEntry
            ?.savedStateHandle
            ?.set(key, value)
        println("CarlosLog: navigateUpWithResult called")
        navController.popBackStack()
    }

    /**
     * If the lifecycle is not resumed it means this NavBackStackEntry already processed a nav event.
     *
     * This is used to de-duplicate navigation events.
     */
    private fun NavBackStackEntry.lifecycleIsResumed() =
        this.lifecycle.currentState == Lifecycle.State.RESUMED

    companion object {
        private const val TAG = "AppNavigator"
    }
}
