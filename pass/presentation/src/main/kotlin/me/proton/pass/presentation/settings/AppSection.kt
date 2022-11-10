package me.proton.pass.presentation.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.component.ProtonSettingsHeader
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.usersettings.presentation.compose.view.CrashReportSettingToggleItem
import me.proton.core.usersettings.presentation.compose.view.TelemetrySettingToggleItem
import me.proton.pass.commonui.api.ThemePreviewProvider
import me.proton.pass.presentation.R

@Composable
fun AppSection(
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(vertical = 12.dp)) {
        ProtonSettingsHeader(title = stringResource(R.string.settings_app_section_title))
        TelemetrySettingToggleItem()
        CrashReportSettingToggleItem(divider = {})
    }
}

@Preview
@Composable
fun AppSectionPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    ProtonTheme(isDark = isDark) {
        Surface {
            AppSection()
        }
    }
}
