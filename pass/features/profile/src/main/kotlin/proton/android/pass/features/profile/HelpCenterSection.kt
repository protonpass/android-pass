/*
 * Copyright (c) 2023-2024 Proton AG
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

package proton.android.pass.features.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultSmallWeak
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.form.PassDivider
import proton.android.pass.composecomponents.impl.setting.SettingOption

@Composable
fun HelpCenterProfileSection(
    modifier: Modifier = Modifier,
    onFeedbackClick: () -> Unit,
    onImportExportClick: () -> Unit,
    onRateAppClick: () -> Unit,
    onTutorialClick: () -> Unit
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Spacing.medium)) {
        Text(
            text = stringResource(R.string.profile_help_center),
            style = ProtonTheme.typography.defaultSmallWeak
        )
        Column(
            modifier = Modifier.roundedContainerNorm()
        ) {
            SettingOption(
                text = stringResource(R.string.profile_option_feedback),
                onClick = onFeedbackClick
            )
            PassDivider()
            SettingOption(
                text = stringResource(R.string.profile_option_import_export),
                isLink = true,
                onClick = onImportExportClick
            )
            PassDivider()
            SettingOption(
                text = stringResource(R.string.profile_option_tutorial),
                isLink = true,
                onClick = onTutorialClick
            )
            if (SHOW_RATING_OPTION) {
                PassDivider()
                SettingOption(
                    text = stringResource(R.string.profile_option_rating),
                    onClick = onRateAppClick
                )
            }
        }
    }
}

@Preview
@Composable
fun HelpCenterSectionPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            HelpCenterProfileSection(
                onFeedbackClick = {},
                onImportExportClick = {},
                onRateAppClick = {},
                onTutorialClick = {}
            )
        }
    }
}

const val SHOW_RATING_OPTION = false
