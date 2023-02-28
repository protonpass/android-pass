package proton.android.pass.ui.detail

import androidx.compose.animation.AnimatedContentScope.SlideDirection.Companion.Left
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.navigation.NavGraphBuilder
import proton.android.pass.featureitemdetail.impl.ItemDetailScreen
import proton.android.pass.navigation.api.AppNavigator
import proton.android.pass.navigation.api.composable
import proton.android.pass.ui.navigation.EditAlias
import proton.android.pass.ui.navigation.EditLogin
import proton.android.pass.ui.navigation.EditNote
import proton.android.pass.ui.navigation.ViewItem
import proton.pass.domain.ItemType

private const val TRANSITION_TIME_MILLIS = 500
private const val FADE_DELAY_TIME_MILLIS = 100

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.itemDetailGraph(nav: AppNavigator) {
    composable(
        navItem = ViewItem,
        enterTransition = {
            fadeIn(tween(TRANSITION_TIME_MILLIS, delayMillis = FADE_DELAY_TIME_MILLIS)) +
                slideIntoContainer(Left, tween(TRANSITION_TIME_MILLIS))
        },
        exitTransition = null,
        popEnterTransition = null,
        popExitTransition = null
    ) {
        ItemDetailScreen(
            onUpClick = { nav.onBackClick() },
            onEditClick = { shareId, itemId, itemType ->
                val destination = when (itemType) {
                    is ItemType.Login -> EditLogin
                    is ItemType.Note -> EditNote
                    is ItemType.Alias -> EditAlias
                    is ItemType.Password -> null // Edit password does not exist yet
                }
                val route = when (itemType) {
                    is ItemType.Login -> EditLogin.createNavRoute(shareId, itemId)
                    is ItemType.Note -> EditNote.createNavRoute(shareId, itemId)
                    is ItemType.Alias -> EditAlias.createNavRoute(shareId, itemId)
                    is ItemType.Password -> null // Edit password does not exist yet
                }

                if (destination != null && route != null) {
                    nav.navigate(destination, route)
                }
            }
        )
    }
}
