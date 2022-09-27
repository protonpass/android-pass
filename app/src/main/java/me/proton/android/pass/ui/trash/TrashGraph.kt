package me.proton.android.pass.ui.trash

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import me.proton.android.pass.ui.navigation.NavItem
import me.proton.android.pass.ui.navigation.composable

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialApi::class)
fun NavGraphBuilder.trashGraph(
    navigationDrawer: @Composable (@Composable () -> Unit) -> Unit,
    onDrawerIconClick: () -> Unit
) {
    composable(NavItem.Trash) {
        navigationDrawer {
            TrashScreen(
                onDrawerIconClick = onDrawerIconClick
            )
        }
    }
}
