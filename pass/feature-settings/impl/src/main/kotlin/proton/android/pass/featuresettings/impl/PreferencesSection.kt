/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

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
internal fun PreferencesSection(
    modifier: Modifier = Modifier,
    theme: ThemePreference,
    onEvent: (SettingsContentEvent) -> Unit
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
internal fun PreferencesSectionPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            PreferencesSection(
                theme = ThemePreference.Dark,
                onEvent = {}
            )
        }
    }
}
