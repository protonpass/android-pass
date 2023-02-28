package proton.android.pass.ui.navigation

import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.navigation.api.AliasOptionalNavArgId
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.navigation.api.NavItem
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId

object OnBoarding : NavItem(baseRoute = "onboarding")

object Home : NavItem(baseRoute = "home", isTopLevel = true)

object Settings : NavItem(baseRoute = "settings", isTopLevel = true)

object Trash : NavItem(baseRoute = "trash", isTopLevel = true)


object CreateLogin : NavItem(
    baseRoute = "login/create",
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
        AliasOptionalNavArgId.IsDraft
    )
) {
    fun createNavRoute(
        shareId: Option<ShareId> = None,
        title: Option<String> = None,
        isDraft: Boolean = false
    ) = buildString {
        append(baseRoute)
        val map = mutableMapOf<String, Any>()
        if (shareId is Some) {
            map[CommonOptionalNavArgId.ShareId.key] = shareId.value.id
        }
        if (title is Some) {
            map[AliasOptionalNavArgId.Title.key] = title.value
        }
        map[AliasOptionalNavArgId.IsDraft.key] = isDraft
        val path = map.toPath()
        append(path)
    }
}

object EditAlias : NavItem(
    baseRoute = "alias/edit",
    navArgIds = listOf(CommonNavArgId.ShareId, CommonNavArgId.ItemId),
    optionalArgIds = listOf(AliasOptionalNavArgId.IsDraft)
) {
    fun createNavRoute(shareId: ShareId, itemId: ItemId) = buildString {
        append("$baseRoute/${shareId.id}/${itemId.id}")
        append("?${AliasOptionalNavArgId.IsDraft.key}=false")
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

object CreateTotp : NavItem(baseRoute = "totp/create")
object CameraTotp : NavItem(baseRoute = "totp/camera")
object PhotoPickerTotp : NavItem(baseRoute = "totp/photopicker")

fun Map<String, Any>.toPath() = this
    .map { "${it.key}=${it.value}" }
    .joinToString(
        prefix = "?",
        separator = "&"
    )
