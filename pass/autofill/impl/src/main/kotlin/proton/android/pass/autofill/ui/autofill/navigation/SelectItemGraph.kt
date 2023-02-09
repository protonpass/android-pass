package proton.android.pass.autofill.ui.autofill.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavGraphBuilder
import proton.android.pass.autofill.entities.AutofillAppState
import proton.android.pass.autofill.entities.AutofillMappings
import proton.android.pass.autofill.ui.autofill.AutofillNavItem
import proton.android.pass.autofill.ui.autofill.select.SelectItemScreen
import proton.android.pass.navigation.api.AppNavigator
import proton.android.pass.navigation.api.composable

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.selectItemGraph(
    appNavigator: AppNavigator,
    state: AutofillAppState,
    onAutofillItemClicked: (AutofillMappings) -> Unit,
    onClose: () -> Unit
) {
    composable(AutofillNavItem.SelectItem) {
        SelectItemScreen(
            autofillAppState = state,
            onItemSelected = onAutofillItemClicked,
            onCreateLoginClicked = {
                appNavigator.navigate(AutofillNavItem.CreateLogin)
            },
            onClose = onClose
        )
    }
}
