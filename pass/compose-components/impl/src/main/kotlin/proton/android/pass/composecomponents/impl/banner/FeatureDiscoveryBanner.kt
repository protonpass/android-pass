/*
 * Copyright (c) 2025 Proton AG
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

package proton.android.pass.composecomponents.impl.banner

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.Gradients
import proton.android.pass.commonui.api.PassPalette
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.text.Text

@Composable
fun FeatureDiscoveryBanner(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
    closeIcon: @Composable () -> Unit,
    onClose: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 2.dp,
                color = PassPalette.White10,
                shape = PassTheme.shapes.containerInputShape
            )
            .background(Gradients.RadialPurple, PassTheme.shapes.containerInputShape)
    ) {
        content()
        IconButton(
            modifier = Modifier.align(Alignment.TopEnd),
            onClick = onClose
        ) {
            closeIcon()
        }
    }
}

@Composable
fun FeatureDiscoveryText(
    modifier: Modifier = Modifier,
    title: String,
    body: String
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Spacing.extraSmall)
    ) {
        Text.Body1Bold(
            text = title,
            color = PassPalette.MistyGray
        )
        Text.Body3Regular(
            text = body,
            color = PassPalette.MistyGray
        )
    }
}
