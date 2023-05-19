package proton.android.pass.featureitemdetail.impl

import androidx.compose.animation.AnimatedContentScope.SlideDirection.Companion.Left
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.navigation.NavGraphBuilder
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.composable
import proton.pass.domain.ItemId
import proton.pass.domain.ItemType
import proton.pass.domain.ShareId

private const val TRANSITION_TIME_MILLIS = 500
private const val FADE_DELAY_TIME_MILLIS = 100

sealed interface ItemDetailNavigation {
    data class OnEdit(
        val shareId: ShareId,
        val itemId: ItemId,
        val itemType: ItemType
    ) : ItemDetailNavigation

    data class OnMigrate(
        val shareId: ShareId,
        val itemId: ItemId
    ) : ItemDetailNavigation

    data class OnCreateLoginFromAlias(val alias: String) : ItemDetailNavigation
    data class OnViewItem(val shareId: ShareId, val itemId: ItemId) : ItemDetailNavigation
    object Back : ItemDetailNavigation
    object Upgrade : ItemDetailNavigation
}

object ViewItem : NavItem(
    baseRoute = "item",
    navArgIds = listOf(CommonNavArgId.ShareId, CommonNavArgId.ItemId)
) {
    fun createNavRoute(shareId: ShareId, itemId: ItemId) =
        "$baseRoute/${shareId.id}/${itemId.id}"
}

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.itemDetailGraph(
    onNavigate: (ItemDetailNavigation) -> Unit,
) {
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
            onNavigate = onNavigate
        )
    }
}
