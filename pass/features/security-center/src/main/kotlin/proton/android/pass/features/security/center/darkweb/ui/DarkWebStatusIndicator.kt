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

package proton.android.pass.features.security.center.darkweb.ui

import androidx.annotation.DrawableRes
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import proton.android.pass.composecomponents.impl.container.Circle
import proton.android.pass.features.security.center.darkweb.presentation.DarkWebStatus
import me.proton.core.presentation.R as CoreR

@Composable
internal fun DarkWebStatusIndicator(modifier: Modifier = Modifier, status: DarkWebStatus) {
    val content = status.toContent()
    Circle(
        modifier = modifier,
        backgroundColor = content.backgroundColor
    ) {
        Icon(
            painter = painterResource(content.icon),
            tint = content.iconColor,
            contentDescription = null
        )
    }
}

@Composable
private fun DarkWebStatus.toContent() = when (this) {
    DarkWebStatus.AllGood -> DarkWebStatusIndicatorContent(
        backgroundColor = PassTheme.colors.cardInteractionNormMinor2,
        iconColor = PassTheme.colors.cardInteractionNormMajor2,
        icon = CoreR.drawable.ic_proton_check_circle_full

    )
    DarkWebStatus.Warning -> DarkWebStatusIndicatorContent(
        backgroundColor = PassTheme.colors.passwordInteractionNormMinor2,
        iconColor = PassTheme.colors.passwordInteractionNormMajor2,
        icon = CoreR.drawable.ic_proton_exclamation_circle_filled
    )
    DarkWebStatus.Loading -> DarkWebStatusIndicatorContent(
        backgroundColor = PassTheme.colors.interactionNormMinor2,
        iconColor = PassTheme.colors.interactionNormMajor2,
        icon = CoreR.drawable.ic_proton_question_circle_filled
    )
}

internal data class DarkWebStatusIndicatorContent(
    val backgroundColor: Color,
    val iconColor: Color,
    @DrawableRes val icon: Int
)

@Preview
@Composable
internal fun DarkWebStatusIndicatorPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    val status = if (input.second) DarkWebStatus.AllGood else DarkWebStatus.Warning

    PassTheme(isDark = input.first) {
        Surface {
            DarkWebStatusIndicator(status = status)
        }
    }
}
