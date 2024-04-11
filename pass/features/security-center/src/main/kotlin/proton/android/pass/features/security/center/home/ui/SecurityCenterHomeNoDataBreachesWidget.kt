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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.sp
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.commonui.api.heroNorm
import proton.android.pass.composecomponents.impl.buttons.PassCircleButton
import proton.android.pass.composecomponents.impl.container.roundedContainer
import proton.android.pass.composecomponents.impl.icon.PassPlusIcon
import proton.android.pass.features.security.center.R
import proton.android.pass.composecomponents.impl.R as CompR

@[Composable Suppress("FunctionMaxLength")]
internal fun SecurityCenterHomeNoDataBreachesWidget(
    modifier: Modifier = Modifier,
    onActionClick: () -> Unit
) {
    Column(
        modifier = modifier
            .roundedContainer(
                backgroundColor = PassTheme.colors.interactionNormMinor2,
                borderColor = PassTheme.colors.interactionNormMinor1
            )
            .padding(all = Spacing.small),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(space = Spacing.small)
    ) {
        PassPlusIcon(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = Spacing.small,
                    end = Spacing.small
                ),
            alignment = Alignment.CenterEnd
        )

        Text(
            modifier = Modifier.padding(horizontal = Spacing.medium),
            text = stringResource(id = R.string.security_center_home_dark_web_monitoring_title),
            fontSize = 24.sp,
            textAlign = TextAlign.Center,
            style = PassTheme.typography.heroNorm()
        )

        Text(
            modifier = Modifier.padding(horizontal = Spacing.medium),
            text = stringResource(id = R.string.security_center_home_widget_no_breaches_subtitle),
            textAlign = TextAlign.Center,
            style = ProtonTheme.typography.body1Regular
        )

        PassCircleButton(
            modifier = Modifier.padding(
                start = Spacing.medium,
                top = Spacing.small,
                end = Spacing.medium,
                bottom = Spacing.medium,
            ),
            text = stringResource(id = CompR.string.action_enable),
            onClick = onActionClick
        )
    }
}

@[Preview Composable Suppress("FunctionMaxLength")]
fun SecurityCenterHomeNoDataBreachesWidgetPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            SecurityCenterHomeNoDataBreachesWidget(
                onActionClick = {}
            )
        }
    }
}
