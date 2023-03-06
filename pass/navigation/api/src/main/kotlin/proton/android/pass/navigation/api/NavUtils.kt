package proton.android.pass.navigation.api

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.dialog
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.bottomSheet

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.composable(
    navItem: NavItem,
    enterTransition: (AnimatedContentScope<NavBackStackEntry>.() -> EnterTransition?)? = null,
    exitTransition: (AnimatedContentScope<NavBackStackEntry>.() -> ExitTransition?)? = null,
    popEnterTransition: (AnimatedContentScope<NavBackStackEntry>.() -> EnterTransition?)? = enterTransition,
    popExitTransition: (AnimatedContentScope<NavBackStackEntry>.() -> ExitTransition?)? = exitTransition,
    content: @Composable (NavBackStackEntry) -> Unit
) {
    composable(
        route = navItem.route,
        arguments = navItem.args,
        enterTransition = enterTransition,
        exitTransition = exitTransition,
        popEnterTransition = popEnterTransition,
        popExitTransition = popExitTransition
    ) {
        content(it)
    }
}

@OptIn(ExperimentalMaterialNavigationApi::class)
fun NavGraphBuilder.bottomSheet(
    navItem: NavItem,
    content: @Composable (NavBackStackEntry) -> Unit
) {
    bottomSheet(
        route = navItem.route,
        arguments = navItem.args
    ) { content(it) }
}

fun NavGraphBuilder.dialog(
    navItem: NavItem,
    content: @Composable (NavBackStackEntry) -> Unit
) {
    dialog(
        route = navItem.route,
        arguments = navItem.args
    ) { content(it) }
}

fun Map<String, Any>.toPath() = this
    .map { "${it.key}=${it.value}" }
    .joinToString(
        prefix = "?",
        separator = "&"
    )
