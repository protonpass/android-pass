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

package proton.android.pass.featureitemcreate.impl.attachments.banner

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.Gradients
import proton.android.pass.commonui.api.PassPalette
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.icon.Icon
import proton.android.pass.composecomponents.impl.image.Image
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.featureitemcreate.impl.R
import me.proton.core.presentation.R as CoreR

@Composable
fun AttachmentBanner(modifier: Modifier = Modifier, onClose: () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(PassTheme.shapes.containerInputShape)
            .border(
                width = 2.dp,
                color = PassPalette.White10,
                shape = PassTheme.shapes.containerInputShape
            )
            .padding(2.dp)
            .clip(PassTheme.shapes.containerInputShape)
            .background(Gradients.RadialPurple)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(
                modifier = Modifier
                    .padding(Spacing.medium)
                    .weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(Spacing.extraSmall)
            ) {
                Text.Body1Bold(
                    text = stringResource(R.string.banner_title_attachments),
                    color = Color.White
                )
                Text.Body3Regular(
                    text = stringResource(R.string.banner_body_attachments),
                    color = Color.White
                )
            }
            Image.Default(R.drawable.attachments_file)
        }
        IconButton(
            modifier = Modifier.align(Alignment.TopEnd),
            onClick = onClose
        ) {
            Icon.Default(
                id = CoreR.drawable.ic_proton_cross_circle_filled,
                tint = PassPalette.StormyNight70
            )
        }
    }
}

@Preview
@Composable
fun AttachmentBannerPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            AttachmentBanner(
                onClose = {}
            )
        }
    }
}
