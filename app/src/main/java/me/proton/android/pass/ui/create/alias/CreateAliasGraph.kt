package me.proton.core.pass.presentation.create.alias

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.navigation.NavGraphBuilder
import me.proton.android.pass.ui.navigation.AppNavigator
import me.proton.android.pass.ui.navigation.NavArgId
import me.proton.android.pass.ui.navigation.NavItem
import me.proton.android.pass.ui.navigation.composable
import me.proton.android.pass.ui.navigation.findArg
import me.proton.core.pass.domain.ShareId

@OptIn(
    ExperimentalAnimationApi::class, ExperimentalMaterialApi::class,
    ExperimentalComposeUiApi::class
)
fun NavGraphBuilder.createAliasGraph(nav: AppNavigator) {
    composable(NavItem.CreateAlias) {
        val shareId = ShareId(it.findArg(NavArgId.ShareId))
        CreateAlias(
            onUpClick = { nav.onBackClick() },
            shareId = shareId,
            onSuccess = { itemId ->
                nav.navigate(
                    destination = NavItem.ViewItem,
                    route = NavItem.ViewItem.createNavRoute(shareId, itemId),
                    backDestination = NavItem.Home
                )
            }
        )
    }
}
