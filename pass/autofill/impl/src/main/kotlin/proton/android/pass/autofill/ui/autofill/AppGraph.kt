package proton.android.pass.autofill.ui.autofill

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.navigation.NavGraphBuilder
import proton.android.pass.autofill.entities.AutofillAppState
import proton.android.pass.autofill.entities.AutofillItem
import proton.android.pass.autofill.entities.AutofillMappings
import proton.android.pass.autofill.ui.autofill.navigation.createAliasGraph
import proton.android.pass.autofill.ui.autofill.navigation.createLoginGraph
import proton.android.pass.autofill.ui.autofill.navigation.createTotpGraph
import proton.android.pass.autofill.ui.autofill.navigation.selectItemGraph
import proton.android.pass.featureauth.impl.authGraph
import proton.android.pass.navigation.api.AppNavigator

@Suppress("LongParameterList")
@ExperimentalAnimationApi
@ExperimentalMaterialApi
@ExperimentalComposeUiApi
fun NavGraphBuilder.appGraph(
    appNavigator: AppNavigator,
    autofillAppState: AutofillAppState,
    selectedAutofillItem: AutofillItem?,
    onAutofillSuccess: (AutofillMappings) -> Unit,
    onAutofillCancel: () -> Unit,
    onAutofillItemReceived: (AutofillItem) -> Unit
) {
    authGraph(
        onNavigateBack = onAutofillCancel,
        onAuthSuccessful = {
            if (selectedAutofillItem != null) {
                onAutofillItemReceived(selectedAutofillItem)
            } else {
                appNavigator.navigate(SelectItem)
            }
        },
        onAuthDismissed = onAutofillCancel,
        onAuthFailed = onAutofillCancel
    )
    selectItemGraph(appNavigator, autofillAppState, onAutofillSuccess, onAutofillCancel)
    createLoginGraph(appNavigator, autofillAppState, onAutofillItemReceived)
    createAliasGraph(appNavigator)
    createAliasGraph(appNavigator)
    createTotpGraph(appNavigator)
}
