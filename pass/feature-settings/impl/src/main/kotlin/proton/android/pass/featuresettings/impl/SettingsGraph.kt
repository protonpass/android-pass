package proton.android.pass.featuresettings.impl

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavGraphBuilder
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.composable

object Settings : NavItem(baseRoute = "settings", isTopLevel = true)

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.settingsGraph(
    onReportProblemClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    composable(Settings) {
        SettingsScreen(
            onLogoutClick = onLogoutClick,
            onReportProblemClick = onReportProblemClick
        )
    }
}
