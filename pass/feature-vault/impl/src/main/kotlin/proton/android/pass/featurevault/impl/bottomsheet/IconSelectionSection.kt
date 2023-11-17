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

package proton.android.pass.featurevault.impl.bottomsheet

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.extension.toResource
import proton.android.pass.domain.ShareIcon

private const val ITEMS_PER_COLUMN = 5

@Composable
fun IconSelectionSection(
    modifier: Modifier = Modifier,
    selected: ShareIcon,
    onIconSelected: (ShareIcon) -> Unit
) {
    Column(modifier = modifier.fillMaxWidth()) {
        ShareIcon.values().toList().chunked(ITEMS_PER_COLUMN).forEach { rowIcons ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                rowIcons.forEach { shareIcon ->
                    IconItem(
                        icon = shareIcon,
                        isSelected = shareIcon == selected,
                        onClick = { onIconSelected(shareIcon) }
                    )
                }
            }
        }
    }
}

@Suppress("MagicNumber")
@Composable
fun IconItem(
    modifier: Modifier = Modifier,
    icon: ShareIcon,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .size(54.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Canvas(modifier = Modifier.size(54.dp)) {
                drawCircle(color = Color(0xFF494958), style = Stroke(width = 20f))
            }
        }

        Box(
            modifier = Modifier.size(40.dp)
                .clip(CircleShape)
                .background(PassTheme.colors.loginInteractionNormMinor2),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                modifier = Modifier
                    .size(24.dp),
                painter = painterResource(icon.toResource()),
                tint = PassTheme.colors.textNorm,
                contentDescription = null
            )
        }
    }
}

@Preview
@Composable
fun IconSelectionSectionPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            IconSelectionSection(
                selected = ShareIcon.Icon3,
                onIconSelected = {}
            )
        }
    }
}
