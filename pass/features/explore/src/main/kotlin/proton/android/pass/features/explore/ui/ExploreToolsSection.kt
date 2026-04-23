/*
 * Copyright (c) 2026 Proton AG
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

package proton.android.pass.features.explore.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.features.explore.R
import proton.android.pass.features.explore.presentation.ExploreUiEvent
import me.proton.core.presentation.R as CoreR

@Composable
internal fun ExploreToolsSection(
    showPasswordHealth: Boolean,
    showCodes: Boolean,
    onEvent: (ExploreUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text.Body2Bold(
            modifier = Modifier.padding(bottom = 12.dp),
            text = stringResource(R.string.explore_section_tools_title)
        )
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(modifier = Modifier.roundedContainerNorm()) {
                ExploreToolRow(
                    iconRes = CoreR.drawable.ic_proton_key,
                    titleRes = R.string.explore_tool_password_generator_title,
                    subtitleRes = null,
                    onClick = { onEvent(ExploreUiEvent.PasswordGeneratorClicked) },
                    iconTint = PassTheme.colors.textNorm
                )
            }
            Column(modifier = Modifier.roundedContainerNorm()) {
                ExploreToolRow(
                    iconRes = R.drawable.ic_explore_dark_web,
                    titleRes = R.string.explore_tool_dark_web_monitor_title,
                    subtitleRes = R.string.explore_tool_dark_web_monitor_subtitle,
                    onClick = { onEvent(ExploreUiEvent.DarkWebMonitorClicked) }
                )
                if (showPasswordHealth) {
                    Divider(color = PassTheme.colors.inputBorderNorm)
                    ExploreToolRow(
                        iconRes = R.drawable.ic_explore_password_health,
                        titleRes = R.string.explore_tool_password_health_title,
                        subtitleRes = R.string.explore_tool_password_health_subtitle,
                        onClick = { onEvent(ExploreUiEvent.PasswordHealthClicked) }
                    )
                }
                if (showCodes) {
                    Divider(color = PassTheme.colors.inputBorderNorm)
                    ExploreToolRow(
                        iconRes = R.drawable.ic_explore_codes,
                        titleRes = R.string.explore_tool_codes_title,
                        subtitleRes = R.string.explore_tool_codes_subtitle,
                        onClick = { onEvent(ExploreUiEvent.CodesClicked) },
                        iconTint = PassTheme.colors.textNorm
                    )
                }
                Divider(color = PassTheme.colors.inputBorderNorm)
                ExploreToolRow(
                    iconRes = R.drawable.ic_explore_aliases,
                    titleRes = R.string.explore_tool_aliases_title,
                    subtitleRes = R.string.explore_tool_aliases_subtitle,
                    onClick = { onEvent(ExploreUiEvent.AliasesClicked) }
                )
            }
        }
    }
}

@Preview
@Composable
internal fun ExploreToolsSectionPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            ExploreToolsSection(showPasswordHealth = true, showCodes = true, onEvent = {})
        }
    }
}
