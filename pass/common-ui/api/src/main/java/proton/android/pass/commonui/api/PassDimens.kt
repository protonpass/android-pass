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

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class PassDimens(
    val bottomsheetHorizontalPadding: Dp,
    val bottomsheetVerticalPadding: Dp,
    val topBarButtonHeight: Dp
) {
    companion object {
        val Phone: PassDimens = PassDimens(
            bottomsheetHorizontalPadding = Spacing.medium,
            bottomsheetVerticalPadding = Spacing.large,
            topBarButtonHeight = 40.dp
        )
    }
}

val LocalPassDimens = staticCompositionLocalOf {
    PassDimens(
        bottomsheetHorizontalPadding = Spacing.none,
        bottomsheetVerticalPadding = Spacing.none,
        topBarButtonHeight = Spacing.none
    )
}

object Spacing {
    val none: Dp = 0.dp
    val extraSmall: Dp = 4.dp
    val small: Dp = 8.dp
    val mediumSmall: Dp = 12.dp
    val medium: Dp = 16.dp
    val mediumLarge: Dp = 24.dp
    val large: Dp = 32.dp
    val extraLarge: Dp = 64.dp
}
