package proton.android.pass.featuresettings.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.default
import me.proton.core.compose.theme.defaultSmallWeak
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.buttons.CircleIconButton
import proton.android.pass.composecomponents.impl.container.roundedContainer
import proton.android.pass.composecomponents.impl.setting.SettingOption
import me.proton.core.presentation.compose.R as CoreR
import proton.android.pass.composecomponents.impl.R as ComponentsR

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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.settings_option_force_sync),
                    style = ProtonTheme.typography.default,
                    color = PassTheme.colors.accentBrandOpaque
                )
                CircleIconButton(
                    backgroundColor = PassTheme.colors.accentPurpleWeakest,
                    onClick = onForceSyncClick
                ) {
                    Icon(
                        painter = painterResource(CoreR.drawable.ic_proton_arrows_rotate),
                        contentDescription = stringResource(
                            ComponentsR.string.regenerate_password_icon_content_description
                        ),
                        tint = PassTheme.colors.accentBrandOpaque
                    )
                }
            }
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
