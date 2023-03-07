package proton.android.pass.featuresettings.impl

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.composable

object Settings : NavItem(baseRoute = "settings", isTopLevel = true)

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.settingsGraph(
    navigationDrawer: @Composable (@Composable () -> Unit) -> Unit,
    onLogoutClick: () -> Unit
) {
    composable(Settings) {
        navigationDrawer {
            SettingsScreen(
                onLogoutClick = onLogoutClick
            )
        }
    }
}
