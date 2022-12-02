package me.proton.android.pass.ui.navigation

import androidx.navigation.navArgument
import me.proton.android.pass.navigation.api.NavArgId
import me.proton.android.pass.navigation.api.NavItem
import me.proton.pass.domain.ItemId
import me.proton.pass.domain.ShareId

sealed class AppNavItem(
    val baseRoute: String,
    private val navArgIds: List<NavArgId> = emptyList(),
    override val isTopLevel: Boolean = false
) : NavItem {
    override val route = run {
        val argKeys = navArgIds.map { "{${it.key}}" }
        listOf(baseRoute).plus(argKeys).joinToString("/")
    }

    override val args = navArgIds.map {
        navArgument(it.key) { type = it.navType }
    }

    object OnBoarding : AppNavItem("onBoarding")

    object Auth : AppNavItem("auth")

    object Home : AppNavItem("home", isTopLevel = true)

    object Settings : AppNavItem("settings", isTopLevel = true)

    object Trash : AppNavItem("trash", isTopLevel = true)

    object Help : AppNavItem("help", isTopLevel = true)

    object CreateLogin : AppNavItem("createLogin", listOf(NavArgId.ShareId)) {
        fun createNavRoute(shareId: ShareId) = "$baseRoute/${shareId.id}"
    }

    object EditLogin : AppNavItem("editLogin", listOf(NavArgId.ShareId, NavArgId.ItemId)) {
        fun createNavRoute(shareId: ShareId, itemId: ItemId) =
            "$baseRoute/${shareId.id}/${itemId.id}"
    }

    object CreateAlias : AppNavItem("createAlias", listOf(NavArgId.ShareId)) {
        fun createNavRoute(shareId: ShareId) = "$baseRoute/${shareId.id}"
    }

    object EditAlias : AppNavItem("editAlias", listOf(NavArgId.ShareId, NavArgId.ItemId)) {
        fun createNavRoute(shareId: ShareId, itemId: ItemId) =
            "$baseRoute/${shareId.id}/${itemId.id}"
    }

    object CreateNote : AppNavItem("createNote", listOf(NavArgId.ShareId)) {
        fun createNavRoute(shareId: ShareId) = "$baseRoute/${shareId.id}"
    }

    object EditNote : AppNavItem("editNote", listOf(NavArgId.ShareId, NavArgId.ItemId)) {
        fun createNavRoute(shareId: ShareId, itemId: ItemId) =
            "$baseRoute/${shareId.id}/${itemId.id}"
    }

    object CreatePassword : AppNavItem("createPassword", listOf(NavArgId.ShareId)) {
        fun createNavRoute(shareId: ShareId) = "${CreatePassword.baseRoute}/${shareId.id}"
    }

    object EditPassword : AppNavItem("editPassword", listOf(NavArgId.ShareId, NavArgId.ItemId)) {
        fun createNavRoute(shareId: ShareId, itemId: ItemId) =
            "$baseRoute/${shareId.id}/${itemId.id}"
    }

    object ViewItem : AppNavItem("viewItem", listOf(NavArgId.ShareId, NavArgId.ItemId)) {
        fun createNavRoute(shareId: ShareId, itemId: ItemId) =
            "$baseRoute/${shareId.id}/${itemId.id}"
    }
}
