package me.proton.android.pass.ui.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun rememberAppNavigator(
    navController: NavHostController = rememberAnimatedNavController()
): AppNavigator = remember(navController) { AppNavigator(navController) }

@Stable
class AppNavigator(
    private val navController: NavHostController
) {

    fun navigate(destination: NavItem, route: String? = null) {
        if (destination.isTopLevel) {
            navController.navigate(route ?: destination.route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        } else {
            navController.navigate(route ?: destination.route)
        }
    }

    fun onBackClick() {
        navController.popBackStack()
    }
}
