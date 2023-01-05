package me.proton.android.pass.ui.create.login

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import me.proton.android.pass.featurecreateitem.impl.alias.AliasItem
import me.proton.android.pass.featurecreateitem.impl.alias.RESULT_CREATED_DRAFT_ALIAS
import me.proton.android.pass.featurecreateitem.impl.login.CreateLogin
import me.proton.android.pass.featurecreateitem.impl.login.InitialCreateLoginUiState
import me.proton.android.pass.navigation.api.AppNavigator
import me.proton.android.pass.navigation.api.composable
import me.proton.android.pass.ui.navigation.AppNavItem

@OptIn(
    ExperimentalAnimationApi::class,
    ExperimentalLifecycleComposeApi::class
)
fun NavGraphBuilder.createLoginGraph(nav: AppNavigator) {
    composable(AppNavItem.CreateLogin) {
        val createdDraftAlias by nav.navState<AliasItem>(RESULT_CREATED_DRAFT_ALIAS, null)
            .collectAsStateWithLifecycle()

        val initialContents = if (createdDraftAlias != null) {
            InitialCreateLoginUiState(
                aliasItem = createdDraftAlias
            )
        } else {
            null
        }
        CreateLogin(
            initialContents = initialContents,
            onClose = { nav.onBackClick() },
            onSuccess = { nav.onBackClick() },
            onCreateAliasClick = { shareId, titleOption ->
                nav.navigate(
                    destination = AppNavItem.CreateAlias,
                    route = AppNavItem.CreateAlias.createNavRoute(
                        shareId = shareId,
                        title = titleOption,
                        isDraft = true
                    )
                )
            }
        )
    }
}
