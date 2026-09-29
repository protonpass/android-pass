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

package proton.android.pass.composecomponents.impl.item.icon

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import proton.android.pass.common.api.DecodedItemIcon
import proton.android.pass.common.api.ItemIcon
import proton.android.pass.commonui.api.PassTheme
import java.nio.ByteBuffer

/**
 * Renders a custom item icon (see [ItemIcon]). The icon is validated and decoded locally, so no
 * network request is ever made for it. Renders [fallback] if the icon is invalid or cannot be
 * decoded as an image.
 */
@Composable
fun ItemCustomIcon(
    modifier: Modifier = Modifier,
    icon: String?,
    size: Int = 40,
    shape: Shape = PassTheme.shapes.squircleMediumShape,
    enabled: Boolean = true,
    fallback: @Composable () -> Unit
) {
    val decodedIcon = remember(icon) { ItemIcon.decode(icon) }

    if (decodedIcon == null) {
        fallback()
    } else {
        ItemCustomIcon(
            modifier = modifier,
            decodedIcon = decodedIcon,
            size = size,
            shape = shape,
            enabled = enabled,
            fallback = fallback
        )
    }
}

@Composable
private fun ItemCustomIcon(
    modifier: Modifier = Modifier,
    decodedIcon: DecodedItemIcon,
    size: Int = 40,
    shape: Shape = PassTheme.shapes.squircleMediumShape,
    enabled: Boolean = true,
    fallback: @Composable () -> Unit
) {
    val context = LocalContext.current
    val sizePx = with(LocalDensity.current) { size.dp.roundToPx() }
    val request = remember(decodedIcon, sizePx) {
        ImageRequest.Builder(context)
            // A fresh read-only buffer per request: never a URL, so nothing is fetched remotely
            .data(ByteBuffer.wrap(decodedIcon.bytes).asReadOnlyBuffer())
            .size(sizePx)
            .build()
    }

    Box(modifier = modifier.size(size.dp)) {
        SubcomposeAsyncImage(
            modifier = Modifier
                .clip(shape)
                .size(size.dp),
            model = request,
            contentScale = ContentScale.Crop,
            loading = {
                Box(
                    modifier = Modifier
                        .size(size.dp)
                        .background(PassTheme.colors.loginInteractionNormMinor2, shape)
                )
            },
            error = { fallback() },
            success = {
                SubcomposeAsyncImageContent(
                    modifier = Modifier
                        .size(size.dp)
                        .border(
                            width = 1.dp,
                            color = PassTheme.colors.loginIconBorder,
                            shape = shape
                        )
                        .background(Color.White)
                )
            },
            contentDescription = null
        )

        if (!enabled) {
            Box(
                modifier = Modifier
                    .size(size.dp)
                    .background(
                        color = PassTheme.colors.loginIconDisabledMask,
                        shape = shape
                    )
            )
        }
    }
}
