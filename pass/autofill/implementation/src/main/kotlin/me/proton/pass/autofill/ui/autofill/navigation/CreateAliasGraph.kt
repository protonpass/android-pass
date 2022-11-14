package me.proton.pass.autofill.ui.autofill.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.navigation.NavGraphBuilder
import me.proton.android.pass.navigation.api.AppNavigator
import me.proton.android.pass.navigation.api.composable
import me.proton.pass.autofill.entities.AutofillAppState
import me.proton.pass.autofill.ui.autofill.AutofillNavItem
import me.proton.pass.presentation.create.alias.CreateAlias
import me.proton.pass.presentation.create.alias.InitialCreateAliasUiState
import me.proton.pass.presentation.create.alias.RESULT_CREATED_ALIAS

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialApi::class)
fun NavGraphBuilder.createAliasGraph(
    appNavigator: AppNavigator,
    state: AutofillAppState
) {
    composable(AutofillNavItem.CreateAlias) {
        CreateAlias(
            initialState = InitialCreateAliasUiState(
                title = state.title
            ),
            onSuccess = { alias ->
                appNavigator.navigateUpWithResult(RESULT_CREATED_ALIAS, alias)
            },
            onUpClick = { appNavigator.onBackClick() },
            onClose = { appNavigator.onBackClick() }
        )
    }
}
