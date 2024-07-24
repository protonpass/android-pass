/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.features.sl.sync.settings.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.commonui.api.asAnnotatedString
import proton.android.pass.commonui.api.body3Norm
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.item.SectionSubtitle
import proton.android.pass.composecomponents.impl.text.PassTextWithInnerLink
import proton.android.pass.features.sl.sync.R
import proton.android.pass.composecomponents.impl.R as CompR

@Composable
internal fun SimpleLoginSyncSettingsNotes(
    modifier: Modifier = Modifier,
    onCheckedChange: (Boolean) -> Unit,
    onLinkClick: () -> Unit,
    isChecked: Boolean
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(space = Spacing.small)
    ) {
        Row(
            modifier = Modifier
                .roundedContainerNorm()
                .fillMaxWidth()
                .padding(all = Spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SectionSubtitle(
                modifier = Modifier.weight(weight = 1f),
                text = stringResource(id = R.string.simple_login_sync_settings_notes_title)
                    .asAnnotatedString(),
            )

            Switch(
                checked = isChecked,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = PassTheme.colors.interactionNormMajor1
                ),
                onCheckedChange = onCheckedChange
            )
        }

        PassTextWithInnerLink(
            modifier = Modifier.padding(horizontal = Spacing.small),
            text = stringResource(id = R.string.simple_login_sync_settings_notes_description),
            innerLink = stringResource(id = CompR.string.action_learn_more),
            style = PassTheme.typography.body3Norm().copy(color = PassTheme.colors.textWeak),
            onClick = onLinkClick
        )

    }
}

@[Preview Composable]
internal fun SimpleLoginSyncSettingsNotesPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            SimpleLoginSyncSettingsNotes(
                isChecked = true,
                onCheckedChange = {},
                onLinkClick = {}
            )
        }
    }
}
