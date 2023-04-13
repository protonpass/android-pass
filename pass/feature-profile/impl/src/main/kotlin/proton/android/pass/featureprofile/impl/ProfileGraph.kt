package proton.android.pass.featureprofile.impl

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraphBuilder
import proton.android.pass.commonui.api.BrowserUtils
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
        val context = LocalContext.current
        FeedbackBottomsheetContent(
            onSendEmail = {
                context.startActivity(
                    Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:pass@proton.me"))
                )
            },
            onOpenTwitter = {
                BrowserUtils.openWebsite(context, "https://twitter.com/proton_pass")
            },
            onOpenReddit = {
                BrowserUtils.openWebsite(context, "https://www.reddit.com/r/ProtonPass/")
            }
        )
    }
}
