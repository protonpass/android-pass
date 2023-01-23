package proton.android.pass.ui.create.login

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import proton.android.pass.common.api.toOption
import proton.android.pass.featurecreateitem.impl.alias.AliasItem
import proton.android.pass.featurecreateitem.impl.alias.RESULT_CREATED_DRAFT_ALIAS
import proton.android.pass.featurecreateitem.impl.login.UpdateLogin
import proton.android.pass.navigation.api.AppNavigator
import proton.android.pass.navigation.api.composable
import proton.android.pass.ui.navigation.AppNavItem

@OptIn(
    ExperimentalAnimationApi::class,
    ExperimentalLifecycleComposeApi::class
)
fun NavGraphBuilder.updateLoginGraph(nav: AppNavigator) {
    composable(AppNavItem.EditLogin) {
        val createdDraftAlias by nav.navState<AliasItem>(RESULT_CREATED_DRAFT_ALIAS, null)
            .collectAsStateWithLifecycle()

        UpdateLogin(
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
                        shareId = shareId.toOption(),
                        title = titleOption,
                        isDraft = true
                    )
                )
            },
            onSentToTrash = {
                nav.popUpTo(AppNavItem.Home)
            }
        )
    }
}
