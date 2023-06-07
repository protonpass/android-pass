package proton.android.pass.featuretrial.impl

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.commonui.api.BrowserUtils

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun TrialScreen(
    modifier: Modifier = Modifier,
    onNavigate: (TrialNavigation) -> Unit,
    viewModel: TrialViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    BackHandler { onNavigate(TrialNavigation.Close) }

    TrialScreenContent(
        modifier = modifier,
        state = state,
        onNavigate = onNavigate,
        onLearnMore = {
            BrowserUtils.openWebsite(context, "https://proton.me/support/pass-trial")
        }
    )
}
