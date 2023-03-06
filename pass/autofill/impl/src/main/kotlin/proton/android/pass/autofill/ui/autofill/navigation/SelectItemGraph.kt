package proton.android.pass.autofill.ui.autofill.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavGraphBuilder
import proton.android.pass.autofill.entities.AutofillAppState
import proton.android.pass.autofill.entities.AutofillMappings
import proton.android.pass.autofill.ui.autofill.select.SelectItemScreen
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.composable

object SelectItem : NavItem(baseRoute = "item/select", isTopLevel = true)

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.selectItemGraph(
    state: AutofillAppState,
    onCreateLoginClicked: () -> Unit,
    onAutofillItemClicked: (AutofillMappings) -> Unit,
    onAutofillCancel: () -> Unit
) {
    composable(SelectItem) {
        BackHandler {
            onAutofillCancel()
        }
        SelectItemScreen(
            autofillAppState = state,
            onItemSelected = onAutofillItemClicked,
            onCreateLoginClicked = onCreateLoginClicked,
            onClose = onAutofillCancel
        )
    }
}
