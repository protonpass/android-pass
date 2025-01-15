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

package proton.android.pass.composecomponents.impl.container

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.commonui.api.body3Weak
import me.proton.core.presentation.R as CoreR

@Composable
fun PassInfoWarningBanner(
    modifier: Modifier = Modifier,
    text: String,
    backgroundColor: Color = PassTheme.colors.backgroundStrong
) {
    Row(
        modifier = modifier
            .background(
                color = backgroundColor,
                shape = PassTheme.shapes.squircleMediumShape
            )
            .padding(
                horizontal = Spacing.medium,
                vertical = Spacing.mediumSmall
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.small)
    ) {
        Icon(
            painter = painterResource(id = CoreR.drawable.ic_proton_info_circle_filled),
            contentDescription = null,
            tint = PassTheme.colors.textWeak
        )

        Text(
            text = text,
            style = PassTheme.typography.body3Weak(),
            color = PassTheme.colors.textWeak
        )
    }
}

@[Preview Composable]
internal fun PassInfoWarningBannerPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            PassInfoWarningBanner(text = "This is an informative warning banner")
        }
    }
}
