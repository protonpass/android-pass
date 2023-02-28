package proton.android.pass.autofill.ui.autofill.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavGraphBuilder
import proton.android.pass.autofill.ui.autofill.CreateAlias
import proton.android.pass.featurecreateitem.impl.alias.CreateAliasScreen
import proton.android.pass.featurecreateitem.impl.alias.RESULT_CREATED_DRAFT_ALIAS
import proton.android.pass.navigation.api.AppNavigator
import proton.android.pass.navigation.api.composable

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.createAliasGraph(appNavigator: AppNavigator) {
    composable(CreateAlias) {
        CreateAliasScreen(
            onAliasCreated = { appNavigator.onBackClick() },
            onAliasDraftCreated = { aliasItem ->
                appNavigator.navigateUpWithResult(RESULT_CREATED_DRAFT_ALIAS, aliasItem)
            },
            onUpClick = { appNavigator.onBackClick() },
            onClose = { appNavigator.onBackClick() }
        )
    }
}
