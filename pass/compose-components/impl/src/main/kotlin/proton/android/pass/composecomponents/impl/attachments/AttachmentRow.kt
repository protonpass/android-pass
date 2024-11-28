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

package proton.android.pass.composecomponents.impl.attachments

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.commonui.api.applyIf
import proton.android.pass.composecomponents.impl.icon.Icon
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.domain.attachments.AttachmentType
import me.proton.core.presentation.R as CoreR

@Composable
fun AttachmentRow(
    modifier: Modifier = Modifier,
    filename: String,
    attachmentType: AttachmentType,
    size: String,
    isLoading: Boolean = false,
    isEnabled: Boolean = true,
    onOptionsClick: () -> Unit,
    onAttachmentClick: () -> Unit
) {
    Row(
        modifier = modifier.applyIf(
            !isLoading,
            ifTrue = { clickable(onClick = onAttachmentClick) }
        ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.medium)
    ) {
        AttachmentImage(
            attachmentType = attachmentType
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Spacing.extraSmall)
        ) {
            Text.Body1Regular(text = filename, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text.Body3Weak(size)
        }
        when {
            isLoading -> CircularProgressIndicator(modifier = Modifier.size(24.dp))
            isEnabled -> IconButton(onOptionsClick) {
                Icon.Default(
                    id = CoreR.drawable.ic_proton_three_dots_vertical,
                    tint = PassTheme.colors.textWeak
                )
            }
        }
    }
}

@Preview
@Composable
fun AttachmentRowPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            AttachmentRow(
                filename = "image.jpg",
                attachmentType = AttachmentType.RasterImage,
                size = "1.2 MB",
                onAttachmentClick = {},
                onOptionsClick = {}
            )
        }
    }
}
