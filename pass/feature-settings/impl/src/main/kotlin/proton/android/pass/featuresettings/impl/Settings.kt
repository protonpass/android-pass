package proton.android.pass.featuresettings.impl

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.proton.core.compose.component.ProtonSettingsList
import proton.android.pass.preferences.value

@Suppress("UnusedPrivateMember")
@Composable
fun Settings(
    modifier: Modifier = Modifier,
    state: SettingsUiState,
    onCopyToClipboardChange: (Boolean) -> Unit,
    onForceSyncClick: () -> Unit,
    onAppVersionClick: (String) -> Unit,
    onReportProblemClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    ProtonSettingsList(modifier = modifier) {
        item {
            CopyTotpToClipboardSection(
                state = state.copyTotpToClipboard.value(),
                onToggleChange = onCopyToClipboardChange
            )
            Divider(modifier = Modifier.fillMaxWidth())
        }
/*
        item {
            AppearanceSection(
                theme = state.themePreference,
                onSelectThemeClick = onOpenThemeSelection
            )
            Divider(modifier = Modifier.fillMaxWidth())
        }*/

        item {
            AppSection(
                appVersion = state.appVersion,
                onForceSyncClick = onForceSyncClick,
                onAppVersionClick = onAppVersionClick,
                onReportProblemClick = onReportProblemClick
            )
            Divider(modifier = Modifier.fillMaxWidth())
        }
        item {
            AccountSection(
                currentAccount = state.currentAccount,
                onLogoutClick = onLogoutClick
            )
        }
    }
}
