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
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Shapes
import androidx.compose.material.Typography
import androidx.compose.runtime.Composable

@Composable
fun ProtonTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    darkColors: Colors = ProtonPalette.Dark.Colors,
    lightColors: Colors = ProtonPalette.Light.Colors,
    typography: Typography = ProtonTypography.Default,
    shapes: Shapes = ProtonShape.Default,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = if (darkTheme) darkColors else lightColors,
        typography = typography,
        shapes = shapes,
        content = content
    )
}

@Composable
fun ProtonAlertDialogTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = ProtonPalette.Light.AlertDialogColors,
        typography = ProtonTypography.AlertDialog,
        content = content
    )
}

@Composable
fun ProtonBottomNavigationTheme(
    isSelected: Boolean = false,
    content: @Composable () -> Unit
) {
    ProtonTheme(
        darkColors = ProtonPalette.Dark.BottomNavigationColors,
        lightColors = ProtonPalette.Light.BottomNavigationColors,
        typography = ProtonTypography.BottomNavigation(isSelected),
        content = content
    )
}

@Composable
fun ProtonNavigationDrawerTheme(
    content: @Composable () -> Unit
) {
    ProtonTheme(
        darkTheme = true,
        darkColors = ProtonPalette.Dark.NavigationDrawerColors,
        typography = ProtonTypography.NavigationDrawerTypography,
        content = content
    )
}

@Composable
fun ProtonTopAppBarTheme(
    content: @Composable () -> Unit
) {
    ProtonTheme(
        darkColors = ProtonPalette.Dark.TopAppBarColors,
        lightColors = ProtonPalette.Light.TopAppBarColors,
        content = content
    )
}

@Composable
fun ProtonListItemTheme(
    content: @Composable () -> Unit
) {
    ProtonTheme(
        darkColors = ProtonPalette.Dark.ListItem,
        lightColors = ProtonPalette.Light.ListItem,
        content = content
    )
}

@Composable
fun ProtonSnackbarTheme(
    content: @Composable () -> Unit
) {
    ProtonTheme(
        darkTheme = false,
        lightColors = ProtonPalette.Light.SnackbarColors,
        content = content
    )
}

@Composable
fun ProtonModalBottomSheetLayoutTheme(
    content: @Composable () -> Unit
) {
    ProtonTheme(
        darkColors = ProtonPalette.Dark.ModalBottomSheetLayoutColors,
        lightColors = ProtonPalette.Light.ModalBottomSheetLayoutColors,
        shapes = ProtonShape.ModalBottomSheetLayoutShapes,
        content = content
    )
}

@Composable
fun ProtonLinearProgressTheme(
    content: @Composable () -> Unit
) {
    ProtonTheme(
        darkColors = ProtonPalette.Dark.LinearProgressIndicatorColors,
        lightColors = ProtonPalette.Light.LinearProgressIndicatorColors,
        content = content
    )
}
