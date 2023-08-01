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
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.commonui.api.body3Norm
import proton.android.pass.composecomponents.impl.R

@Composable
fun UpgradeButton(
    modifier: Modifier = Modifier,
    color: Color = PassTheme.colors.interactionNormMajor1,
    onUpgradeClick: () -> Unit
) {
    CircleButton(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp, 10.dp),
        color = color,
        onClick = onUpgradeClick
    ) {
        Text(
            text = stringResource(R.string.upgrade),
            style = PassTheme.typography.body3Norm(),
            color = PassTheme.colors.textInvert
        )
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            modifier = Modifier.size(16.dp),
            painter = painterResource(me.proton.core.presentation.compose.R.drawable.ic_proton_arrow_out_square),
            contentDescription = null,
            tint = PassTheme.colors.textInvert
        )
    }
}

@Preview
@Composable
fun UpgradeButtonPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            UpgradeButton {}
        }
    }
}
