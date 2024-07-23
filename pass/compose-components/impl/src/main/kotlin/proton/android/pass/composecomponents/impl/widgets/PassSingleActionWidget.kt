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

package proton.android.pass.composecomponents.impl.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.sp
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.commonui.api.applyIf
import proton.android.pass.commonui.api.heroNorm
import proton.android.pass.composecomponents.impl.buttons.PassCircleButton
import proton.android.pass.composecomponents.impl.container.roundedContainer

@Composable
fun PassSingleActionWidget(
    modifier: Modifier = Modifier,
    title: String,
    message: String,
    actionText: String,
    onActionClick: () -> Unit,
    topRightIcon: @Composable (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .roundedContainer(
                backgroundColor = PassTheme.colors.interactionNormMinor2,
                borderColor = PassTheme.colors.interactionNormMinor1
            )
            .padding(all = Spacing.small)
            .applyIf(
                condition = topRightIcon == null,
                ifTrue = { padding(top = Spacing.medium) }
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(space = Spacing.small)
    ) {
        topRightIcon?.let { icon ->
            icon()
        }

        Text(
            modifier = Modifier.padding(horizontal = Spacing.medium),
            text = title,
            fontSize = 24.sp,
            textAlign = TextAlign.Center,
            style = PassTheme.typography.heroNorm()
        )

        Text(
            modifier = Modifier.padding(horizontal = Spacing.medium),
            text = message,
            textAlign = TextAlign.Center,
            style = ProtonTheme.typography.body1Regular
        )

        PassCircleButton(
            modifier = Modifier.padding(
                start = Spacing.medium,
                top = Spacing.small,
                end = Spacing.medium,
                bottom = Spacing.medium
            ),
            text = actionText,
            onClick = onActionClick
        )
    }
}

@[Preview Composable]
internal fun PassSingleActionWidgetPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            PassSingleActionWidget(
                title = "Widget title",
                message = "Widget message",
                actionText = "Action",
                onActionClick = {}
            )
        }
    }
}
