package me.proton.android.pass.ui.navigation

import androidx.navigation.NavType
import androidx.navigation.navArgument
import me.proton.core.pass.domain.ItemId
import me.proton.core.pass.domain.ShareId

sealed class NavItem(
    val baseRoute: String,
    val navArgs: List<NavArg> = emptyList()
) {
    val route = run {
        val argKeys = navArgs.map { "{${it.key}}" }
        listOf(baseRoute).plus(argKeys).joinToString("/")
    }

    val args = navArgs.map {
        navArgument(it.key) { type = it.navType }
    }

    object Launcher : NavItem("auth")
    object Trash : NavItem("trash")
    object CreateLogin : NavItem("createLogin", listOf(NavArg.ShareId)) {
        fun createNavRoute(shareId: ShareId) = "$baseRoute/${shareId.id}"
    }
    object EditLogin : NavItem("editLogin", listOf(NavArg.ShareId, NavArg.ItemId)) {
        fun createNavRoute(shareId: ShareId, itemId: ItemId) = "$baseRoute/${shareId.id}/${itemId.id}"
    }
    object CreateNote : NavItem("createNote", listOf(NavArg.ShareId)) {
        fun createNavRoute(shareId: ShareId) = "$baseRoute/${shareId.id}"
    }
    object EditNote : NavItem("editNote", listOf(NavArg.ShareId, NavArg.ItemId)) {
        fun createNavRoute(shareId: ShareId, itemId: ItemId) = "$baseRoute/${shareId.id}/${itemId.id}"
    }
    object ViewItem : NavItem("viewItem", listOf(NavArg.ShareId, NavArg.ItemId)) {
        fun createNavRoute(shareId: ShareId, itemId: ItemId) = "$baseRoute/${shareId.id}/${itemId.id}"
    }
}

enum class NavArg(val key: String, val navType: NavType<*>) {
    ItemId("itemId", NavType.StringType),
    ShareId("shareId", NavType.StringType),
}
