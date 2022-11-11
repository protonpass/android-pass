package me.proton.android.pass.ui.create.login

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import me.proton.android.pass.ui.navigation.AppNavigator
import me.proton.android.pass.ui.navigation.NavItem
import me.proton.android.pass.ui.navigation.composable
import me.proton.pass.presentation.create.alias.RESULT_CREATED_ALIAS
import me.proton.pass.presentation.create.login.UpdateLogin

@OptIn(
    ExperimentalAnimationApi::class, ExperimentalMaterialApi::class,
    ExperimentalComposeUiApi::class
)
fun NavGraphBuilder.updateLoginGraph(nav: AppNavigator) {
    composable(NavItem.EditLogin) {
        val createdAlias by nav.navState<String>(RESULT_CREATED_ALIAS, null)
            .collectAsStateWithLifecycle()

        UpdateLogin(
            createdAlias = createdAlias,
            onUpClick = { nav.onBackClick() },
            onSuccess = { shareId, itemId ->
                nav.navigate(
                    destination = NavItem.ViewItem,
                    route = NavItem.ViewItem.createNavRoute(shareId, itemId),
                    backDestination = NavItem.Home
                )
            },
            onCreateAliasClick = { shareId ->
                nav.navigate(NavItem.CreateAlias, NavItem.CreateAlias.createNavRoute(shareId))
            }
        )
    }
}
