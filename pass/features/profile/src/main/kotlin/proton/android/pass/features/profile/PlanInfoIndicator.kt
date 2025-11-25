/*
 * Copyright (c) 2023-2024 Proton AG
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

package proton.android.pass.features.profile

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
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
import proton.android.pass.commonui.api.body3Norm
import proton.android.pass.composecomponents.impl.R as CompR

@Composable
internal fun PlanInfoIndicator(modifier: Modifier = Modifier, planInfo: PlanInfo) {
    val resources = when (planInfo) {
        PlanInfo.Hide -> null

        is PlanInfo.Unlimited -> {
            PlanResources(
                icon = CompR.drawable.account_unlimited_indicator,
                color = PassTheme.colors.noteInteractionNorm,
                text = planInfo.planName
            )
        }
    }

    if (resources != null) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(Spacing.extraSmall),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(resources.icon),
                contentDescription = resources.text,
                tint = Color.Unspecified
            )

            Text(
                text = resources.text,
                style = PassTheme.typography.body3Norm(),
                color = resources.color
            )
        }
    }
}

private data class PlanResources(
    @DrawableRes val icon: Int,
    val color: Color,
    val text: String
)

@Preview
@Composable
internal fun PlanInfoIndicatorPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            PlanInfoIndicator(planInfo = PlanInfo.Unlimited(planName = "Example plan"))
        }
    }
}
