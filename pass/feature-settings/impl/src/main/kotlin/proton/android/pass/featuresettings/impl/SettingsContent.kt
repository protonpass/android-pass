package proton.android.pass.featuresettings.impl

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import proton.android.pass.composecomponents.impl.loading.LoadingDialog
import proton.android.pass.composecomponents.impl.topbar.BackArrowTopAppBar
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState

@Composable
fun SettingsContent(
    modifier: Modifier = Modifier,
    state: SettingsUiState,
    onSelectThemeClick: () -> Unit,
    onClipboardClick: () -> Unit,
    onViewLogsClick: () -> Unit,
    onForceSyncClick: () -> Unit,
    onAppVersionClick: (String) -> Unit,
    onReportProblemClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    onTermsClick: () -> Unit,
    onUpClick: () -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            BackArrowTopAppBar(
                title = stringResource(R.string.title_settings),
                onUpClick = onUpClick
            )
        }
    ) { contentPadding ->
        if (state.isLoadingState == IsLoadingState.Loading) {
            LoadingDialog()
        }
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(contentPadding)
                .padding(16.dp)
        ) {
            PreferencesSection(
                theme = state.themePreference,
                onSelectThemeClick = onSelectThemeClick,
                onClipboardClick = onClipboardClick
            )
            AboutSection(
                onPrivacyClick = onPrivacyClick,
                onTermsClick = onTermsClick
            )
            ApplicationSection(
                onViewLogsClick = onViewLogsClick,
                onForceSyncClick = onForceSyncClick
            )
            AppSection(
                appVersion = state.appVersion,
                onAppVersionClick = onAppVersionClick,
                onReportProblemClick = onReportProblemClick
            )
        }
    }
}
