package me.proton.android.pass.ui.create.note

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.navigation.NavGraphBuilder
import me.proton.android.pass.navigation.api.AppNavigator
import me.proton.android.pass.ui.navigation.AppNavItem
import me.proton.android.pass.ui.navigation.composable
import me.proton.pass.presentation.create.note.UpdateNote

@OptIn(
    ExperimentalAnimationApi::class, ExperimentalMaterialApi::class,
    ExperimentalComposeUiApi::class
)
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
