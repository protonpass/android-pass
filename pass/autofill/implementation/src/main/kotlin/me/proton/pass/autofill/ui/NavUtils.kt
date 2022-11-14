package me.proton.pass.autofill.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import com.google.accompanist.navigation.animation.composable
import me.proton.pass.autofill.ui.autofill.AutofillNavItem

@ExperimentalAnimationApi
fun NavGraphBuilder.composable(
    navItem: AutofillNavItem,
    content: @Composable (NavBackStackEntry) -> Unit
) {
    composable(
        route = navItem.route
    ) {
        content(it)
    }
}

