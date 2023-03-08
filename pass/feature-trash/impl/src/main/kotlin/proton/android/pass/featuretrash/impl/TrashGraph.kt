package proton.android.pass.featuretrash.impl

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavGraphBuilder
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.composable

object Trash : NavItem(baseRoute = "trash", isTopLevel = true)

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.trashGraph(
    onDismiss: () -> Unit
) {
    composable(Trash) {
        TrashScreen(
            onDismiss = onDismiss
        )
    }
}
