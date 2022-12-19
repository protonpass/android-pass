package me.proton.android.pass.ui.create.note

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavGraphBuilder
import me.proton.android.pass.navigation.api.AppNavigator
import me.proton.android.pass.navigation.api.composable
import me.proton.android.pass.ui.navigation.AppNavItem
import me.proton.pass.presentation.create.note.UpdateNote

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
            },
            onSentToTrash = {
                nav.popUpTo(AppNavItem.Home)
            }
        )
    }
}
