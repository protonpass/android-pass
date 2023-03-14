package proton.android.pass.featureprofile.impl

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavGraphBuilder
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.composable

object Profile : NavItem(baseRoute = "profile", isTopLevel = true)

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.profileGraph(
    onListClick: () -> Unit,
    onCreateItemClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    composable(Profile) {
        ProfileScreen(
            onListClick = onListClick,
            onCreateItemClick = onCreateItemClick,
            onSettingsClick = onSettingsClick
        )
    }
}
