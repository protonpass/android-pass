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

package proton.android.pass.composecomponents.impl.buttons

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ButtonElevation
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.commonui.api.body3Norm
import proton.android.pass.composecomponents.impl.R
import proton.android.pass.composecomponents.impl.icon.Icon
import proton.android.pass.composecomponents.impl.text.Text

@Composable
fun UpgradeButton(
    modifier: Modifier = Modifier,
    onUpgradeClick: () -> Unit,
    backgroundColor: Color = PassTheme.colors.interactionNormMinor2,
    contentColor: Color = PassTheme.colors.interactionNormMajor2,
    elevation: ButtonElevation = ButtonDefaults.elevation()
) {
    CircleButton(
        modifier = modifier,
        contentPadding = PaddingValues(
            horizontal = Spacing.medium,
            vertical = Spacing.mediumSmall
        ),
        color = backgroundColor,
        onClick = onUpgradeClick,
        elevation = elevation
    ) {
        Icon(
            modifier = Modifier.size(size = Spacing.medium),
            painter = painterResource(R.drawable.ic_brand_pass),
            contentDescription = null,
            tint = contentColor
        )

        Spacer(modifier = Modifier.width(width = Spacing.small))

        Text(
            text = stringResource(R.string.upgrade),
            style = PassTheme.typography.body3Norm(),
            color = contentColor
        )
    }
}

@Composable
fun UpgradeIcon(
    modifier: Modifier = Modifier,
    onUpgradeClick: () -> Unit,
    tint: Color = PassTheme.colors.interactionNormMajor2
) {
    Box(
        modifier = modifier
            .border(
                width = 1.dp,
                color = PassTheme.colors.interactionNormMinor1,
                shape = RoundedCornerShape(size = 10.dp)
            )
            .size(44.dp)
            .clip(RoundedCornerShape(size = 10.dp))
            .clickable(onClick = onUpgradeClick)
            .padding(Spacing.small),
        contentAlignment = Alignment.Center
    ) {
        Icon.Default(
            id = R.drawable.ic_diamond,
            tint = tint
        )
    }
}

@Composable
fun LongUpgradeButton(
    modifier: Modifier = Modifier,
    onUpgradeClick: () -> Unit,
    textId: Int = R.string.upgrade_to_pass_plus,
    tint: Color = PassTheme.colors.interactionNormMajor2
) {
    Row(
        modifier = modifier
            .border(
                width = 1.dp,
                color = PassTheme.colors.interactionNormMinor1,
                shape = RoundedCornerShape(size = 10.dp)
            )
            .clip(RoundedCornerShape(size = 10.dp))
            .clickable(onClick = onUpgradeClick)
            .padding(Spacing.small),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = Spacing.medium)
    ) {
        Icon.Default(
            id = R.drawable.ic_diamond,
            tint = tint
        )

        Text.Body1Medium(
            modifier = Modifier.weight(weight = 1f),
            text = stringResource(textId)
        )

        Icon.Default(
            id = me.proton.core.presentation.R.drawable.ic_arrow_right,
            tint = tint
        )
    }
}

@Preview
@Composable
internal fun UpgradeButtonPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            UpgradeButton(
                onUpgradeClick = {}
            )
        }
    }
}

@Preview
@Composable
internal fun UpgradeIconPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            UpgradeIcon(
                onUpgradeClick = {}
            )
        }
    }
}

@Preview
@Composable
internal fun LongUpgradePreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            LongUpgradeButton(
                textId = R.string.upgrade,
                onUpgradeClick = {}
            )
        }
    }
}


