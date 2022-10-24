package me.proton.android.pass.ui.navigation

import androidx.navigation.NavType
import androidx.navigation.navArgument
import me.proton.pass.domain.ItemId
import me.proton.pass.domain.ShareId

sealed class NavItem(
    val baseRoute: String,
    private val navArgIds: List<NavArgId> = emptyList(),
    val isTopLevel: Boolean = false
) {
    val route = run {
        val argKeys = navArgIds.map { "{${it.key}}" }
        listOf(baseRoute).plus(argKeys).joinToString("/")
    }

    val args = navArgIds.map {
        navArgument(it.key) { type = it.navType }
    }

    object Home : NavItem("home", isTopLevel = true)

    object Settings : NavItem("settings", isTopLevel = true)

    object Trash : NavItem("trash", isTopLevel = true)

    object Help : NavItem("help", isTopLevel = true)

    object CreateLogin : NavItem("createLogin", listOf(NavArgId.ShareId)) {
        fun createNavRoute(shareId: ShareId) = "$baseRoute/${shareId.id}"
    }

    object EditLogin : NavItem("editLogin", listOf(NavArgId.ShareId, NavArgId.ItemId)) {
        fun createNavRoute(shareId: ShareId, itemId: ItemId) =
            "$baseRoute/${shareId.id}/${itemId.id}"
    }

    object CreateAlias : NavItem("createAlias", listOf(NavArgId.ShareId)) {
        fun createNavRoute(shareId: ShareId) = "$baseRoute/${shareId.id}"
    }

    object EditAlias : NavItem("editAlias", listOf(NavArgId.ShareId, NavArgId.ItemId)) {
        fun createNavRoute(shareId: ShareId, itemId: ItemId) =
            "$baseRoute/${shareId.id}/${itemId.id}"
    }

    object CreateNote : NavItem("createNote", listOf(NavArgId.ShareId)) {
        fun createNavRoute(shareId: ShareId) = "$baseRoute/${shareId.id}"
    }

    object EditNote : NavItem("editNote", listOf(NavArgId.ShareId, NavArgId.ItemId)) {
        fun createNavRoute(shareId: ShareId, itemId: ItemId) =
            "$baseRoute/${shareId.id}/${itemId.id}"
    }

    object CreatePassword : NavItem("createPassword", listOf(NavArgId.ShareId)) {
        fun createNavRoute(shareId: ShareId) = "${CreatePassword.baseRoute}/${shareId.id}"
    }

    object EditPassword : NavItem("editPassword", listOf(NavArgId.ShareId, NavArgId.ItemId)) {
        fun createNavRoute(shareId: ShareId, itemId: ItemId) =
            "$baseRoute/${shareId.id}/${itemId.id}"
    }

    object ViewItem : NavItem("viewItem", listOf(NavArgId.ShareId, NavArgId.ItemId)) {
        fun createNavRoute(shareId: ShareId, itemId: ItemId) =
            "$baseRoute/${shareId.id}/${itemId.id}"
    }
}

enum class NavArgId(val key: String, val navType: NavType<*>) {
    ItemId("itemId", NavType.StringType),
    ShareId("shareId", NavType.StringType),
}
