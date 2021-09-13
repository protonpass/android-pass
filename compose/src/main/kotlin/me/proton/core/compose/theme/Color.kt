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
import androidx.compose.material.Colors
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

internal object ProtonColors {
    val Woodsmoke = Color(0xFF17181C)
    val Charade = Color(0xFF25272C)
    val Tuna = Color(0xFF303239)
    val Blackcurrant = Color(0XFF3B3D41)
    val Abbey = Color(0xFF494D55)
    val StormGray = Color(0xFF727680)
    val SantasGray = Color(0xFF9CA0AA)
    val FrenchGray = Color(0xFFBABDC6)
    val Mischka = Color(0xFFDADCE3)
    val AthensGray = Color(0xFFEAECF1)
    val Whisper = Color(0xFFF5F6FA)
    val Chambray = Color(0xFF3C4B88)
    val SanMarino = Color(0xFF5064B6)
    val CornflowerBlue = Color(0xFF657EE4)
    val Portage = Color(0xFF8498E9)
    val Perano = Color(0xFFA2B1EE)
    val Pomegranate = Color(0xFFE84118)
    val White = Color.White
    val Black = Color.Black
}

internal object ProtonPalette {
    object Dark {
        val Colors = darkColors(
            primary = ProtonColors.CornflowerBlue,
            primaryVariant = ProtonColors.SanMarino,
            secondary = ProtonColors.Perano,
            secondaryVariant = ProtonColors.Portage,
            background = ProtonColors.Woodsmoke,
            surface = ProtonColors.Charade,
            error = ProtonColors.Pomegranate,
            onPrimary = ProtonColors.White,
            onSecondary = ProtonColors.White,
            onBackground = ProtonColors.White,
            onSurface = ProtonColors.White,
            onError = ProtonColors.White
        )

        val BottomNavigationColors = Colors.copy(
            primary = ProtonColors.CornflowerBlue, // selected item
            surface = ProtonColors.AthensGray.copy(alpha = 0.12f), // divider
            background = ProtonColors.Woodsmoke,
            onBackground = ProtonColors.SantasGray, // unselected item
        )

        val NavigationDrawerColors = Colors.copy(
            background = ProtonColors.Woodsmoke,
            onBackground = Color.White,
            surface = ProtonColors.Blackcurrant,
            onSurface = Color.White,
        )

        val TopAppBarColors = Colors.copy(
            background = ProtonColors.Woodsmoke,
            onBackground = ProtonColors.White
        )

        val ListItem = Colors.copy(
            background = ProtonColors.Woodsmoke,
            onBackground = ProtonColors.White,
            surface = ProtonColors.AthensGray.copy(alpha = 0.12f), // divider
        )

        val ModalBottomSheetLayoutColors = Colors.copy(
            background = ProtonColors.Woodsmoke,
            onBackground = ProtonColors.White,
            surface = ProtonColors.AthensGray.copy(alpha = 0.12f), // divider
        )

        val LinearProgressIndicatorColors = Colors.copy(
            surface = ProtonColors.AthensGray.copy(alpha = 0.12f)
        )
    }

    object Light {
        val Colors = lightColors(
            primary = ProtonColors.CornflowerBlue,
            primaryVariant = ProtonColors.SanMarino,
            secondary = ProtonColors.Perano,
            secondaryVariant = ProtonColors.Portage,
            background = ProtonColors.White,
            surface = ProtonColors.Whisper,
            error = ProtonColors.Pomegranate,
            onPrimary = ProtonColors.White,
            onSecondary = ProtonColors.White,
            onBackground = ProtonColors.Woodsmoke,
            onSurface = ProtonColors.CornflowerBlue,
            onError = ProtonColors.White
        )

        val AlertDialogColors = Colors.copy(
            surface = ProtonColors.White,
            onSurface = ProtonColors.Woodsmoke
        )

