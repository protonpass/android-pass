package me.proton.pass.autofill.ui.autofill.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavGraphBuilder
import me.proton.android.pass.navigation.api.AppNavigator
import me.proton.android.pass.navigation.api.composable
import me.proton.pass.autofill.entities.AutofillAppState
import me.proton.pass.autofill.entities.AutofillItem
import me.proton.pass.autofill.ui.autofill.AutofillNavItem
import me.proton.pass.autofill.ui.autofill.select.SelectItemInitialState
import me.proton.pass.autofill.ui.autofill.select.SelectItemScreen

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
