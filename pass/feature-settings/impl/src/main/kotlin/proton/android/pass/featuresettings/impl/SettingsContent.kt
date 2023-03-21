package proton.android.pass.featuresettings.impl

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.PassTypography
import proton.android.pass.composecomponents.impl.loading.LoadingDialog
import proton.android.pass.composecomponents.impl.topbar.iconbutton.BackArrowCircleIconButton
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState

@Composable
fun SettingsContent(
    modifier: Modifier = Modifier,
    state: SettingsUiState,
    onSelectThemeClick: () -> Unit,
    onViewLogsClick: () -> Unit,
    onForceSyncClick: () -> Unit,
    onAppVersionClick: (String) -> Unit,
    onReportProblemClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    onTermsClick: () -> Unit,
    onUpClick: () -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            ProtonTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.title_settings),
                        style = PassTypography.hero
                    )
                },
                navigationIcon = {
                    BackArrowCircleIconButton(
                        modifier = modifier.padding(12.dp, 4.dp),
                        color = PassTheme.colors.accentBrandOpaque,
                        onUpClick = onUpClick
                    )
                }
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
                onClipboardClick = { }
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
            AccountSection(
                currentAccount = state.currentAccount,
                onLogoutClick = onLogoutClick
            )
        }
    }
}
