package proton.android.pass.ui.create.alias

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.navigation.NavGraphBuilder
import proton.android.pass.featurecreateitem.impl.alias.UpdateAlias
import proton.android.pass.featurehome.impl.Home
import proton.android.pass.navigation.api.AppNavigator
import proton.android.pass.navigation.api.composable
import proton.android.pass.ui.navigation.EditAlias
import proton.android.pass.ui.navigation.ViewItem

@OptIn(
    ExperimentalAnimationApi::class, ExperimentalMaterialApi::class,
    ExperimentalComposeUiApi::class
)
fun NavGraphBuilder.updateAliasGraph(nav: AppNavigator) {
    composable(EditAlias) {
        UpdateAlias(
            onUpClick = { nav.onBackClick() },
            onSuccess = { shareId, itemId ->
                nav.navigate(
                    destination = ViewItem,
                    route = ViewItem.createNavRoute(shareId, itemId),
                    backDestination = Home
                )
            }
        )
    }
}
