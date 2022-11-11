package me.proton.android.pass.ui.home

import androidx.compose.runtime.Stable
import me.proton.android.pass.navigation.api.AppNavigator
import me.proton.android.pass.ui.navigation.AppNavItem
import me.proton.pass.domain.ItemId
import me.proton.pass.domain.ShareId

@Stable
data class HomeScreenNavigation(val appNavigator: AppNavigator) {
    val toCreateLogin: (ShareId) -> Unit = { shareId: ShareId ->
        appNavigator.navigate(AppNavItem.CreateLogin, AppNavItem.CreateLogin.createNavRoute(shareId))
    }
    val toEditLogin: (ShareId, ItemId) -> Unit = { shareId: ShareId, itemId: ItemId ->
        appNavigator.navigate(AppNavItem.EditLogin, AppNavItem.EditLogin.createNavRoute(shareId, itemId))
    }
    val toCreateNote: (ShareId) -> Unit = { shareId: ShareId ->
        appNavigator.navigate(AppNavItem.CreateNote, AppNavItem.CreateNote.createNavRoute(shareId))
    }
    val toEditNote: (ShareId, ItemId) -> Unit = { shareId: ShareId, itemId: ItemId ->
        appNavigator.navigate(AppNavItem.EditNote, AppNavItem.EditNote.createNavRoute(shareId, itemId))
    }
    val toCreateAlias: (ShareId) -> Unit = { shareId: ShareId ->
        appNavigator.navigate(AppNavItem.CreateAlias, AppNavItem.CreateAlias.createNavRoute(shareId))
    }
    val toEditAlias: (ShareId, ItemId) -> Unit = { shareId: ShareId, itemId: ItemId ->
        appNavigator.navigate(AppNavItem.EditAlias, AppNavItem.EditAlias.createNavRoute(shareId, itemId))
    }
    val toItemDetail: (ShareId, ItemId) -> Unit = { shareId: ShareId, itemId: ItemId ->
        appNavigator.navigate(AppNavItem.ViewItem, AppNavItem.ViewItem.createNavRoute(shareId, itemId))
    }
    val toAuth: () -> Unit = { appNavigator.navigate(AppNavItem.Auth) }
    val toOnBoarding: () -> Unit = { appNavigator.navigate(AppNavItem.OnBoarding) }

    val toCreatePassword: (ShareId) -> Unit = { shareId: ShareId ->
        appNavigator.navigate(
            AppNavItem.CreatePassword,
            AppNavItem.CreatePassword.createNavRoute(shareId)
        )
    }
}
