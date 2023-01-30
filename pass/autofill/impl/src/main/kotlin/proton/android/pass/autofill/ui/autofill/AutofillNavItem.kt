package proton.android.pass.autofill.ui.autofill

import androidx.navigation.NamedNavArgument
import androidx.navigation.navArgument
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.navigation.api.AliasOptionalNavArgId
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.OptionalNavArgId
import proton.pass.domain.ShareId

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

    object Auth : AutofillNavItem("auth", isTopLevel = true)
    object SelectItem : AutofillNavItem("item/select", isTopLevel = true)
    object CreateLogin : AutofillNavItem("login/create", isTopLevel = true)
    object CreateAlias : AutofillNavItem(
        baseRoute = "alias/create",
        navArgIds = listOf(CommonNavArgId.ShareId),
        optionalArgIds = listOf(AliasOptionalNavArgId.Title, AliasOptionalNavArgId.IsDraft)
    ) {
        fun createNavRoute(
            shareId: ShareId,
            isDraft: Boolean = false,
            title: Option<String> = None
        ) = buildString {
            append("$baseRoute/${shareId.id}")
            append("?${AliasOptionalNavArgId.IsDraft.key}=$isDraft")
            if (title.isNotEmpty()) append("&${AliasOptionalNavArgId.Title.key}=${title.value()}")
        }
    }

    object CreateTotp : AutofillNavItem(baseRoute = "totp/create")
    object CameraTotp : AutofillNavItem(baseRoute = "totp/camera")
}
