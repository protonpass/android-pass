package proton.android.pass.featuresettings.impl

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Divider
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.setting.SettingOption
import proton.android.pass.preferences.ThemePreference

@Composable
fun PreferencesSection(
    modifier: Modifier = Modifier,
    theme: ThemePreference,
    onEvent: (SettingsContentEvent) -> Unit,
) {
    Column(
        modifier = modifier.roundedContainerNorm()
    ) {
        val subtitle = stringResource(
            when (theme) {
                ThemePreference.System -> R.string.settings_appearance_preference_subtitle_match_system
                ThemePreference.Dark -> R.string.settings_appearance_preference_subtitle_dark
                ThemePreference.Light -> R.string.settings_appearance_preference_subtitle_light
            }
        )
        SettingOption(
            text = subtitle,
            label = stringResource(R.string.settings_appearance_preference_title),
            onClick = { onEvent(SettingsContentEvent.SelectTheme) }
        )
        Divider(color = PassTheme.colors.inputBorderNorm)
        SettingOption(
            text = stringResource(R.string.settings_option_clipboard),
            onClick = { onEvent(SettingsContentEvent.Clipboard) }
        )
    }
}

@Preview
@Composable
fun PreferencesSectionPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            PreferencesSection(
                theme = ThemePreference.Dark,
                onEvent = {}
            )
        }
    }
}
