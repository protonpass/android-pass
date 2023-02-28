package proton.android.pass.autofill.ui.autofill.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavGraphBuilder
import proton.android.pass.autofill.entities.AutofillAppState
import proton.android.pass.autofill.entities.AutofillMappings
import proton.android.pass.autofill.ui.autofill.CreateLogin
import proton.android.pass.autofill.ui.autofill.SelectItem
import proton.android.pass.autofill.ui.autofill.select.SelectItemScreen
import proton.android.pass.navigation.api.AppNavigator
import proton.android.pass.navigation.api.composable

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.selectItemGraph(
    appNavigator: AppNavigator,
    state: AutofillAppState,
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
            onCreateLoginClicked = {
                appNavigator.navigate(CreateLogin)
            },
            onClose = onAutofillCancel
        )
    }
}
