package me.proton.android.pass.ui.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import kotlinx.coroutines.flow.StateFlow
import me.proton.android.pass.log.PassLogger

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun rememberAppNavigator(
    navController: NavHostController = rememberAnimatedNavController()
): AppNavigator = remember(navController) { AppNavigator(navController) }

@Stable
class AppNavigator(
    val navController: NavHostController
) {

    fun navigate(destination: NavItem, route: String? = null, backDestination: NavItem? = null) {
        val destinationRoute = route ?: destination.route
        PassLogger.d(TAG, "Navigating to $destinationRoute")

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

    fun onBackClick() {
        navController.popBackStack()
    }

    fun navigateUpWithResult(key: String, value: Any) {
        navController.previousBackStackEntry
            ?.savedStateHandle
            ?.set(key, value)
        navController.popBackStack()
    }

    fun <T> navState(key: String, default: T): StateFlow<T> =
        (navController.currentBackStackEntry?.savedStateHandle ?: SavedStateHandle())
            .getStateFlow(key, default)

    companion object {
        private const val TAG = "AppNavigator"
    }
}
