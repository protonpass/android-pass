package proton.android.pass.ui.create.alias

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavGraphBuilder
import proton.android.pass.featurecreateitem.impl.alias.CreateAliasScreen
import proton.android.pass.featurecreateitem.impl.alias.RESULT_CREATED_DRAFT_ALIAS
import proton.android.pass.navigation.api.AppNavigator
import proton.android.pass.navigation.api.composable
import proton.android.pass.ui.navigation.CreateAlias

@OptIn(
    ExperimentalAnimationApi::class
)
fun NavGraphBuilder.createAliasGraph(nav: AppNavigator) {
    composable(CreateAlias) {
        CreateAliasScreen(
            onClose = { nav.onBackClick() },
            onUpClick = { nav.onBackClick() },
            onAliasCreated = { nav.onBackClick() },
            onAliasDraftCreated = { aliasItem ->
                nav.navigateUpWithResult(RESULT_CREATED_DRAFT_ALIAS, aliasItem)
            }
        )
    }
}
