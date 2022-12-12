package me.proton.android.pass.ui.settings

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import me.proton.android.pass.navigation.api.composable
import me.proton.android.pass.ui.navigation.AppNavItem
import me.proton.pass.presentation.settings.SettingsScreen

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.settingsGraph(
    modifier: Modifier,
    appVersion: String,
    navigationDrawer: @Composable (@Composable () -> Unit) -> Unit,
    onDrawerIconClick: () -> Unit
) {
    composable(AppNavItem.Settings) {
        navigationDrawer {
            SettingsScreen(
                modifier = modifier,
                appVersion = appVersion,
                onDrawerIconClick = onDrawerIconClick
            )
        }
    }
}
