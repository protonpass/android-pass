package me.proton.pass.autofill.ui.autofill.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavGraphBuilder
import me.proton.android.pass.navigation.api.AppNavigator
import me.proton.android.pass.navigation.api.composable
import me.proton.pass.autofill.ui.autofill.AutofillNavItem
import me.proton.pass.presentation.create.alias.CreateAlias
import me.proton.pass.presentation.create.alias.RESULT_CREATED_DRAFT_ALIAS

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.createAliasGraph(appNavigator: AppNavigator) {
    composable(AutofillNavItem.CreateAlias) {
        CreateAlias(
            onAliasCreated = { appNavigator.onBackClick() },
            onAliasDraftCreated = { aliasItem ->
                appNavigator.navigateUpWithResult(RESULT_CREATED_DRAFT_ALIAS, aliasItem)
            },
            onUpClick = { appNavigator.onBackClick() },
            onClose = { appNavigator.onBackClick() }
        )
    }
}
