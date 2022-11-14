package me.proton.pass.autofill.ui.autofill.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import me.proton.android.pass.navigation.api.AppNavigator
import me.proton.android.pass.navigation.api.composable
import me.proton.pass.autofill.entities.AutofillAppState
import me.proton.pass.autofill.ui.autofill.AutofillNavItem
import me.proton.pass.presentation.components.model.ItemUiModel
import me.proton.pass.presentation.create.alias.RESULT_CREATED_ALIAS
import me.proton.pass.presentation.create.login.CreateLogin
import me.proton.pass.presentation.create.login.InitialCreateLoginUiState

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialApi::class)
fun NavGraphBuilder.createLoginGraph(
    appNavigator: AppNavigator,
    state: AutofillAppState,
    onItemCreated: (ItemUiModel) -> Unit
) {
    composable(AutofillNavItem.CreateLogin) {
        val createdAlias by appNavigator.navState<String>(RESULT_CREATED_ALIAS, null)
            .collectAsStateWithLifecycle()

        val packageName = if (state.webDomain.isEmpty()) {
            state.packageName
        } else {
            null
        }

        CreateLogin(
            initialContents = InitialCreateLoginUiState(
                title = state.title,
                username = createdAlias,
                url = state.webDomain.value(),
                packageName = packageName
            ),
            onClose = { appNavigator.onBackClick() },
            onSuccess = onItemCreated,
            onCreateAliasClick = {
                appNavigator.navigate(
                    AutofillNavItem.CreateAlias,
                    AutofillNavItem.CreateAlias.createNavRoute(it)
                )
            }
        )
    }
}
