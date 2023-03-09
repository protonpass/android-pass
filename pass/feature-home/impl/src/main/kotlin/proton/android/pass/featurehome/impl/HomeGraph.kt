package proton.android.pass.featurehome.impl

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavGraphBuilder
import proton.android.pass.common.api.Option
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.composable
import proton.pass.domain.ShareId

object Home : NavItem(baseRoute = "home", isTopLevel = true)

@OptIn(ExperimentalAnimationApi::class)
@Suppress("LongParameterList")
fun NavGraphBuilder.homeGraph(
    homeScreenNavigation: HomeScreenNavigation,
    onAddItemClick: (Option<ShareId>) -> Unit,
    onTrashClick: () -> Unit,
    onCreateVaultClick: () -> Unit,
    onEditVaultClick: (ShareId) -> Unit
) {
    composable(Home) {
        NavHome(
            homeScreenNavigation = homeScreenNavigation,
            onAddItemClick = onAddItemClick,
            onTrashClick = onTrashClick,
            onCreateVaultClick = onCreateVaultClick,
            onEditVaultClick = onEditVaultClick
        )
    }
}
