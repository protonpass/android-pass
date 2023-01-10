package proton.android.pass.autofill.ui.autofill.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavGraphBuilder
import proton.android.pass.navigation.api.AppNavigator
import proton.android.pass.navigation.api.composable
import proton.android.pass.autofill.entities.AutofillAppState
import proton.android.pass.autofill.entities.AutofillItem
import proton.android.pass.autofill.ui.autofill.AutofillNavItem
import proton.android.pass.autofill.ui.autofill.select.SelectItemInitialState
import proton.android.pass.autofill.ui.autofill.select.SelectItemScreen

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.selectItemGraph(
    appNavigator: AppNavigator,
    state: AutofillAppState,
    onAutofillItemClicked: (AutofillItem) -> Unit,
    onClose: () -> Unit
) {
    composable(AutofillNavItem.SelectItem) {
        SelectItemScreen(
            initialState = SelectItemInitialState(
                packageName = state.packageName,
                webDomain = state.webDomain
            ),
            onItemSelected = onAutofillItemClicked,
            onCreateLoginClicked = {
                appNavigator.navigate(AutofillNavItem.CreateLogin)
            },
            onClose = onClose
        )
    }
}
