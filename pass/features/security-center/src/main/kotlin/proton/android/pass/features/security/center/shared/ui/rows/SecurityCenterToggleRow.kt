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

package proton.android.pass.features.security.center.shared.ui.rows

import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import proton.android.pass.commonui.api.PassTheme

@Composable
internal fun SecurityCenterToggleRow(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onClick: () -> Unit
) {
    SecurityCenterRow(
        modifier = modifier,
        title = title,
        subtitle = subtitle,
        isClickable = false,
        trailingContent = {
            Switch(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { onClick() },
                checked = isChecked,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = PassTheme.colors.interactionNormMajor1
                ),
                onCheckedChange = null
            )
        }
    )
}
