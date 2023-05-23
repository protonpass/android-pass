package proton.android.pass.featureprofile.impl

import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import proton.android.pass.commonui.api.BrowserUtils.openWebsite

@Composable
fun FeedbackBottomsheet(onNavigateEvent: (ProfileNavigation) -> Unit) {
    val context = LocalContext.current
    FeedbackBottomsheetContent(
        onSendReport = {
            onNavigateEvent(ProfileNavigation.Report)
        },
        onOpenReddit = {
            openWebsite(context, PASS_REDDIT)
        }
    )
}

@VisibleForTesting
const val PASS_REDDIT = "https://www.reddit.com/r/ProtonPass/"
