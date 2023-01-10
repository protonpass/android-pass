package proton.android.pass.ui.trash

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import proton.android.pass.navigation.api.composable
import proton.android.pass.ui.navigation.AppNavItem
import proton.android.pass.presentation.trash.TrashScreen

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialApi::class)
fun NavGraphBuilder.trashGraph(
    navigationDrawer: @Composable (@Composable () -> Unit) -> Unit,
    onDrawerIconClick: () -> Unit
) {
    composable(AppNavItem.Trash) {
        navigationDrawer {
            TrashScreen(
                onDrawerIconClick = onDrawerIconClick
            )
        }
    }
}
