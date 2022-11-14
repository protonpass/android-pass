package me.proton.pass.autofill.ui.autofill

import androidx.navigation.navArgument
import me.proton.android.pass.navigation.api.NavArgId
import me.proton.android.pass.navigation.api.NavItem
import me.proton.pass.domain.ShareId

sealed class AutofillNavItem(
    val baseRoute: String,
    private val navArgIds: List<NavArgId> = emptyList(),
    override val isTopLevel: Boolean
) : NavItem {

    override val route = run {
        val argKeys = navArgIds.map { "{${it.key}}" }
        listOf(baseRoute).plus(argKeys).joinToString("/")
    }

    override val args = navArgIds.map {
        navArgument(it.key) { type = it.navType }
    }

    object Auth : AutofillNavItem("auth", isTopLevel = true)
    object SelectItem : AutofillNavItem("selectItem", isTopLevel = true)
    object CreateLogin : AutofillNavItem("createLogin", isTopLevel = true)
    object CreateAlias :
        AutofillNavItem("createAlias", listOf(NavArgId.ShareId), isTopLevel = false) {
        fun createNavRoute(shareId: ShareId) = "$baseRoute/${shareId.id}"
    }
}
