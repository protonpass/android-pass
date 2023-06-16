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

package proton.android.pass.featurepassword.impl.bottomsheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Surface
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultSmallNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider

@Composable
fun GeneratePasswordToggleRow(
    modifier: Modifier = Modifier,
    text: String,
    value: Boolean,
    onChange: (Boolean) -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            color = PassTheme.colors.textNorm,
            style = ProtonTheme.typography.defaultSmallNorm
        )
        Switch(
            checked = value,
            colors = SwitchDefaults.colors(
                checkedThumbColor = PassTheme.colors.loginInteractionNormMajor1,
            ),
            onCheckedChange = onChange
        )
    }
}

@Preview
@Composable
fun GeneratePasswordToggleRowPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    PassTheme(isDark = input.first) {
        Surface {
            GeneratePasswordToggleRow(
                text = "Some preference",
                value = input.second,
                onChange = {}
            )
        }
    }
}
