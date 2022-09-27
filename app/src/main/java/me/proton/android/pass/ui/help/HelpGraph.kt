package me.proton.android.pass.ui.help

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import me.proton.android.pass.ui.navigation.NavItem
import me.proton.android.pass.ui.navigation.composable

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialApi::class)
fun NavGraphBuilder.helpGraph(
    navigationDrawer: @Composable (@Composable () -> Unit) -> Unit,
    onDrawerIconClick: () -> Unit
) {
    composable(NavItem.Help) {
        navigationDrawer {
            HelpScreen { onDrawerIconClick() }
        }
    }
}
