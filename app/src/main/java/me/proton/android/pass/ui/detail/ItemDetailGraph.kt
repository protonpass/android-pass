package me.proton.android.pass.ui.detail

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.navigation.NavGraphBuilder
import me.proton.android.pass.ui.navigation.AppNavigator
import me.proton.android.pass.ui.navigation.NavArgId
import me.proton.android.pass.ui.navigation.NavItem
import me.proton.android.pass.ui.navigation.composable
import me.proton.android.pass.ui.navigation.findArg
import me.proton.core.pass.domain.ItemType

@OptIn(
    ExperimentalAnimationApi::class,
    ExperimentalComposeUiApi::class
)
fun NavGraphBuilder.itemDetailGraph(nav: AppNavigator) {
    composable(NavItem.ViewItem) {
        ItemDetailScreen(
            onUpClick = { nav.onBackClick() },
            shareId = it.findArg(NavArgId.ShareId),
            itemId = it.findArg(NavArgId.ItemId),
            onEditClick = { shareId, itemId, itemType ->
                val destination = when (itemType) {
                    is ItemType.Login -> NavItem.EditLogin
                    is ItemType.Note -> NavItem.EditNote
                    is ItemType.Alias -> NavItem.EditAlias
                    is ItemType.Password -> null // Edit password does not exist yet
                }
                val route = when (itemType) {
                    is ItemType.Login -> NavItem.EditLogin.createNavRoute(shareId, itemId)
                    is ItemType.Note -> NavItem.EditNote.createNavRoute(shareId, itemId)
                    is ItemType.Alias -> NavItem.EditAlias.createNavRoute(shareId, itemId)
                    is ItemType.Password -> null // Edit password does not exist yet
                }

                if (destination != null && route != null) {
                    nav.navigate(destination, route)
                }
            },
            onMovedToTrash = { nav.onBackClick() }
        )
    }
}
