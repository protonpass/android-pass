package me.proton.pass.autofill.ui.autofill

import androidx.navigation.NamedNavArgument
import androidx.navigation.navArgument
import me.proton.android.pass.navigation.api.AliasOptionalNavArgId
import me.proton.android.pass.navigation.api.CommonNavArgId
import me.proton.android.pass.navigation.api.NavItem
import me.proton.android.pass.navigation.api.OptionalNavArgId
import me.proton.pass.common.api.Option
import me.proton.pass.domain.ShareId

sealed class AutofillNavItem(
    val baseRoute: String,
    private val navArgIds: List<CommonNavArgId> = emptyList(),
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

    object Auth : AutofillNavItem("auth", isTopLevel = true)
    object SelectItem : AutofillNavItem("selectItem", isTopLevel = true)
    object CreateLogin : AutofillNavItem("createLogin", isTopLevel = true)
    object CreateAlias : AutofillNavItem(
        baseRoute = "createAlias",
        navArgIds = listOf(CommonNavArgId.ShareId),
        optionalArgIds = listOf(AliasOptionalNavArgId.Title)
    ) {
        fun createNavRoute(shareId: ShareId, title: Option<String>) = buildString {
            append("$baseRoute/${shareId.id}")
            if (title.isNotEmpty()) append("?${title.value()}")
        }
    }
}
