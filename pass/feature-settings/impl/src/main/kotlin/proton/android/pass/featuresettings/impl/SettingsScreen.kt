package proton.android.pass.featuresettings.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.commonui.api.BrowserUtils.openWebsite

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onSelectThemeClick: () -> Unit,
    onClipboardClick: () -> Unit,
    onUpClick: () -> Unit,
    onViewLogsClick: () -> Unit,
    onPrimaryVaultClick: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    SettingsContent(
        modifier = modifier,
        state = state,
        onUseFaviconsChange = { viewModel.onUseFaviconsChange(it) },
        onViewLogsClick = onViewLogsClick,
        onForceSyncClick = { viewModel.onForceSync() },
        onSelectThemeClick = onSelectThemeClick,
        onClipboardClick = onClipboardClick,
        onPrivacyClick = { openWebsite(context, "https://proton.me/legal/privacy") },
        onTermsClick = { openWebsite(context, "https://proton.me/legal/terms") },
        onUpClick = onUpClick,
        onPrimaryVaultClick = onPrimaryVaultClick
    )
}
