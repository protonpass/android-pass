package proton.android.pass.featuresettings.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.form.PassDivider
import proton.android.pass.composecomponents.impl.setting.ColorSettingOption
import proton.android.pass.composecomponents.impl.setting.SettingOption
import proton.android.pass.composecomponents.impl.setting.SettingToggle
import me.proton.core.presentation.compose.R as CoreR

@Composable
fun ApplicationSection(
    modifier: Modifier = Modifier,
    shareTelemetry: Boolean,
    shareCrashes: Boolean,
    onEvent: (SettingsContentEvent) -> Unit
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = stringResource(R.string.settings_application_section_title),
            style = ProtonTheme.typography.defaultSmallWeak,
        )
        Column(
            modifier = Modifier.roundedContainerNorm()
        ) {
            SettingOption(
                text = stringResource(R.string.settings_option_view_logs),
                onClick = { onEvent(SettingsContentEvent.ViewLogs) }
            )
            PassDivider()
            ColorSettingOption(
                text = stringResource(R.string.settings_option_force_sync),
                textColor = PassTheme.colors.interactionNormMajor2,
                iconBgColor = PassTheme.colors.interactionNormMinor1,
                icon = {
                    Icon(
                        painter = painterResource(CoreR.drawable.ic_proton_arrows_rotate),
                        contentDescription = "",
                        tint = PassTheme.colors.interactionNormMajor2
                    )
                },
                onClick = { onEvent(SettingsContentEvent.ForceSync) }
            )
            PassDivider()
            SettingToggle(
                text = stringResource(R.string.settings_share_telemetry),
                isChecked = shareTelemetry,
                onClick = { onEvent(SettingsContentEvent.TelemetryChange(it)) }
            )
            PassDivider()
            SettingToggle(
                text = stringResource(R.string.settings_share_crashes),
                isChecked = shareCrashes,
                onClick = { onEvent(SettingsContentEvent.CrashReportChange(it)) }
            )
        }
    }
}

@Preview
@Composable
fun ApplicationSectionPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    PassTheme(isDark = input.first) {
        Surface {
            ApplicationSection(
                shareTelemetry = input.second,
                shareCrashes = input.second,
                onEvent = {}
            )
        }
    }
}
