package proton.android.pass.autofill.ui.autofill.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavGraphBuilder
import proton.android.pass.autofill.entities.AutofillAppState
import proton.android.pass.autofill.entities.AutofillMappings
import proton.android.pass.autofill.ui.autofill.select.SelectItemScreen
import proton.android.pass.featuresearchoptions.api.SearchSortingType
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.composable

object SelectItem : NavItem(baseRoute = "item/select", isTopLevel = true)

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.selectItemGraph(
    state: AutofillAppState,
    onNavigate: (SelectItemNavigation) -> Unit
) {
    composable(SelectItem) {
        BackHandler {
            onNavigate(SelectItemNavigation.Cancel)
        }
        SelectItemScreen(
            autofillAppState = state,
            onNavigate = onNavigate
        )
    }
}

sealed interface SelectItemNavigation {
    object AddItem : SelectItemNavigation
    data class ItemSelected(val autofillMappings: AutofillMappings) : SelectItemNavigation
    data class SortingBottomsheet(val searchSortingType: SearchSortingType) : SelectItemNavigation
    object Cancel : SelectItemNavigation
}
