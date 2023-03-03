package proton.android.pass.ui.navigation

import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.navigation.api.AliasOptionalNavArgId
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.toPath
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId

object Trash : NavItem(baseRoute = "trash", isTopLevel = true)

object EditLogin : NavItem(
    baseRoute = "login/edit",
    navArgIds = listOf(CommonNavArgId.ShareId, CommonNavArgId.ItemId)
) {
    fun createNavRoute(shareId: ShareId, itemId: ItemId) =
        "$baseRoute/${shareId.id}/${itemId.id}"
}

object CreateAlias : NavItem(
    baseRoute = "alias/create",
    optionalArgIds = listOf(
        CommonOptionalNavArgId.ShareId,
        AliasOptionalNavArgId.Title,
    )
) {
    fun createNavRoute(
        shareId: Option<ShareId> = None,
        title: Option<String> = None,
    ) = buildString {
        append(baseRoute)
        val map = mutableMapOf<String, Any>()
        if (shareId is Some) {
            map[CommonOptionalNavArgId.ShareId.key] = shareId.value.id
        }
        if (title is Some) {
            map[AliasOptionalNavArgId.Title.key] = title.value
        }
        val path = map.toPath()
        append(path)
    }
}

object EditAlias : NavItem(
    baseRoute = "alias/edit",
    navArgIds = listOf(CommonNavArgId.ShareId, CommonNavArgId.ItemId),
    optionalArgIds = emptyList()
) {
    fun createNavRoute(shareId: ShareId, itemId: ItemId) = buildString {
        append("$baseRoute/${shareId.id}/${itemId.id}")
    }
}

object CreateNote : NavItem(
    baseRoute = "note/create",
    optionalArgIds = listOf(CommonOptionalNavArgId.ShareId)
) {
    fun createNavRoute(shareId: Option<ShareId>) = buildString {
        append(baseRoute)
        val map = mutableMapOf<String, Any>()
        if (shareId is Some) {
            map[CommonOptionalNavArgId.ShareId.key] = shareId.value.id
        }
        val path = map.toPath()
        append(path)
    }
}

object EditNote : NavItem(
    baseRoute = "note/edit",
    navArgIds = listOf(CommonNavArgId.ShareId, CommonNavArgId.ItemId)
) {
    fun createNavRoute(shareId: ShareId, itemId: ItemId) =
        "$baseRoute/${shareId.id}/${itemId.id}"
}

object ViewItem : NavItem(
    baseRoute = "item",
    navArgIds = listOf(CommonNavArgId.ShareId, CommonNavArgId.ItemId)
) {
    fun createNavRoute(shareId: ShareId, itemId: ItemId) =
        "$baseRoute/${shareId.id}/${itemId.id}"
}
