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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.textInverted
import me.proton.core.compose.theme.textNorm
import me.proton.core.compose.theme.textWeak

object PassTypography {
    private val heroRegular: TextStyle
        @Composable get() = TextStyle(
            fontSize = 28.sp,
            fontWeight = FontWeight.W700,
            letterSpacing = 0.01.em,
            lineHeight = 34.sp
        )

    val hero: TextStyle
        @Composable get() = hero()

    val heroWeak: TextStyle
        @Composable get() = heroWeak()

    private val body1Base: TextStyle
        @Composable get() = TextStyle(
            fontSize = 16.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight(400),
            textAlign = TextAlign.Center,
            letterSpacing = 0.5.sp,
        )

    private val body3Base: TextStyle
        @Composable get() = TextStyle(
            fontSize = 14.sp,
            fontWeight = FontWeight.W400,
            letterSpacing = 0.02.em,
            lineHeight = 20.sp
        )

    val body1Regular: TextStyle
        @Composable get() = body1Regular()

    val body3Regular: TextStyle
        @Composable get() = body3Regular()

    val body3RegularWeak: TextStyle
        @Composable get() = body3RegularWeak()

    val body3RegularInverted: TextStyle
        @Composable get() = body3RegularInverted()

    val body3Medium: TextStyle
        @Composable get() = body3Medium()

    val body3Bold: TextStyle
        @Composable get() = body3Bold()

    @Composable
    fun hero(enabled: Boolean = true): TextStyle =
        heroRegular.copy(color = ProtonTheme.colors.textNorm(enabled))

    @Composable
    fun heroWeak(enabled: Boolean = true): TextStyle =
        heroRegular.copy(color = ProtonTheme.colors.textWeak(enabled))

    @Composable
    fun body1Regular(enabled: Boolean = true): TextStyle = body1Base
        .copy(color = ProtonTheme.colors.textNorm(enabled))

    @Composable
    fun body3Regular(enabled: Boolean = true): TextStyle = body3Base
        .copy(color = ProtonTheme.colors.textNorm(enabled))

    @Composable
    fun body3RegularWeak(enabled: Boolean = true): TextStyle = body3Base
        .copy(color = ProtonTheme.colors.textWeak(enabled))

    @Composable
    fun body3RegularInverted(enabled: Boolean = true): TextStyle = body3Base
        .copy(color = ProtonTheme.colors.textInverted(enabled))

    @Composable
    fun body3Medium(enabled: Boolean = true): TextStyle =
        body3Base.copy(fontWeight = FontWeight.W500, color = ProtonTheme.colors.textNorm(enabled))

    @Composable
    fun body3Bold(enabled: Boolean = true): TextStyle =
        body3Base.copy(fontWeight = FontWeight.W500, color = ProtonTheme.colors.textNorm(enabled))

}
