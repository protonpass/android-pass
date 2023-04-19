package proton.android.pass.featureprofile.impl

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavGraphBuilder
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.bottomSheet
import proton.android.pass.navigation.api.composable

object Profile : NavItem(baseRoute = "profile", isTopLevel = true)
object FeedbackBottomsheet : NavItem(baseRoute = "feedback/bottomsheet")

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.profileGraph(
    onAccountClick: () -> Unit,
    onListClick: () -> Unit,
    onCreateItemClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onFeedbackClick: () -> Unit,
) {
    composable(Profile) {
        ProfileScreen(
            onAccountClick = onAccountClick,
            onListClick = onListClick,
            onCreateItemClick = onCreateItemClick,
            onSettingsClick = onSettingsClick,
            onFeedbackClick = onFeedbackClick
        )
    }

    bottomSheet(FeedbackBottomsheet) {
        FeedbackBottomsheet()
    }
}
