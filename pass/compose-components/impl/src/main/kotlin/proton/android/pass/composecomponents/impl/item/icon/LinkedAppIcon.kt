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

package proton.android.pass.composecomponents.impl.item.icon

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import proton.android.pass.commonui.api.PassTheme
import proton.pass.domain.entity.PackageName

@Composable
fun LinkedAppIcon(
    modifier: Modifier = Modifier,
    packageName: String,
    size: Int = 40,
    shape: Shape,
    emptyContent: @Composable () -> Unit
) {
    LinkedAppIcon(
        modifier = modifier,
        packageName = PackageName(packageName),
        size = size,
        shape = shape,
        emptyContent = emptyContent
    )
}

@Composable
fun LinkedAppIcon(
    modifier: Modifier = Modifier,
    packageName: PackageName,
    size: Int = 40,
    shape: Shape = PassTheme.shapes.squircleMediumShape,
    emptyContent: @Composable () -> Unit
) {
    var isLoaded by remember { mutableStateOf(false) }

    val backgroundColor by if (CROSSFADE_ENABLED) {
        animateColorAsState(
            targetValue = if (isLoaded) {
                Color.White
            } else PassTheme.colors.loginInteractionNormMinor2,
            animationSpec = tween(
                durationMillis = CROSSFADE_ANIMATION_MS
            )
        )
    } else {
        remember { mutableStateOf(Color.White) }
    }

    SubcomposeAsyncImage(
        modifier = modifier
            .clip(shape)
            .size(size.dp),
        model = ImageRequest.Builder(LocalContext.current)
            .data(packageName)
            .size(size)
            .apply {
                if (CROSSFADE_ENABLED) {
                    crossfade(CROSSFADE_ANIMATION_MS)
                }
            }
            .build(),
        loading = {
            emptyContent()
        },
        error = {
            emptyContent()
        },
        onSuccess = {
            isLoaded = true
        },
        success = {
            SubcomposeAsyncImageContent(
                modifier = Modifier
                    .size(size.dp)
                    .border(
                        width = 1.dp,
                        color = PassTheme.colors.loginIconBorder,
                        shape = shape
                    )
                    .background(backgroundColor)
                    .padding(8.dp)
            )
        },
        contentDescription = null
    )
}
