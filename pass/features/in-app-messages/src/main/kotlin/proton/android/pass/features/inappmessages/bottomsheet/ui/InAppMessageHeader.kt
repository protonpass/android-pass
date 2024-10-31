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

package proton.android.pass.features.inappmessages.bottomsheet.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import proton.android.pass.commonui.api.PassPalette
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.icon.Icon

@Composable
fun InAppMessageHeader(
    modifier: Modifier = Modifier,
    imageUrl: String?,
    onClose: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 100.dp, max = 200.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        PassPalette.PaleApricot,
                        Color.Transparent
                    ),
                    endY = Float.POSITIVE_INFINITY / 2
                )
            )
    ) {
        Column {
            Spacer(modifier = Modifier.height(30.dp))
            imageUrl?.let {
                AsyncImage(
                    modifier = Modifier.fillMaxSize().padding(horizontal = Spacing.medium),
                    contentScale = ContentScale.Fit,
                    model = imageUrl,
                    placeholder = if (LocalInspectionMode.current) {
                        ColorPainter(Color.Red)
                    } else {
                        null
                    },
                    contentDescription = null
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            IconButton(onClick = onClose) {
                Icon.Default(
                    id = me.proton.core.presentation.R.drawable.ic_proton_cross_circle_filled,
                    tint = PassTheme.colors.textNorm
                )
            }
        }
    }
}
