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

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import me.proton.core.compose.theme.ProtonColors
import me.proton.core.compose.theme.ProtonShapes
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.ProtonTypography
import me.proton.core.compose.theme.isNightMode

@Composable
fun PassTheme(
    isDark: Boolean = isNightMode(),
    protonColors: ProtonColors = if (isDark) ProtonColors.Dark else ProtonColors.Light,
    passColors: PassColors = if (isDark) PassColors.Dark else PassColors.Light,
    passDimens: PassDimens = PassDimens.Phone,
    passShapes: PassShapes = PassShapes.Default,
    passTypography: PassTypography = PassTypography.Default,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalPassColors provides passColors,
        LocalPassDimens provides passDimens,
        LocalPassShapes provides passShapes,
        LocalPassTypography provides passTypography
    ) {
        ProtonTheme(
            isDark = isDark,
            colors = protonColors.copy(
                brandNorm = passColors.interactionNorm,
                backgroundNorm = passColors.backgroundNorm,
                interactionNorm = passColors.interactionNorm,
            ),
            typography = ProtonTypography.Default.copy(
                hero = passTypography.heroUnspecified,
            ),
            shapes = ProtonShapes().copy(
                small = RoundedCornerShape(Radius.small + Radius.small),
                medium = RoundedCornerShape(Radius.medium + Radius.small),
                large = RoundedCornerShape(Radius.large + Radius.small),
                bottomSheet = passShapes.bottomsheetShape,
            ),
            content = content
        )
    }
}

object PassTheme {
    val colors: PassColors
        @Composable
        @ReadOnlyComposable
        get() = LocalPassColors.current
    val dimens: PassDimens
        @Composable
        @ReadOnlyComposable
        get() = LocalPassDimens.current
    val shapes: PassShapes
        @Composable
        @ReadOnlyComposable
        get() = LocalPassShapes.current
    val typography: PassTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalPassTypography.current
}
