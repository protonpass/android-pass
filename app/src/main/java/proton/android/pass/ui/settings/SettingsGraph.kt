package proton.android.pass.ui.settings

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import proton.android.pass.navigation.api.composable
import proton.android.pass.ui.navigation.AppNavItem
import proton.android.pass.presentation.settings.SettingsScreen

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.settingsGraph(
    navigationDrawer: @Composable (@Composable () -> Unit) -> Unit,
    onDrawerIconClick: () -> Unit
) {
    composable(AppNavItem.Settings) {
        navigationDrawer {
            SettingsScreen(
                onDrawerIconClick = onDrawerIconClick
            )
        }
    }
}
