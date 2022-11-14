package me.proton.pass.autofill.ui.autofill

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.navigation.NavGraphBuilder
import me.proton.android.pass.navigation.api.AppNavigator
import me.proton.pass.autofill.entities.AutofillAppState
import me.proton.pass.autofill.entities.AutofillItem
import me.proton.pass.autofill.ui.autofill.navigation.authGraph
import me.proton.pass.autofill.ui.autofill.navigation.createAliasGraph
import me.proton.pass.autofill.ui.autofill.navigation.createLoginGraph
import me.proton.pass.autofill.ui.autofill.navigation.selectItemGraph
import me.proton.pass.presentation.components.model.ItemUiModel

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
    selectItemGraph(appNavigator, state, onAutofillItemClicked)
    createLoginGraph(appNavigator, state, onItemCreated)
    createAliasGraph(appNavigator, state)
}
