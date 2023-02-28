package proton.android.pass.ui.trash

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import proton.android.pass.featuretrash.impl.TrashScreen
import proton.android.pass.navigation.api.composable
import proton.android.pass.ui.navigation.Trash

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
