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

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.captionNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Radius
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.features.secure.links.R
import me.proton.core.presentation.R as CoreR

@Composable
internal fun SecureLinksOverviewWidgetInfo(
    modifier: Modifier = Modifier,
    @DrawableRes iconResId: Int,
    @StringRes titleResId: Int,
    infoText: String
) {
    Row(
        modifier = modifier
            .background(
                color = PassTheme.colors.interactionNormMinor1,
                shape = RoundedCornerShape(size = Radius.small)
            )
            .fillMaxWidth()
            .padding(all = Spacing.medium),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = Spacing.mediumSmall)
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = null,
            tint = PassTheme.colors.interactionNormMajor2
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(space = Spacing.extraSmall)
        ) {
            Text(
                text = stringResource(id = titleResId),
                style = ProtonTheme.typography.captionNorm,
                color = PassTheme.colors.textWeak
            )

            Text(
                text = infoText,
                style = ProtonTheme.typography.body1Medium,
                color = PassTheme.colors.textNorm
            )
        }
    }
}

@[Preview Composable]
internal fun SecureLinksOverviewInfoWidgetPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            SecureLinksOverviewWidgetInfo(
                iconResId = CoreR.drawable.ic_proton_clock,
                titleResId = R.string.secure_links_overview_widget_expiration_title,
                infoText = "14 days"
            )
        }
    }
}
