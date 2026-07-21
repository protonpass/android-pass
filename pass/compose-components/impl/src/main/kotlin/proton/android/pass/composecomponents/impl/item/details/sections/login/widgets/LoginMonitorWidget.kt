/*
 * Copyright (c) 2024-2026 Proton AG
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

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.text.Text

@Composable
internal fun LoginMonitorWidget(
    modifier: Modifier = Modifier,
    title: String,
    @DrawableRes iconResId: Int,
    titleColor: Color,
    @StringRes subtitleResId: Int? = null,
    subtitle: String? = null,
    subtitleColor: Color = PassTheme.colors.textWeak,
    trailingAction: (@Composable RowScope.() -> Unit)? = null,
    additionalContent: (@Composable ColumnScope.() -> Unit)? = null
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(space = Spacing.small)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(space = Spacing.small),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                modifier = Modifier.size(size = 16.dp),
                painter = painterResource(id = iconResId),
                contentDescription = null,
                tint = titleColor
            )

            Column(
                modifier = Modifier.weight(weight = 1f),
                verticalArrangement = Arrangement.spacedBy(space = Spacing.extraSmall)
            ) {
                Text.Body2Medium(
                    text = title,
                    color = titleColor
                )

                val subtitleText = subtitle ?: subtitleResId?.let { id -> stringResource(id = id) }
                subtitleText?.let { text ->
                    Text.Body3Regular(
                        text = text,
                        color = subtitleColor
                    )
                }
            }

            trailingAction?.invoke(this)
        }

        additionalContent?.let { content ->
            Column(
                modifier = Modifier.padding(start = Spacing.mediumLarge),
                content = content
            )
        }
    }
}
