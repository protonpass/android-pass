package proton.android.pass.featureprofile.impl

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavGraphBuilder
import proton.android.pass.featureprofile.impl.applock.AppLockBottomsheet
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.bottomSheet
import proton.android.pass.navigation.api.composable

object Profile : NavItem(baseRoute = "profile", isTopLevel = true)
object FeedbackBottomsheet : NavItem(baseRoute = "feedback/bottomsheet")
object AppLockBottomsheet : NavItem(baseRoute = "applock/bottomsheet")

sealed interface ProfileNavigation {
    object Account : ProfileNavigation
    object AppLock : ProfileNavigation
    object List : ProfileNavigation
    object CreateItem : ProfileNavigation
    object Settings : ProfileNavigation
    object Feedback : ProfileNavigation
    object Report : ProfileNavigation
}

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.profileGraph(
    dismissBottomSheet: () -> Unit,
    onNavigateEvent: (ProfileNavigation) -> Unit
) {
    composable(Profile) {
        ProfileScreen(onNavigateEvent = onNavigateEvent)
    }

    bottomSheet(FeedbackBottomsheet) {
        FeedbackBottomsheet(onNavigateEvent = onNavigateEvent)
    }

    bottomSheet(AppLockBottomsheet) {
        AppLockBottomsheet(onClose = dismissBottomSheet)
    }
}
