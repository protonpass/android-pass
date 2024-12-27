/*
 * Copyright (c) 2023-2024 Proton AG
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

package proton.android.pass.features.featureflags

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import proton.android.pass.preferences.FeatureFlag

@Composable
fun BoolFeatureFlagItem(
    modifier: Modifier = Modifier,
    featureFlag: FeatureFlag,
    value: Boolean,
    onToggle: (FeatureFlag, Boolean) -> Unit
) {
    Row(
        modifier = modifier
            .clickable {
                onToggle(featureFlag, !value)
            }
            .padding(16.dp)
    ) {
        Column(modifier.weight(1f)) {
            Text(text = featureFlag.title)
            Text(text = featureFlag.description)
        }
        Switch(
            checked = value,
            onCheckedChange = { onToggle(featureFlag, it) }
        )
    }
}
