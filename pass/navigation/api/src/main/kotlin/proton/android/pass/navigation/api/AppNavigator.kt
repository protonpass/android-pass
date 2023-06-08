package proton.android.pass.navigation.api

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
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

    fun navigate(destination: NavItem, route: String? = null, backDestination: NavItem? = null) {
        val destinationRoute = route ?: destination.route
        val currentRoute = navController.currentBackStackEntry?.destination?.route
        // Discard duplicated nav events
        if (!lifecycleIsResumed() && !destination.isBottomsheet && destinationRoute != currentRoute) {
            PassLogger.d(
                TAG,
                "Navigation event discarded as it was duplicated. " +
                    "Current: ${navController.currentBackStackEntry?.destination?.route} | " +
                    "Destination: ${destination.route}"
            )
            return
        }
        PassLogger.i(TAG, "Navigating to $destinationRoute")

        when {
            destination.isTopLevel -> navController.navigate(destinationRoute) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }

            destination.isBottomsheet -> navController.navigate(destinationRoute) {
                launchSingleTop = true
                if (backDestination != null) {
                    popUpTo(backDestination.route)
                }
            }

            else -> navController.navigate(destinationRoute) {
                if (backDestination != null) {
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
        PassLogger.i(
            TAG,
            "Navigating back to ${navController.previousBackStackEntry?.destination?.route}"
        )
        navController.popBackStack()
    }

    fun navigateUpWithResult(key: String, value: Any) {
        navController.previousBackStackEntry
            ?.savedStateHandle
            ?.set(key, value)
        navController.popBackStack()
    }

    fun navigateUpWithResult(values: Map<String, Any>) {
        navController.previousBackStackEntry
            ?.savedStateHandle
            ?.let {
                values.forEach { (key, value) ->
                    it[key] = value
                }
            }
        navController.popBackStack()
    }

    /**
     * If the lifecycle is not resumed it means this NavBackStackEntry already processed a nav event.
     *
     * This is used to de-duplicate navigation events.
     */
    private fun lifecycleIsResumed() =
        navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED

    companion object {
        private const val TAG = "AppNavigator"
    }
}
