package proton.android.pass.featureitemcreate.impl.alias

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavGraphBuilder
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.navigation.api.AliasOptionalNavArgId
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.composable
import proton.android.pass.navigation.api.toPath
import proton.pass.domain.ShareId

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

@OptIn(
    ExperimentalAnimationApi::class
)
fun NavGraphBuilder.createAliasGraph(
    onAliasCreatedSuccess: () -> Unit,
    onBackClick: () -> Unit,
) {
    composable(CreateAlias) {
        CreateAliasScreen(
            onClose = onBackClick,
            onUpClick = onBackClick,
            onAliasCreated = { onAliasCreatedSuccess() },
        )
    }
}
