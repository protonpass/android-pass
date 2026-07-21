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

package proton.android.pass.composecomponents.impl.item.details.sections.login.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LocalMinimumInteractiveComponentEnforcement
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.R
import proton.android.pass.composecomponents.impl.buttons.Button

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun MonitorCheckActionButton(
    isRestoreMode: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPending: Boolean = false
) {
    CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
        Button.Circular(
            modifier = modifier.alpha(if (isPending) PENDING_ALPHA else 1f),
            color = PassTheme.colors.interactionNormMinor2,
            contentPadding = PaddingValues(
                horizontal = Spacing.medium,
                vertical = Spacing.small
            ),
            enabled = !isPending,
            onClick = onClick
        ) {
            Text(
                text = stringResource(
                    id = if (isRestoreMode) {
                        R.string.login_item_monitor_action_restore
                    } else {
                        R.string.login_item_monitor_action_ignore
                    }
                ),
                style = ProtonTheme.typography.captionMedium,
                color = PassTheme.colors.interactionNormMajor2
            )
        }
    }
}

private const val PENDING_ALPHA = 0.5f

@[Preview Composable]
internal fun MonCheckActionBtnPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            Column(
                verticalArrangement = Arrangement.spacedBy(Spacing.small),
                modifier = Modifier.padding(Spacing.medium)
            ) {
                MonitorCheckActionButton(isRestoreMode = false, onClick = {})
                MonitorCheckActionButton(isRestoreMode = false, isPending = true, onClick = {})
                MonitorCheckActionButton(isRestoreMode = true, onClick = {})
                MonitorCheckActionButton(isRestoreMode = true, isPending = true, onClick = {})
            }
        }
    }
}
