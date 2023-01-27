package proton.android.pass.autofill.ui.autofill

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.navigation.NavGraphBuilder
import proton.android.pass.autofill.entities.AutofillAppState
import proton.android.pass.autofill.entities.AutofillItem
import proton.android.pass.autofill.ui.autofill.navigation.authGraph
import proton.android.pass.autofill.ui.autofill.navigation.createAliasGraph
import proton.android.pass.autofill.ui.autofill.navigation.createLoginGraph
import proton.android.pass.autofill.ui.autofill.navigation.createTotpGraph
import proton.android.pass.autofill.ui.autofill.navigation.selectItemGraph
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.navigation.api.AppNavigator

@ExperimentalAnimationApi
@ExperimentalMaterialApi
@ExperimentalComposeUiApi
fun NavGraphBuilder.appGraph(
    appNavigator: AppNavigator,
    state: AutofillAppState,
    onAutofillItemClicked: (AutofillItem) -> Unit,
    onItemCreated: (ItemUiModel) -> Unit,
    onFinished: () -> Unit
) {
    authGraph(appNavigator, onFinished)
    selectItemGraph(appNavigator, state, onAutofillItemClicked, onFinished)
    createLoginGraph(appNavigator, state, onItemCreated)
    createAliasGraph(appNavigator)
    createAliasGraph(appNavigator)
    createTotpGraph(appNavigator)
}
