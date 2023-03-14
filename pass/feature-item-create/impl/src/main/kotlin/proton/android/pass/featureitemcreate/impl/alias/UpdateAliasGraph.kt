package proton.android.pass.featureitemcreate.impl.alias

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.navigation.NavGraphBuilder
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.composable
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId

object EditAlias : NavItem(
    baseRoute = "alias/edit",
    navArgIds = listOf(CommonNavArgId.ShareId, CommonNavArgId.ItemId),
    optionalArgIds = emptyList()
) {
    fun createNavRoute(shareId: ShareId, itemId: ItemId) = buildString {
        append("$baseRoute/${shareId.id}/${itemId.id}")
    }
}

@OptIn(
    ExperimentalAnimationApi::class, ExperimentalMaterialApi::class,
    ExperimentalComposeUiApi::class
)
fun NavGraphBuilder.updateAliasGraph(
    onBackClick: () -> Unit,
    onAliasUpdatedSuccess: (ShareId, ItemId) -> Unit
) {
    composable(EditAlias) {
        UpdateAlias(
            onUpClick = onBackClick,
            onSuccess = onAliasUpdatedSuccess
        )
    }
}
