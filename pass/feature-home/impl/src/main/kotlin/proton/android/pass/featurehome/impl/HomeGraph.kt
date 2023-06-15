package proton.android.pass.featurehome.impl

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.navigation.NavGraphBuilder
import proton.android.pass.common.api.Option
import proton.android.pass.commonuimodels.api.ItemTypeUiState
import proton.android.pass.featuresearchoptions.api.SearchSortingType
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.composable
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId

object Home : NavItem(baseRoute = "home", isTopLevel = true, noHistory = true)

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialApi::class)
fun NavGraphBuilder.homeGraph(
    onNavigateEvent: (HomeNavigation) -> Unit,
) {
    composable(Home) {
        HomeScreen(
            modifier = Modifier.testTag(HomeScreenTestTag.screen),
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
    data class VaultOptions(val shareId: ShareId) : HomeNavigation
    data class SortingBottomsheet(val searchSortingType: SearchSortingType) : HomeNavigation
    object TrialInfo : HomeNavigation
}
