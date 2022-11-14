package me.proton.android.pass.ui.detail

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.navigation.NavGraphBuilder
import me.proton.android.pass.navigation.api.AppNavigator
import me.proton.android.pass.navigation.api.composable
import me.proton.android.pass.ui.navigation.AppNavItem
import me.proton.pass.domain.ItemType

@OptIn(
    ExperimentalAnimationApi::class,
    ExperimentalComposeUiApi::class
)
fun NavGraphBuilder.itemDetailGraph(nav: AppNavigator) {
    composable(AppNavItem.ViewItem) {
        ItemDetailScreen(
            onUpClick = { nav.onBackClick() },
            onEditClick = { shareId, itemId, itemType ->
                val destination = when (itemType) {
                    is ItemType.Login -> AppNavItem.EditLogin
                    is ItemType.Note -> AppNavItem.EditNote
                    is ItemType.Alias -> AppNavItem.EditAlias
                    is ItemType.Password -> null // Edit password does not exist yet
                }
                val route = when (itemType) {
                    is ItemType.Login -> AppNavItem.EditLogin.createNavRoute(shareId, itemId)
                    is ItemType.Note -> AppNavItem.EditNote.createNavRoute(shareId, itemId)
                    is ItemType.Alias -> AppNavItem.EditAlias.createNavRoute(shareId, itemId)
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
