package proton.android.pass.ui.navigation

import androidx.navigation.NamedNavArgument
import androidx.navigation.navArgument
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.navigation.api.AliasOptionalNavArgId
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.navigation.api.NavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.OptionalNavArgId
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId

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
                        if (it.navType.isNullableAllowed) {
                            nullable = true
                        }
                        if (it.default != null) {
                            defaultValue = it.default
                        }
                        type = it.navType
                    }
                }
            )

    object OnBoarding : AppNavItem("onboarding")

    object Auth : AppNavItem("auth")

    object Home : AppNavItem("home", isTopLevel = true)

    object Settings : AppNavItem("settings", isTopLevel = true)

    object Trash : AppNavItem("trash", isTopLevel = true)

    object Help : AppNavItem("help", isTopLevel = true)

    object VaultList : AppNavItem("vault", isTopLevel = true)
    object CreateVault : AppNavItem("vault/create")

    object CreateLogin : AppNavItem(
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

    object EditLogin : AppNavItem(
        baseRoute = "login/edit",
        navArgIds = listOf(CommonNavArgId.ShareId, CommonNavArgId.ItemId)
    ) {
        fun createNavRoute(shareId: ShareId, itemId: ItemId) =
            "$baseRoute/${shareId.id}/${itemId.id}"
    }

    object CreateAlias : AppNavItem(
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

    object EditAlias : AppNavItem(
        baseRoute = "alias/edit",
        navArgIds = listOf(CommonNavArgId.ShareId, CommonNavArgId.ItemId),
        optionalArgIds = listOf(AliasOptionalNavArgId.IsDraft)
    ) {
        fun createNavRoute(shareId: ShareId, itemId: ItemId) = buildString {
            append("$baseRoute/${shareId.id}/${itemId.id}")
            append("?${AliasOptionalNavArgId.IsDraft.key}=false")
        }
    }

    object CreateNote : AppNavItem(
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

    object EditNote :
        AppNavItem("note/edit", listOf(CommonNavArgId.ShareId, CommonNavArgId.ItemId)) {
        fun createNavRoute(shareId: ShareId, itemId: ItemId) =
            "$baseRoute/${shareId.id}/${itemId.id}"
    }

    object CreatePassword : AppNavItem("password/create")

    object ViewItem :
        AppNavItem("item", listOf(CommonNavArgId.ShareId, CommonNavArgId.ItemId)) {
        fun createNavRoute(shareId: ShareId, itemId: ItemId) =
            "$baseRoute/${shareId.id}/${itemId.id}"
    }

    object CreateTotp : AppNavItem(baseRoute = "totp/create")
    object CameraTotp : AppNavItem(baseRoute = "totp/camera")
    object PhotoPickerTotp : AppNavItem(baseRoute = "totp/photopicker")

    fun Map<String, Any>.toPath() = this
        .map { "${it.key}=${it.value}" }
        .joinToString(
            prefix = "?",
            separator = "&"
        )
}
