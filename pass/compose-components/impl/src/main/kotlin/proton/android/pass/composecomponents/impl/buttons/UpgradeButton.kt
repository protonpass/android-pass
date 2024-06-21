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

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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

@Composable
fun UpgradeButton(
    modifier: Modifier = Modifier,
    backgroundColor: Color = PassTheme.colors.interactionNormMinor2,
    contentColor: Color = PassTheme.colors.interactionNormMajor2,
    onUpgradeClick: () -> Unit
) {
    CircleButton(
        modifier = modifier,
        contentPadding = PaddingValues(
            horizontal = Spacing.medium,
            vertical = 10.dp
        ),
        color = backgroundColor,
        onClick = onUpgradeClick
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

@Preview
@Composable
internal fun UpgradeButtonPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            UpgradeButton {}
        }
    }
}
