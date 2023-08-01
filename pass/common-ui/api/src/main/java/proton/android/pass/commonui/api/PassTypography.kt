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

package proton.android.pass.commonui.api

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.textInverted
import me.proton.core.compose.theme.textNorm
import me.proton.core.compose.theme.textWeak

@Immutable
data class PassTypography(
    internal val heroRegular: TextStyle,
    internal val body3Regular: TextStyle,
) {
    companion object {
        val Default: PassTypography = PassTypography(
            heroRegular = TextStyle(
                fontSize = 28.sp,
                fontWeight = FontWeight.W700,
                letterSpacing = 0.01.em,
                lineHeight = 34.sp
            ),
            body3Regular = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.W400,
                letterSpacing = 0.02.em,
                lineHeight = 20.sp
            )
        )
    }
}

val LocalPassTypography: ProvidableCompositionLocal<PassTypography> = staticCompositionLocalOf {
    PassTypography.Default
}

val PassTypography.heroUnspecified: TextStyle
    @Composable get() = heroRegular

@Composable
fun PassTypography.heroNorm(enabled: Boolean = true): TextStyle =
    heroUnspecified.copy(color = ProtonTheme.colors.textNorm(enabled))

@Composable
fun PassTypography.heroWeak(enabled: Boolean = true): TextStyle =
    heroUnspecified.copy(color = ProtonTheme.colors.textWeak(enabled))

val PassTypography.body3Unspecified: TextStyle
    @Composable get() = body3Regular

@Composable
fun PassTypography.body3Norm(enabled: Boolean = true): TextStyle =
    body3Unspecified.copy(color = ProtonTheme.colors.textNorm(enabled))

@Composable
fun PassTypography.body3Weak(enabled: Boolean = true): TextStyle =
    body3Unspecified.copy(color = ProtonTheme.colors.textWeak(enabled))

@Composable
fun PassTypography.body3Inverted(enabled: Boolean = true): TextStyle =
    body3Unspecified.copy(color = ProtonTheme.colors.textInverted(enabled))

@Composable
fun PassTypography.body3Medium(enabled: Boolean = true): TextStyle =
    body3Unspecified.copy(
        fontWeight = FontWeight.W500,
        color = ProtonTheme.colors.textNorm(enabled)
    )

@Composable
fun PassTypography.body3Bold(enabled: Boolean = true): TextStyle =
    body3Unspecified.copy(
        fontWeight = FontWeight.W500,
        color = ProtonTheme.colors.textNorm(enabled)
    )
