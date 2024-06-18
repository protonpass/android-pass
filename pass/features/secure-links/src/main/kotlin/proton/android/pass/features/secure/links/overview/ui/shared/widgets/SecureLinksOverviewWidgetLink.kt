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

package proton.android.pass.features.secure.links.overview.ui.shared.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Radius
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.commonui.api.body3Weak

@Composable
internal fun SecureLinksOverviewWidgetLink(modifier: Modifier = Modifier, secureLink: String) {
    Text(
        modifier = modifier
            .background(
                color = PassTheme.colors.interactionNormMinor1,
                shape = RoundedCornerShape(size = Radius.small)
            )
            .fillMaxWidth()
            .padding(all = Spacing.medium),
        text = secureLink,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = PassTheme.typography.body3Weak(),
        color = PassTheme.colors.textNorm
    )
}

@[Preview Composable]
internal fun SecureLinksOverviewLinkWidgetPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            SecureLinksOverviewWidgetLink(
                secureLink = "https://secure/link/PNHEXJT1SAME13XDS70YEAYJKW#zq1Ax1m"
            )
        }
    }
}
