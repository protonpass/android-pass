package proton.android.pass.featuretrash.impl

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.composable

object Trash : NavItem(baseRoute = "trash", isTopLevel = true)

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.trashGraph(
    navigationDrawer: @Composable (@Composable () -> Unit) -> Unit,
    onDrawerIconClick: () -> Unit
) {
    composable(Trash) {
        navigationDrawer {
            TrashScreen(
                onDrawerIconClick = onDrawerIconClick
            )
        }
    }
}
