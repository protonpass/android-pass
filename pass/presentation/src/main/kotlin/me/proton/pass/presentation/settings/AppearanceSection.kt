package me.proton.pass.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.android.pass.preferences.ThemePreference
import me.proton.core.compose.component.ProtonSettingsHeader
import me.proton.core.compose.theme.ProtonDimens
import me.proton.core.compose.theme.ProtonTheme
import me.proton.pass.commonui.api.ThemePreviewProvider
import me.proton.pass.presentation.R
import me.proton.pass.presentation.components.settings.SettingPreferenceDescription
import me.proton.pass.presentation.components.settings.SettingPreferenceSubtitle
import me.proton.pass.presentation.components.settings.SettingPreferenceTitle

@Composable
fun AppearanceSection(
    modifier: Modifier = Modifier,
    theme: ThemePreference,
    onSelectThemeClick: () -> Unit
) {
    val subtitle = when (theme) {
        ThemePreference.System -> R.string.settings_appearance_preference_subtitle_match_system
        ThemePreference.Dark -> R.string.settings_appearance_preference_subtitle_dark
        ThemePreference.Light -> R.string.settings_appearance_preference_subtitle_light
    }

    Column(modifier = modifier) {
        ProtonSettingsHeader(title = R.string.settings_appearance_section_title)
        Column(
            modifier = Modifier
                .clickable(onClick = onSelectThemeClick)
                .padding(horizontal = ProtonDimens.DefaultSpacing)
        ) {
            SettingPreferenceTitle(
                modifier = Modifier.padding(top = 16.dp),
                text = stringResource(R.string.settings_appearance_preference_title)
            )
            SettingPreferenceSubtitle(
                modifier = Modifier.padding(bottom = 12.dp),
                text = stringResource(subtitle)
            )
            SettingPreferenceDescription(
                modifier = Modifier.padding(bottom = 12.dp),
                text = stringResource(R.string.settings_appearance_preference_description)
            )
        }
    }
}

@Preview
@Composable
fun AppearanceSectionPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    ProtonTheme(isDark = isDark) {
        Surface {
            AppearanceSection(
                theme = ThemePreference.System,
                onSelectThemeClick = {}
            )
        }
    }
}
