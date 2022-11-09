package me.proton.android.pass.ui.home

import androidx.compose.runtime.Stable
import me.proton.android.pass.ui.navigation.AppNavigator
import me.proton.android.pass.ui.navigation.NavItem
import me.proton.pass.domain.ItemId
import me.proton.pass.domain.ShareId

@Stable
data class HomeScreenNavigation(val appNavigator: AppNavigator) {
    val toCreateLogin: (ShareId) -> Unit = { shareId: ShareId ->
        appNavigator.navigate(NavItem.CreateLogin, NavItem.CreateLogin.createNavRoute(shareId))
    }
    val toEditLogin: (ShareId, ItemId) -> Unit = { shareId: ShareId, itemId: ItemId ->
        appNavigator.navigate(NavItem.EditLogin, NavItem.EditLogin.createNavRoute(shareId, itemId))
    }
    val toCreateNote: (ShareId) -> Unit = { shareId: ShareId ->
        appNavigator.navigate(NavItem.CreateNote, NavItem.CreateNote.createNavRoute(shareId))
    }
    val toEditNote: (ShareId, ItemId) -> Unit = { shareId: ShareId, itemId: ItemId ->
        appNavigator.navigate(NavItem.EditNote, NavItem.EditNote.createNavRoute(shareId, itemId))
    }
    val toCreateAlias: (ShareId) -> Unit = { shareId: ShareId ->
        appNavigator.navigate(NavItem.CreateAlias, NavItem.CreateAlias.createNavRoute(shareId))
    }
    val toEditAlias: (ShareId, ItemId) -> Unit = { shareId: ShareId, itemId: ItemId ->
        appNavigator.navigate(NavItem.EditAlias, NavItem.EditAlias.createNavRoute(shareId, itemId))
    }
    val toItemDetail: (ShareId, ItemId) -> Unit = { shareId: ShareId, itemId: ItemId ->
        appNavigator.navigate(NavItem.ViewItem, NavItem.ViewItem.createNavRoute(shareId, itemId))
    }
    val toAuth: () -> Unit = { appNavigator.navigate(NavItem.Auth) }
    val toOnBoarding: () -> Unit = { appNavigator.navigate(NavItem.OnBoarding) }

    val toCreatePassword: (ShareId) -> Unit = { shareId: ShareId ->
        appNavigator.navigate(
            NavItem.CreatePassword,
            NavItem.CreatePassword.createNavRoute(shareId)
        )
    }
}
