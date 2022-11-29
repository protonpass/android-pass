package me.proton.android.pass.ui.create.login

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import me.proton.android.pass.navigation.api.AppNavigator
import me.proton.android.pass.navigation.api.composable
import me.proton.android.pass.ui.navigation.AppNavItem
import me.proton.pass.presentation.create.alias.RESULT_CREATED_ALIAS
import me.proton.pass.presentation.create.login.CreateLogin
import me.proton.pass.presentation.create.login.InitialCreateLoginUiState

@OptIn(
    ExperimentalAnimationApi::class,
    ExperimentalLifecycleComposeApi::class
)
fun NavGraphBuilder.createLoginGraph(nav: AppNavigator) {
    composable(AppNavItem.CreateLogin) {
        val createdAlias by nav.navState<String>(RESULT_CREATED_ALIAS, null)
            .collectAsStateWithLifecycle()

        CreateLogin(
            initialContents = InitialCreateLoginUiState(
                username = createdAlias
            ),
            onClose = { nav.onBackClick() },
            onSuccess = { nav.onBackClick() },
            onCreateAliasClick = { shareId ->
                nav.navigate(AppNavItem.CreateAlias, AppNavItem.CreateAlias.createNavRoute(shareId))
            }
        )
    }
}
