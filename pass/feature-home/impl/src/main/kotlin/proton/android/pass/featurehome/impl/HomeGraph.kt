package proton.android.pass.featurehome.impl

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavGraphBuilder
import proton.android.pass.common.api.Option
import proton.android.pass.commonuimodels.api.ItemTypeUiState
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.composable
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId

object Home : NavItem(baseRoute = "home", isTopLevel = true)

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.homeGraph(
    onNavigateEvent: (HomeNavigation) -> Unit
) {
    composable(Home) {
        NavHome(
            onNavigateEvent = onNavigateEvent
        )
    }
}

sealed interface HomeNavigation {
    data class AddItem(
        val shareId: Option<ShareId>,
        val itemTypeUiState: ItemTypeUiState
    ) : HomeNavigation

    data class EditLogin(val shareId: ShareId, val itemId: ItemId) : HomeNavigation
    data class EditNote(val shareId: ShareId, val itemId: ItemId) : HomeNavigation
    data class EditAlias(val shareId: ShareId, val itemId: ItemId) : HomeNavigation
    data class ItemDetail(val shareId: ShareId, val itemId: ItemId) : HomeNavigation
    object Auth : HomeNavigation
    object Profile : HomeNavigation
    object OnBoarding : HomeNavigation
    object CreateVault : HomeNavigation
    data class EditVault(val shareId: ShareId) : HomeNavigation
    data class DeleteVault(val shareId: ShareId) : HomeNavigation
}
