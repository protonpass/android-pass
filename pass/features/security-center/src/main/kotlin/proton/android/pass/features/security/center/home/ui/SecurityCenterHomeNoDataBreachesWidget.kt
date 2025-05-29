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

package proton.android.pass.features.security.center.home.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.icon.PassPlusIcon
import proton.android.pass.composecomponents.impl.widgets.PassSingleActionWidget
import proton.android.pass.features.security.center.R
import proton.android.pass.composecomponents.impl.R as CompR

@[Composable Suppress("FunctionMaxLength")]
internal fun SecurityCenterHomeNoDataBreachesWidget(modifier: Modifier = Modifier, onActionClick: () -> Unit) {
    PassSingleActionWidget(
        modifier = modifier,
        title = stringResource(id = R.string.security_center_home_dark_web_monitoring_title),
        message = stringResource(id = R.string.security_center_home_widget_no_breaches_subtitle),
        actionText = stringResource(id = CompR.string.action_enable),
        onActionClick = onActionClick,
        topRightIcon = {
            PassPlusIcon(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = Spacing.small,
                        end = Spacing.small
                    ),
                alignment = Alignment.CenterEnd
            )
        }
    )
}

@[Preview Composable Suppress("FunctionMaxLength")]
internal fun SecurityCenterHomeNoDataBreachesWPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            SecurityCenterHomeNoDataBreachesWidget(
                onActionClick = {}
            )
        }
    }
}
