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

package proton.android.pass.composecomponents.impl.extension

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import proton.android.pass.commonui.api.PassTheme
import proton.pass.domain.ShareColor

@Suppress("MagicNumber", "ComplexMethod")
@Composable
fun ShareColor.toColor(isBackground: Boolean = false): Color = when (this) {
    ShareColor.Color1 -> PassTheme.colors.vaultColor1.copy(alpha = if (isBackground) 0.16f else 1f)
    ShareColor.Color2 -> PassTheme.colors.vaultColor2.copy(alpha = if (isBackground) 0.16f else 1f)
    ShareColor.Color3 -> PassTheme.colors.vaultColor3.copy(alpha = if (isBackground) 0.16f else 1f)
    ShareColor.Color4 -> PassTheme.colors.vaultColor4.copy(alpha = if (isBackground) 0.16f else 1f)
    ShareColor.Color5 -> PassTheme.colors.vaultColor5.copy(alpha = if (isBackground) 0.16f else 1f)
    ShareColor.Color6 -> PassTheme.colors.vaultColor6.copy(alpha = if (isBackground) 0.16f else 1f)
    ShareColor.Color7 -> PassTheme.colors.vaultColor7.copy(alpha = if (isBackground) 0.16f else 1f)
    ShareColor.Color8 -> PassTheme.colors.vaultColor8.copy(alpha = if (isBackground) 0.16f else 1f)
    ShareColor.Color9 -> PassTheme.colors.vaultColor9.copy(alpha = if (isBackground) 0.16f else 1f)
    ShareColor.Color10 -> PassTheme.colors.vaultColor10.copy(alpha = if (isBackground) 0.16f else 1f)
}
