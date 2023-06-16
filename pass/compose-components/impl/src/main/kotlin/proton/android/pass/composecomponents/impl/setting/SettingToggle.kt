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

package proton.android.pass.composecomponents.impl.setting

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultWeak
import proton.android.pass.commonui.api.PassTheme

@Composable
fun SettingToggle(
    modifier: Modifier = Modifier,
    text: String,
    isChecked: Boolean,
    onClick: (Boolean) -> Unit
) {
    Row(
        modifier = modifier
            .clickable { onClick(!isChecked) }
            .fillMaxWidth()
            .padding(16.dp, 26.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = text,
            style = ProtonTheme.typography.defaultWeak,
            color = PassTheme.colors.textNorm
        )
        Switch(
            checked = isChecked,
            colors = SwitchDefaults.colors(
                checkedThumbColor = PassTheme.colors.interactionNormMajor1,
            ),
            onCheckedChange = null
        )
    }
}
