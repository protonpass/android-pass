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

package proton.android.pass.features.itemcreate.totp.camera

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.container.Circle
import proton.android.pass.features.itemcreate.R

@Composable
fun CameraPreviewTopBar(onOpenImagePicker: () -> Unit, onDismiss: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(AppBarDefaults.ContentPadding)
            .height(56.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Circle(
            modifier = Modifier
                .padding(Spacing.mediumSmall, Spacing.extraSmall),
            backgroundColor = PassTheme.colors.textHint,
            onClick = onDismiss
        ) {
            Icon(
                painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_cross_small),
                contentDescription = stringResource(R.string.close_scree_icon_content_description),
                tint = PassTheme.colors.textNorm
            )
        }

        IconButton(onClick = onOpenImagePicker) {
            Icon(
                modifier = Modifier.size(28.dp),
                painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_image),
                contentDescription = stringResource(R.string.close_scree_icon_content_description),
                tint = PassTheme.colors.textNorm
            )
        }
    }
}

@Preview
@Composable
fun CameraPreviewTopBarPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            CameraPreviewTopBar(onOpenImagePicker = {}, onDismiss = {})
        }
    }
}
