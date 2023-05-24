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
    onNavigate: (SettingsNavigation) -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    SettingsContent(
        modifier = modifier,
        state = state,
        onEvent = {
            when (it) {
                is SettingsContentEvent.UseFaviconsChange -> viewModel.onUseFaviconsChange(it.value)
                is SettingsContentEvent.TelemetryChange -> viewModel.onTelemetryChange(it.value)
                is SettingsContentEvent.CrashReportChange -> viewModel.onCrashReportChange(it.value)
                SettingsContentEvent.ViewLogs -> onNavigate(SettingsNavigation.ViewLogs)
                SettingsContentEvent.ForceSync -> viewModel.onForceSync()
                SettingsContentEvent.SelectTheme -> onNavigate(SettingsNavigation.SelectTheme)
                SettingsContentEvent.Clipboard -> onNavigate(SettingsNavigation.ClipboardSettings)
                SettingsContentEvent.Privacy -> { openWebsite(context, "https://proton.me/legal/privacy") }
                SettingsContentEvent.Terms -> { openWebsite(context, "https://proton.me/legal/terms") }
                SettingsContentEvent.Up -> onNavigate(SettingsNavigation.Close)
                SettingsContentEvent.PrimaryVault -> onNavigate(SettingsNavigation.PrimaryVault)
            }
        }
    )
}
