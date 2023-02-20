package proton.android.pass.ui.create.note

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavGraphBuilder
import proton.android.pass.featurecreateitem.impl.note.UpdateNote
import proton.android.pass.navigation.api.AppNavigator
import proton.android.pass.navigation.api.composable
import proton.android.pass.ui.navigation.AppNavItem

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.updateNoteGraph(nav: AppNavigator) {
    composable(AppNavItem.EditNote) {
        UpdateNote(
            onUpClick = { nav.onBackClick() },
            onSuccess = { shareId, itemId ->
                nav.navigate(
                    destination = AppNavItem.ViewItem,
                    route = AppNavItem.ViewItem.createNavRoute(shareId, itemId),
                    backDestination = AppNavItem.Home
                )
            }
        )
    }
}
