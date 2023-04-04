package proton.android.pass.featuresettings.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.commonui.api.BrowserUtils
import proton.android.pass.commonui.api.PassTheme

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onSelectThemeClick: () -> Unit,
    onClipboardClick: () -> Unit,
    onUpClick: () -> Unit,
    onViewLogsClick: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    SettingsContent(
        modifier = modifier
            .fillMaxSize()
            .background(PassTheme.colors.backgroundStrong),
        state = state,
        onViewLogsClick = onViewLogsClick,
        onForceSyncClick = { viewModel.onForceSync() },
        onSelectThemeClick = onSelectThemeClick,
        onClipboardClick = onClipboardClick,
        onPrivacyClick = {
            BrowserUtils.openWebsite(context, "https://proton.me/legal/privacy")
        },
        onTermsClick = {
            BrowserUtils.openWebsite(context, "https://proton.me/legal/terms")
        },
        onUpClick = onUpClick
    )
}
