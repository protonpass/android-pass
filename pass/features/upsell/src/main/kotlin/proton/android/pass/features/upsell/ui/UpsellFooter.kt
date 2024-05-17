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

package proton.android.pass.features.upsell.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.features.upsell.R
import proton.android.pass.composecomponents.impl.R as CompR

@Composable
internal fun UpsellFooter(
    modifier: Modifier = Modifier,
    submitText: String,
    onUpgradeClick: () -> Unit,
    onNotNowClick: () -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(space = Spacing.medium)
    ) {
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .fillMaxWidth()
                .background(color = PassTheme.colors.interactionNormMajor2)
                .clickable { onUpgradeClick() }
                .padding(vertical = Spacing.medium)
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = submitText,
                color = PassTheme.colors.textInvert,
                style = ProtonTheme.typography.defaultNorm,
                textAlign = TextAlign.Center
            )
        }

        Text(
            modifier = Modifier
                .clickable { onNotNowClick() }
                .padding(all = Spacing.small),
            text = stringResource(id = CompR.string.action_not_now),
            color = PassTheme.colors.interactionNormMajor2,
            style = ProtonTheme.typography.defaultNorm
        )
    }
}

@[Preview Composable]
fun UpsellFooterPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            UpsellFooter(
                submitText = stringResource(id = R.string.upsell_button_upgrade),
                onUpgradeClick = {},
                onNotNowClick = {}
            )
        }
    }
}
