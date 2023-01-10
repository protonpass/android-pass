package proton.android.pass.ui.help

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import proton.android.pass.navigation.api.composable
import proton.android.pass.ui.navigation.AppNavItem
import proton.android.pass.presentation.help.HelpScreen

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialApi::class)
fun NavGraphBuilder.helpGraph(
    navigationDrawer: @Composable (@Composable () -> Unit) -> Unit,
    onDrawerIconClick: () -> Unit
) {
    composable(AppNavItem.Help) {
        navigationDrawer {
            HelpScreen { onDrawerIconClick() }
        }
    }
}
