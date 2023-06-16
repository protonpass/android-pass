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

package proton.android.pass.composecomponents.impl.container

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.commonui.api.applyIf

@Composable
fun InfoBanner(
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    text: AnnotatedString,
    onClick: (() -> Unit)? = null
) {
    Text(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .applyIf(onClick != null, ifTrue = { clickable { onClick?.invoke() } })
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(16.dp),
        text = text,
        color = PassTheme.colors.textNorm,
    )
}

@Composable
fun InfoBanner(
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    text: String,
    onClick: (() -> Unit)? = null
) {
    InfoBanner(modifier, backgroundColor, AnnotatedString(text), onClick)
}

@Preview
@Composable
fun InfoBannerPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            InfoBanner(
                backgroundColor = PassTheme.colors.loginInteractionNormMinor1,
                text = buildAnnotatedString { append("This is an info banner") }
            )
        }
    }
}