        val BottomNavigationColors = Colors.copy(
            primary = ProtonColors.CornflowerBlue, // selected item
            surface = ProtonColors.AthensGray, // divider
            background = Color.White,
            onBackground = ProtonColors.StormGray, // unselected item
        )

        val ListItem = Colors.copy(
            background = ProtonColors.White,
            onBackground = ProtonColors.Woodsmoke,
            surface = ProtonColors.AthensGray // divider
        )

        val SnackbarColors = Colors.copy(
            primary = ProtonColors.Pomegranate,
            surface = ProtonColors.White,
            onSurface = Color(0xFFE21100), // TODO target ProtonColors.Pomegranate once Snackbar applies alpha 0.8f
        )

        val TopAppBarColors = Colors.copy(
            background = ProtonColors.White,
            onBackground = ProtonColors.Woodsmoke
        )

        val ModalBottomSheetLayoutColors = Colors.copy(
            background = ProtonColors.White,
            onBackground = ProtonColors.Woodsmoke,
            surface = ProtonColors.AthensGray, // divider
        )

        val LinearProgressIndicatorColors = Dark.Colors.copy(
            surface = ProtonColors.AthensGray
        )
    }
}

@Composable
fun Colors.textNorm(enabled: Boolean = true, darkTheme: Boolean = isSystemInDarkTheme()) = when {
    enabled && darkTheme -> ProtonColors.White
    enabled && !darkTheme -> ProtonColors.Woodsmoke
    !enabled && darkTheme -> ProtonColors.Abbey
    else -> ProtonColors.FrenchGray
}

@Composable
fun Colors.textHint(darkTheme: Boolean = isSystemInDarkTheme()) =
    if (darkTheme) ProtonColors.StormGray else ProtonColors.SantasGray

@Composable
fun Colors.textWeak(enabled: Boolean = true, darkTheme: Boolean = isSystemInDarkTheme()) = when {
    enabled && darkTheme -> ProtonColors.SantasGray
    enabled && !darkTheme -> ProtonColors.StormGray
    !enabled && darkTheme -> ProtonColors.Abbey
    else -> ProtonColors.FrenchGray
}

@Composable
fun Colors.textInverted(darkTheme: Boolean = isSystemInDarkTheme()) =
    if (darkTheme) ProtonColors.Woodsmoke else ProtonColors.White

val Colors.drawerScrim: Color
    @Composable
    get() = scrimColor

@Composable
fun Colors.modalBottomSheetScrim(darkTheme: Boolean = isSystemInDarkTheme()) =
    if (darkTheme) ProtonColors.Black.copy(alpha = 0.52f) else scrimColor

private val Colors.scrimColor: Color
    @Composable
    get() = ProtonColors.Woodsmoke.copy(alpha = 0.48f)

val Colors.drawerDivider: Color
    @Composable
    get() = ProtonColors.Blackcurrant

@Composable
fun Colors.actionButtonIcon(enabled: Boolean = true, darkTheme: Boolean = isSystemInDarkTheme()) = when {
    enabled && darkTheme -> ProtonColors.White
    enabled && !darkTheme -> ProtonColors.Woodsmoke
    !enabled && darkTheme -> ProtonColors.Abbey
    else -> ProtonColors.FrenchGray
}

@Composable
fun Colors.primaryActionButtonBackground(enabled: Boolean = true, darkTheme: Boolean = isSystemInDarkTheme()) = when {
    enabled && darkTheme -> ProtonColors.White
    enabled && !darkTheme -> ProtonColors.Woodsmoke
    !enabled && darkTheme -> ProtonColors.Charade
    else -> ProtonColors.Whisper
}

@Composable
fun Colors.secondaryActionButtonBackground(enabled: Boolean = true, darkTheme: Boolean = isSystemInDarkTheme()) = when {
    enabled && darkTheme -> ProtonColors.Blackcurrant
    enabled && !darkTheme -> ProtonColors.AthensGray
    !enabled && darkTheme -> ProtonColors.Charade
    else -> ProtonColors.Whisper
}
