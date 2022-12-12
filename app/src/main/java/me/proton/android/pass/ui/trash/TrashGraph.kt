package me.proton.android.pass.ui.trash

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import me.proton.android.pass.navigation.api.composable
import me.proton.android.pass.ui.navigation.AppNavItem
import me.proton.pass.presentation.trash.TrashScreen

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.trashGraph(
    modifier: Modifier,
    navigationDrawer: @Composable (@Composable () -> Unit) -> Unit,
    onDrawerIconClick: () -> Unit
) {
    composable(AppNavItem.Trash) {
        navigationDrawer {
            TrashScreen(
                modifier = modifier,
                onDrawerIconClick = onDrawerIconClick
            )
        }
    }
}
