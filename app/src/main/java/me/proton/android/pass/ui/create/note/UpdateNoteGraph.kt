package me.proton.core.pass.presentation.create.note

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.navigation.NavGraphBuilder
import me.proton.android.pass.ui.navigation.AppNavigator
import me.proton.android.pass.ui.navigation.NavArgId
import me.proton.android.pass.ui.navigation.NavItem
import me.proton.android.pass.ui.navigation.composable
import me.proton.android.pass.ui.navigation.findArg
import me.proton.core.pass.domain.ItemId
import me.proton.core.pass.domain.ShareId

@OptIn(
    ExperimentalAnimationApi::class, ExperimentalMaterialApi::class,
    ExperimentalComposeUiApi::class
)
fun NavGraphBuilder.updateNoteGraph(nav: AppNavigator) {
    composable(NavItem.EditNote) {
        val shareId = ShareId(it.findArg(NavArgId.ShareId))
        val itemId = ItemId(it.findArg(NavArgId.ItemId))
        UpdateNote(
            onUpClick = { nav.onBackClick() },
            shareId = shareId,
            itemId = itemId,
            onSuccess = {
                nav.navigate(
                    destination = NavItem.ViewItem,
                    route = NavItem.ViewItem.createNavRoute(shareId, itemId),
                    backDestination = NavItem.Home
                )
            }
        )
    }
}
