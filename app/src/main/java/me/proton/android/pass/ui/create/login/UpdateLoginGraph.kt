package me.proton.android.pass.ui.create.login

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import me.proton.android.pass.navigation.api.AppNavigator
import me.proton.android.pass.navigation.api.composable
import me.proton.android.pass.ui.navigation.AppNavItem
import me.proton.pass.presentation.create.alias.AliasItem
import me.proton.pass.presentation.create.alias.RESULT_CREATED_DRAFT_ALIAS
import me.proton.pass.presentation.create.login.UpdateLogin

@OptIn(
    ExperimentalAnimationApi::class,
    ExperimentalLifecycleComposeApi::class
)
fun NavGraphBuilder.updateLoginGraph(modifier: Modifier, nav: AppNavigator) {
    composable(AppNavItem.EditLogin) {
        val createdDraftAlias by nav.navState<AliasItem>(RESULT_CREATED_DRAFT_ALIAS, null)
            .collectAsStateWithLifecycle()

        UpdateLogin(
            modifier = modifier,
            draftAlias = createdDraftAlias,
            onUpClick = { nav.onBackClick() },
            onSuccess = { shareId, itemId ->
                nav.navigate(
                    destination = AppNavItem.ViewItem,
                    route = AppNavItem.ViewItem.createNavRoute(shareId, itemId),
                    backDestination = AppNavItem.Home
                )
            },
            onCreateAliasClick = { shareId, titleOption ->
                nav.navigate(
                    AppNavItem.CreateAlias,
                    AppNavItem.CreateAlias.createNavRoute(
                        shareId = shareId,
                        title = titleOption,
                        isDraft = true
                    )
                )
            }
        )
    }
}
