package proton.android.pass.featuresettings.impl

import androidx.compose.material.ScaffoldState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle


@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    onReportProblemClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    val viewModel: SettingsViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    SettingsContent(
        modifier = modifier,
        scaffoldState = scaffoldState,
        state = state,
        onThemeChange = { viewModel.onThemePreferenceChange(it) },
        onCopyToClipboardChange = { viewModel.onCopyToClipboardChange(it) },
        onForceSyncClick = { viewModel.onForceSync() },
        onAppVersionClick = { viewModel.copyAppVersion(it) },
        onReportProblemClick = onReportProblemClick,
        onLogoutClick = onLogoutClick,
    )
}
