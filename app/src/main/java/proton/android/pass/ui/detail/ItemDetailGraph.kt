package proton.android.pass.ui.detail

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavGraphBuilder
import proton.android.pass.navigation.api.AppNavigator
import proton.android.pass.navigation.api.composable
import proton.android.pass.ui.navigation.AppNavItem
import proton.pass.domain.ItemType
import proton.android.pass.presentation.detail.ItemDetailScreen

@OptIn(ExperimentalAnimationApi::class)
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
            }
        )
    }
}
