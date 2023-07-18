/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

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
        // Discard duplicated nav events
        PassLogger.d(TAG, "State ${navController.currentBackStackEntry?.lifecycle?.currentState}")
        if (!lifecycleIsResumed() && destination.navItemType == NavItemType.Screen) {
            PassLogger.d(
                TAG,
                "Navigate: Navigation event discarded as it was duplicated. " +
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
                    if (destination.noHistory) {
                        inclusive = true
                    }
                }
                launchSingleTop = true
                restoreState = true
            }

            destination.navItemType == NavItemType.Bottomsheet ||
                destination.navItemType == NavItemType.Dialog ->
                navController.navigate(destinationRoute) {
                    launchSingleTop = true
                    if (backDestination != null) {
                        popUpTo(backDestination.route) {
                            if (destination.noHistory) {
                                inclusive = true
                            }
                        }
                    }
                }

            else -> navController.navigate(destinationRoute) {
                if (backDestination != null) {
                    popUpTo(backDestination.route) {
                        if (destination.noHistory) {
                            inclusive = true
                        }
                    }
                }
            }
        }
    }

    fun hasDestinationInStack(destination: NavItem): Boolean =
        navController.currentBackStack.value.any { it.destination.route == destination.route }

    fun hasPreviousDestination(destination: NavItem): Boolean =
        navController.previousBackStackEntry.run { this?.destination?.route == destination.route }

    fun popUpTo(destination: NavItem, comesFromBottomsheet: Boolean = false) {
        if (!lifecycleIsResumed() && !comesFromBottomsheet) {
            PassLogger.d(TAG, "PopUpTo: Navigation event discarded as it was duplicated.")
            return
        }
        navController.popBackStack(route = destination.route, inclusive = false, saveState = false)
    }

    fun onBackClick(comesFromBottomsheet: Boolean = false) {
        if (!lifecycleIsResumed() && !comesFromBottomsheet) {
            PassLogger.d(TAG, "OnBackClick: Navigation event discarded as it was duplicated.")
            return
        }
        PassLogger.i(
            TAG,
            "Navigating back to ${navController.previousBackStackEntry?.destination?.route}"
        )
        navController.popBackStack()
    }

    fun navigateUpWithResult(key: String, value: Any, comesFromBottomsheet: Boolean = false) {
        if (!lifecycleIsResumed() && !comesFromBottomsheet) {
            PassLogger.d(
                TAG,
                "NavigateUpWithResult: Navigation event discarded as it was duplicated."
            )
            return
        }
        navController.previousBackStackEntry
            ?.savedStateHandle
            ?.set(key, value)
        navController.popBackStack()
    }

    fun navigateUpWithResult(values: Map<String, Any>, comesFromBottomsheet: Boolean = false) {
        if (!lifecycleIsResumed() && !comesFromBottomsheet) {
            PassLogger.d(
                TAG,
                "NavigateUpWithResult: Navigation event discarded as it was duplicated."
            )
            return
        }
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
