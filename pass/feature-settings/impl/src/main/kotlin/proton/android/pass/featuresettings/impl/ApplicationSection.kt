package proton.android.pass.featuresettings.impl

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultSmallWeak
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.container.roundedContainer
import proton.android.pass.composecomponents.impl.setting.ColorSettingOption
import proton.android.pass.composecomponents.impl.setting.SettingOption
import me.proton.core.presentation.compose.R as CoreR

@Composable
fun ApplicationSection(
    modifier: Modifier = Modifier,
    onViewLogsClick: () -> Unit,
    onForceSyncClick: () -> Unit
) {
    Column(modifier = modifier) {
        Text(
            modifier = Modifier.padding(16.dp),
            text = stringResource(R.string.settings_application_section_title),
            style = ProtonTheme.typography.defaultSmallWeak,
        )
        Column(
            modifier = Modifier.roundedContainer(ProtonTheme.colors.separatorNorm)
        ) {
            SettingOption(
                text = stringResource(R.string.settings_option_view_logs),
                onClick = onViewLogsClick
            )
            Divider()
            ColorSettingOption(
                text = stringResource(R.string.settings_option_force_sync),
                textColor = PassTheme.colors.accentBrandOpaque,
                iconBgColor = PassTheme.colors.accentBrandWeakest,
                icon = {
                    Icon(
                        painter = painterResource(CoreR.drawable.ic_proton_arrows_rotate),
                        contentDescription = "",
                        tint = PassTheme.colors.accentBrandOpaque
                    )
                },
                onClick = onForceSyncClick,
            )
        }
    }
}

@Preview
@Composable
fun ApplicationSectionPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            ApplicationSection(onViewLogsClick = {}, onForceSyncClick = {})
        }
    }
}
