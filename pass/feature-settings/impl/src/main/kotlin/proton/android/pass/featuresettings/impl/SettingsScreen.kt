package proton.android.pass.featuresettings.impl

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
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onReportProblemClick: () -> Unit,
    onSelectThemeClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onUpClick: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    SettingsContent(
        modifier = modifier,
        state = state,
        onForceSyncClick = { viewModel.onForceSync() },
        onAppVersionClick = { viewModel.copyAppVersion(it) },
        onReportProblemClick = onReportProblemClick,
        onLogoutClick = onLogoutClick,
        onSelectThemeClick = onSelectThemeClick,
        onPrivacyClick = {
            BrowserUtils.openWebsite(context, "https://proton.me/legal/privacy")
        },
        onTermsClick = {
            BrowserUtils.openWebsite(context, "https://proton.me/legal/terms")
        },
        onUpClick = onUpClick
    )
}
