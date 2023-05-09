package proton.android.pass.featureitemcreate.impl.alias

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavGraphBuilder
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.featureitemcreate.impl.alias.bottomsheet.CreateAliasBottomSheet
import proton.android.pass.navigation.api.AliasOptionalNavArgId
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.bottomSheet
import proton.android.pass.navigation.api.composable
import proton.android.pass.navigation.api.toPath
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId

object CreateAlias : NavItem(
    baseRoute = "alias/create/screen",
    optionalArgIds = listOf(CommonOptionalNavArgId.ShareId, AliasOptionalNavArgId.Title)
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

object CreateAliasBottomSheet : NavItem(
    baseRoute = "alias/create/bottomsheet",
    navArgIds = listOf(CommonOptionalNavArgId.ShareId),
    optionalArgIds = listOf(AliasOptionalNavArgId.Title)
) {
    fun createNavRoute(shareId: ShareId, title: Option<String> = None): String = buildString {
        append("$baseRoute/${shareId.id}")

        val map = mutableMapOf<String, Any>()
        if (title is Some) {
            map[AliasOptionalNavArgId.Title.key] = title.value
        }
        val optionalPath = map.toPath()
        append(optionalPath)
    }
}

sealed interface CreateAliasNavigation {
    data class CreatedFromBottomsheet(val alias: String) : CreateAliasNavigation
    data class Created(val shareId: ShareId, val itemId: ItemId, val alias: String) : CreateAliasNavigation
    object Close : CreateAliasNavigation
}

@OptIn(
    ExperimentalAnimationApi::class
)
fun NavGraphBuilder.createAliasGraph(
    onNavigate: (CreateAliasNavigation) -> Unit,
) {
    composable(CreateAlias) {
        CreateAliasScreen(onNavigate = onNavigate)
    }

    bottomSheet(CreateAliasBottomSheet) {
        val itemTitle = it.arguments?.getString(AliasOptionalNavArgId.Title.key) ?: ""
        CreateAliasBottomSheet(
            itemTitle = itemTitle,
            onNavigate = onNavigate
        )
    }
}
