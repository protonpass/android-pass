package me.proton.android.pass.ui.create.note

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.navigation.NavGraphBuilder
import me.proton.android.pass.ui.navigation.AppNavigator
import me.proton.android.pass.ui.navigation.NavArgId
import me.proton.android.pass.ui.navigation.NavItem
import me.proton.android.pass.ui.navigation.composable
import me.proton.android.pass.ui.navigation.findArg
import me.proton.core.pass.domain.ShareId

@OptIn(
    ExperimentalAnimationApi::class, ExperimentalMaterialApi::class,
    ExperimentalComposeUiApi::class
)
fun NavGraphBuilder.createNoteGraph(nav: AppNavigator) {
    composable(NavItem.CreateNote) {
        val shareId = ShareId(it.findArg(NavArgId.ShareId))
        CreateNote(
            onUpClick = { nav.onBackClick() },
            shareId = shareId,
            onSuccess = { itemId ->
                nav.navigate(NavItem.ViewItem, NavItem.ViewItem.createNavRoute(shareId, itemId))
            }
        )
    }
}
