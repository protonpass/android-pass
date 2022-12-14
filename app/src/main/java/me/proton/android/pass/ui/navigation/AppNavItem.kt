package me.proton.android.pass.ui.navigation

import androidx.navigation.NamedNavArgument
import androidx.navigation.navArgument
import me.proton.android.pass.navigation.api.AliasOptionalNavArgId
import me.proton.android.pass.navigation.api.NavArgId
import me.proton.android.pass.navigation.api.NavItem
import me.proton.android.pass.navigation.api.OptionalNavArgId
import me.proton.pass.common.api.None
import me.proton.pass.common.api.Option
import me.proton.pass.domain.ItemId
import me.proton.pass.domain.ShareId

sealed class AppNavItem(
    val baseRoute: String,
    private val navArgIds: List<NavArgId> = emptyList(),
    private val optionalArgIds: List<OptionalNavArgId> = emptyList(),
    override val isTopLevel: Boolean = false
) : NavItem {
    override val route = run {
        buildString {
            val argKeys = navArgIds.map { "{${it.key}}" }
            append(listOf(baseRoute).plus(argKeys).joinToString("/"))
            if (optionalArgIds.isNotEmpty()) {
                val optionalArgKeys = optionalArgIds.joinToString(
                    prefix = "?",
                    separator = "&",
                    transform = { "${it.key}={${it.key}}" }
                )
                append(optionalArgKeys)
            }
        }
    }

    override val args: List<NamedNavArgument> =
        navArgIds.map { navArgument(it.key) { type = it.navType } }
            .plus(
                optionalArgIds.map {
                    navArgument(it.key) {
                        nullable = true
                        type = it.navType
                    }
                }
            )

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

    object CreateAlias : AppNavItem(
        baseRoute = "createAlias",
        navArgIds = listOf(NavArgId.ShareId),
        optionalArgIds = listOf(AliasOptionalNavArgId.Title)
    ) {
        fun createNavRoute(shareId: ShareId, title: Option<String> = None) = buildString {
            append("$baseRoute/${shareId.id}")
            if (title.isNotEmpty()) append("?${AliasOptionalNavArgId.Title.key}=${title.value()}")
        }
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

    object ViewItem : AppNavItem("viewItem", listOf(NavArgId.ShareId, NavArgId.ItemId)) {
        fun createNavRoute(shareId: ShareId, itemId: ItemId) =
            "$baseRoute/${shareId.id}/${itemId.id}"
    }
}
