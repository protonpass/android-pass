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

package proton.android.pass.features.upsell.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.container.RoundedCornersColumn

@Composable
internal fun UpsellFeatures(modifier: Modifier = Modifier, features: ImmutableList<Pair<Int, Int>>) {
    RoundedCornersColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.medium),
        verticalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        Spacer(modifier = Modifier.height(height = Spacing.small))

        features.forEach { (iconResId, textResId) ->
            UpsellFeatureRow(
                iconResId = iconResId,
                textResId = textResId
            )
        }

        Spacer(modifier = Modifier.height(height = Spacing.small))
    }
}
