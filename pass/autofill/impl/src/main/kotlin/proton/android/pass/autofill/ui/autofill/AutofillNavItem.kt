package proton.android.pass.autofill.ui.autofill

import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.navigation.api.AliasOptionalNavArgId
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.NavItem
import proton.pass.domain.ShareId

object SelectItem : NavItem(baseRoute = "item/select", isTopLevel = true)
object CreateAlias : NavItem(
    baseRoute = "alias/create",
    navArgIds = listOf(CommonNavArgId.ShareId),
    optionalArgIds = listOf(AliasOptionalNavArgId.Title)
) {
    fun createNavRoute(
        shareId: ShareId,
        title: Option<String> = None
    ) = buildString {
        append("$baseRoute/${shareId.id}")
        if (title.isNotEmpty()) append("?${AliasOptionalNavArgId.Title.key}=${title.value()}")
    }
}
