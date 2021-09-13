/*
 * Copyright (c) 2021 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonCore.
 *
 * ProtonCore is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ProtonCore is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ProtonCore.  If not, see <https://www.gnu.org/licenses/>.
 */
package me.proton.core.compose.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

object ProtonTypography {
    val Default: Typography
        @Composable
        get() = Typography(
            defaultFontFamily = FontFamily.SansSerif,
            h6 = MaterialTheme.typography.headline,
            subtitle1 = MaterialTheme.typography.body1Medium,
            subtitle2 = MaterialTheme.typography.body2Medium,
            body1 = MaterialTheme.typography.body1Regular,
            body2 = MaterialTheme.typography.body2Regular,
            button = MaterialTheme.typography.body1Regular,
            caption = MaterialTheme.typography.captionMedium,
            overline = MaterialTheme.typography.overlineMedium
        )

    val AlertDialog: Typography
        @Composable
        get() = Default.copy(
            subtitle1 = MaterialTheme.typography.headline(true, false), // title
            body2 = MaterialTheme.typography.default(true, false), // text
            button = MaterialTheme.typography.body1Medium // button
        )

    val NavigationDrawerTypography: Typography
        @Composable
        get() = Default.copy(
            h1 = MaterialTheme.typography.defaultWeak()
        )

    @Composable
    fun BottomNavigation(isSelected: Boolean) =
        Default.copy(
            caption = if (isSelected) MaterialTheme.typography.overlineMedium else MaterialTheme.typography.overlineRegular
        )
}

val Typography.headline: TextStyle
    @Composable
    get() = TextStyle(
        fontSize = 20.sp,
        fontWeight = FontWeight.W700,
        letterSpacing = 0.01.em,
        lineHeight = 24.sp
    )

val Typography.body1Regular: TextStyle
    @Composable
    get() = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.W400,
        letterSpacing = 0.03.em,
        lineHeight = 24.sp
    )

val Typography.body1Bold: TextStyle
    @Composable
    get() = body1Regular.copy(
        fontWeight = FontWeight.W700
    )

val Typography.body1Medium: TextStyle
    @Composable
    get() = body1Regular.copy(
        fontWeight = FontWeight.W500
    )

val Typography.body2Regular: TextStyle
    @Composable
    get() = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.W400,
        letterSpacing = 0.02.em,
        lineHeight = 20.sp
    )

val Typography.body2Medium: TextStyle
    @Composable
    get() = body2Regular.copy(
        fontWeight = FontWeight.W500
    )

val Typography.captionRegular: TextStyle
    @Composable
    get() = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.W400,
        letterSpacing = 0.03.em,
        lineHeight = 16.sp
    )

val Typography.captionMedium: TextStyle
    @Composable
    get() = captionRegular.copy(
        fontWeight = FontWeight.W500,
    )

val Typography.overlineRegular: TextStyle
    @Composable
    get() = TextStyle(
        fontSize = 10.sp,
        fontWeight = FontWeight.W400,
        letterSpacing = 0.04.em,
        lineHeight = 16.sp
    )

val Typography.overlineMedium: TextStyle
    @Composable
    get() = overlineRegular.copy(
        fontWeight = FontWeight.W500
    )

@Composable
fun Typography.headline(enabled: Boolean = true, darkTheme: Boolean = isSystemInDarkTheme()) =
    headline.copy(
        color = MaterialTheme.colors.textNorm(enabled, darkTheme)
    )

@Composable
fun Typography.headlineHint(darkTheme: Boolean = isSystemInDarkTheme()) =
    headline.copy(
        color = MaterialTheme.colors.textHint(darkTheme)
    )

@Composable
fun Typography.headlineSmall(enabled: Boolean = true, darkTheme: Boolean = isSystemInDarkTheme()) =
    body1Medium.copy(
        color = MaterialTheme.colors.textNorm(enabled, darkTheme)
    )

@Composable
fun Typography.defaultHighlight(enabled: Boolean = true, darkTheme: Boolean = isSystemInDarkTheme()) =
    body1Bold.copy(
        color = MaterialTheme.colors.textNorm(enabled, darkTheme)
    )

@Composable
fun Typography.defaultStrong(enabled: Boolean = true, darkTheme: Boolean = isSystemInDarkTheme()) =
    MaterialTheme.typography.headlineSmall(enabled, darkTheme)

@Composable
fun Typography.default(enabled: Boolean = true, darkTheme: Boolean = isSystemInDarkTheme()) =
    body1Regular.copy(
        color = MaterialTheme.colors.textNorm(enabled, darkTheme)
    )

@Composable
fun Typography.defaultWeak(enabled: Boolean = true, darkTheme: Boolean = isSystemInDarkTheme()) =
    body1Regular.copy(
        color = MaterialTheme.colors.textWeak(enabled, darkTheme)
    )

@Composable
fun Typography.defaultHint(darkTheme: Boolean = isSystemInDarkTheme()) =
    body1Regular.copy(
        color = MaterialTheme.colors.textHint(darkTheme)
    )

@Composable
fun Typography.defaultInverted(darkTheme: Boolean = isSystemInDarkTheme()) =
    body1Regular.copy(
        color = MaterialTheme.colors.textInverted(darkTheme)
    )

@Composable
fun Typography.defaultSmallStrong(enabled: Boolean = true, darkTheme: Boolean = isSystemInDarkTheme()) =
    body2Medium.copy(
        color = MaterialTheme.colors.textNorm(enabled, darkTheme)
    )

@Composable
fun Typography.defaultSmall(enabled: Boolean = true, darkTheme: Boolean = isSystemInDarkTheme()) =
    body2Regular.copy(
        color = MaterialTheme.colors.textNorm(enabled, darkTheme)
    )

@Composable
fun Typography.defaultSmallWeak(enabled: Boolean = true, darkTheme: Boolean = isSystemInDarkTheme()) =
    body2Regular.copy(
        color = MaterialTheme.colors.textWeak(enabled, darkTheme)
    )

@Composable
fun Typography.defaultSmallInverted(darkTheme: Boolean = isSystemInDarkTheme()) =
    body2Regular.copy(
        color = MaterialTheme.colors.textInverted(darkTheme)
    )

@Composable
fun Typography.captionStrong(enabled: Boolean = true, darkTheme: Boolean = isSystemInDarkTheme()) =
    captionMedium.copy(
        color = MaterialTheme.colors.textNorm(enabled, darkTheme)
    )

@Composable
fun Typography.caption(enabled: Boolean = true, darkTheme: Boolean = isSystemInDarkTheme()) =
    captionRegular.copy(
        color = MaterialTheme.colors.textNorm(enabled, darkTheme)
    )

@Composable
fun Typography.captionWeak(enabled: Boolean = true, darkTheme: Boolean = isSystemInDarkTheme()) =
    captionRegular.copy(
        color = MaterialTheme.colors.textWeak(enabled, darkTheme),
    )

@Composable
fun Typography.captionHint(darkTheme: Boolean = isSystemInDarkTheme()) =
    captionRegular.copy(
        color = MaterialTheme.colors.textHint(darkTheme),
    )

@Composable
fun Typography.overline(enabled: Boolean = true, darkTheme: Boolean = isSystemInDarkTheme()) =
    overlineRegular.copy(
        color = MaterialTheme.colors.textNorm(enabled, darkTheme)
    )

@Composable
fun Typography.overlineStrong(darkTheme: Boolean = isSystemInDarkTheme()) =
    overlineMedium.copy(
        color = MaterialTheme.colors.textInverted(darkTheme)
    )
