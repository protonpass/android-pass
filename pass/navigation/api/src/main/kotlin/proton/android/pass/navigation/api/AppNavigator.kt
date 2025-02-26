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

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.navOptions
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import proton.android.pass.common.api.SpecialCharacters.COLON
import proton.android.pass.log.api.PassLogger
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterialNavigationApi::class)
@Composable
fun rememberAppNavigator(
    bottomSheetNavigator: PassBottomSheetNavigator,
    navController: NavHostController = rememberNavController(bottomSheetNavigator)
): AppNavigator = remember(navController) { AppNavigator(navController, bottomSheetNavigator) }

@OptIn(ExperimentalMaterialNavigationApi::class)
@Stable
class AppNavigator(
    val navController: NavHostController,
    val passBottomSheetNavigator: PassBottomSheetNavigator
) {
    val currentRoute: String?
        get() = navController.currentBackStackEntry?.destination?.route
    private val previousRoute: String?
        get() = navController.previousBackStackEntry?.destination?.route
    private val startRoute: String?
        get() = navController.graph.findStartDestination().route

    fun navigate(
        destination: NavItem,
        route: String? = null,
        backDestination: NavItem? = null,
        force: Boolean = false
    ) {
        val destinationRoute = route ?: destination.route
        // Discard duplicated nav events
        if (!force && !lifecycleIsResumed() && destination.navItemType == NavItemType.Screen) {
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
                            if (backDestination.noHistory) {
                                inclusive = true
                            }
                        }
                    }
                }

            else -> navController.navigate(destinationRoute) {
                if (backDestination != null) {
                    popUpTo(backDestination.route) {
                        if (backDestination.noHistory) {
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

    fun popUpTo(destination: NavItem) {
        if (isInProgress()) return
        navController.popBackStack(route = destination.route, inclusive = false, saveState = false)
    }

    fun navigateBackToStartDestination(force: Boolean = false) {
        if (!force && isInProgress()) return
        val navOptions = navOptions {
            popUpTo(navController.graph.id) { inclusive = true }
        }
        val startDestinationRoute = startRoute ?: return run {
            PassLogger.e(TAG, "No start destination found")
        }
        navController.navigate(startDestinationRoute, navOptions)
    }

    fun navigateBack(force: Boolean = false) {
        if (!force && isInProgress()) return
        when {
            previousRoute == null && startRoute != null && currentRoute != startRoute -> {
                PassLogger.i(TAG, "Navigating back to start destination: $startRoute")
                val navOptions = navOptions {
                    popUpTo(navController.graph.id) { inclusive = true }
                }
                val startDestinationRoute = startRoute ?: return run {
                    PassLogger.e(TAG, "No start destination found")
                }
                navController.navigate(startDestinationRoute, navOptions)
                PassLogger.i(TAG, "Navigated back to start destination")
            }

            previousRoute != null -> {
                val navigatedFromRoute = previousRoute
                PassLogger.i(TAG, "Navigating back to $navigatedFromRoute")
                if (!navController.navigateUp()) {
                    PassLogger.i(TAG, "Could not navigate back to $navigatedFromRoute")
                }
            }

            else -> {
                PassLogger.i(TAG, "No previous route found")
                (navController.context as? Activity)?.finishAffinity()
                    ?: throw IllegalStateException("No previous route found and no activity found")
            }
        }
    }

    fun findCloserDestination(vararg destinations: NavItem): NavItem? = navController.currentBackStack.value
        .asReversed()
        .firstNotNullOfOrNull { entry ->
            destinations.find { it.route == entry.destination.route }
        }

    fun navigateBackWithResult(key: String, value: Any) {
        if (isInProgress()) return
        PassLogger.i(TAG, "Navigating back with result to $previousRoute")
        navController.previousBackStackEntry
            ?.savedStateHandle
            ?.set(key, value)
        navController.navigateUp()
    }

    fun setResult(values: Map<String, Any>) {
        navController.currentBackStackEntry
            ?.savedStateHandle
            ?.let {
                values.forEach { (key, value) ->
                    it[key] = value
                }
            }
    }

    fun navigateBackWithResult(values: Map<String, Any>) {
        if (isInProgress()) return
        PassLogger.i(TAG, "Navigating back with results to $previousRoute")
        navController.previousBackStackEntry
            ?.savedStateHandle
            ?.let {
                values.forEach { (key, value) ->
                    it[key] = value
                }
            }
        navController.navigateUp()
    }

    fun navigateToDeeplink(deepLink: String) {
        val urlDecodedDeeplink = URLDecoder.decode(deepLink, StandardCharsets.UTF_8.name())
        val uri: Uri = Uri.parse("$NAV_SCHEME$COLON$urlDecodedDeeplink")
        val encodingParameters = CommonNavArgId.entries.filter { it.requireEncoding }.map { it.key }
        val (requiresEncoding, regularQuery) = uri.queryParameterNames.partition { it in encodingParameters }
        val queryString = buildString {
            regularQuery.forEach { key ->
                append("$key=${uri.getQueryParameter(key)}&")
            }
            requiresEncoding.forEach { key ->
                val encodedValue = NavParamEncoder.encode(uri.getQueryParameter(key) ?: "")
                append("$key=$encodedValue&")
            }
            if (isNotEmpty()) deleteCharAt(lastIndex)
        }
        val newUri = uri.buildUpon().clearQuery().encodedQuery(queryString).build()
        val intent = Intent(Intent.ACTION_VIEW, newUri)
        val didNavigate = navController.handleDeepLink(intent)
        if (didNavigate) {
            PassLogger.i(TAG, "Navigated to deeplink: $newUri")
        } else {
            PassLogger.i(TAG, "Could not navigate to deeplink: $newUri")
        }
    }

    private fun isInProgress(): Boolean {
        if (!lifecycleIsResumed()) {
            PassLogger.d(TAG, "Navigation event discarded.")
            return true
        }
        return false
    }

    /**
     * If the lifecycle is not resumed it means this NavBackStackEntry already processed a nav event.
     *
     * This is used to de-duplicate navigation events.
     */
    private fun lifecycleIsResumed() = navController.currentBackStackEntry
        ?.lifecycle
        ?.currentState
        ?.let {
            PassLogger.d(TAG, "State $it")
            it == Lifecycle.State.RESUMED
        }
        ?: false

    private companion object {

        private const val TAG = "AppNavigator"

    }

}
