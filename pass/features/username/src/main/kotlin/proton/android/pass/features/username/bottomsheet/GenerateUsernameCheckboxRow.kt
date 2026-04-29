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

package proton.android.pass.features.username.bottomsheet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.Surface
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
internal fun GenerateUsernameCheckboxRow(
    modifier: Modifier = Modifier,
    text: String,
    value: Boolean,
    isEnabled: Boolean,
    onChange: (Boolean) -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = isEnabled) { onChange(!value) },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            color = PassTheme.colors.textNorm,
            style = ProtonTheme.typography.defaultSmallNorm
        )

        Checkbox(
            checked = value,
            enabled = isEnabled,
            colors = CheckboxDefaults.colors(
                checkedColor = PassTheme.colors.loginInteractionNormMajor1,
                checkmarkColor = PassTheme.colors.textInvert
            ),
            onCheckedChange = onChange
        )
    }
}

@[Preview Composable]
internal fun GenerateUsernameCheckboxRowPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    val (isDark, isChecked) = input

    PassTheme(isDark = isDark) {
        Surface {
            GenerateUsernameCheckboxRow(
                text = "Adjectives",
                value = isChecked,
                isEnabled = true,
                onChange = {}
            )
        }
    }
}
