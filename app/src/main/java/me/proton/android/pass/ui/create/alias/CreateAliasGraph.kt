package me.proton.android.pass.ui.create.alias

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import me.proton.android.pass.navigation.api.AppNavigator
import me.proton.android.pass.navigation.api.composable
import me.proton.android.pass.ui.navigation.AppNavItem
import me.proton.pass.presentation.create.alias.CreateAlias
import me.proton.pass.presentation.create.alias.RESULT_CREATED_ALIAS

@OptIn(
    ExperimentalAnimationApi::class
)
fun NavGraphBuilder.createAliasGraph(modifier: Modifier, nav: AppNavigator) {
    composable(AppNavItem.CreateAlias) {
        CreateAlias(
            modifier = modifier,
            onClose = { nav.onBackClick() },
            onUpClick = { nav.onBackClick() },
            onSuccess = { alias ->
                nav.navigateUpWithResult(RESULT_CREATED_ALIAS, alias)
            }
        )
    }
}
