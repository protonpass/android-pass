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

package proton.android.pass.features.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.captionWeak
import me.proton.core.compose.theme.defaultSmallWeak
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.setting.SettingToggle

@Composable
internal fun PrivacySection(
    modifier: Modifier = Modifier,
    useFavicons: Boolean,
    useDigitalAssetLinks: Boolean,
    allowScreenshots: Boolean,
    onEvent: (SettingsContentEvent) -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(space = Spacing.medium)
    ) {
        Text(
            text = stringResource(R.string.settings_privacy_section_title),
            style = ProtonTheme.typography.defaultSmallWeak
        )

        Column(modifier = Modifier.roundedContainerNorm()) {
            SettingToggle(
                text = stringResource(R.string.settings_use_favicons_preference_title),
                isChecked = useFavicons,
                onClick = { onEvent(SettingsContentEvent.UseFaviconsChange(it)) }
            )
        }
        Text(
            text = stringResource(R.string.settings_use_favicons_preference_subtitle),
            style = ProtonTheme.typography.captionWeak.copy(PassTheme.colors.textWeak)
        )
        Column(modifier = Modifier.roundedContainerNorm()) {
            SettingToggle(
                text = stringResource(R.string.settings_use_digital_asset_link_preference_title),
                isChecked = useDigitalAssetLinks,
                onClick = { onEvent(SettingsContentEvent.UseDigitalAssetLinksChange(it)) }
            )
        }

        Text(
            text = stringResource(R.string.settings_use_digital_asset_link_preference_subtitle),
            style = ProtonTheme.typography.captionWeak.copy(PassTheme.colors.textWeak)
        )

        Box(modifier = Modifier.roundedContainerNorm()) {
            SettingToggle(
                text = stringResource(R.string.settings_allow_screenshots_preference_title),
                isChecked = allowScreenshots,
                onClick = { onEvent(SettingsContentEvent.AllowScreenshotsChange(it)) }
            )
        }

        Text(
            text = stringResource(R.string.settings_allow_screenshots_preference_subtitle),
            style = ProtonTheme.typography.captionWeak.copy(PassTheme.colors.textWeak)
        )
    }
}

@Preview
@Composable
internal fun UseFaviconsSectionPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    PassTheme(isDark = input.first) {
        Surface {
            PrivacySection(
                useFavicons = input.second,
                useDigitalAssetLinks = input.second,
                allowScreenshots = input.second,
                onEvent = {}
            )
        }
    }
}
