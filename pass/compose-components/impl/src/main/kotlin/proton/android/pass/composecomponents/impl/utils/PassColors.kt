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

package proton.android.pass.composecomponents.impl.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.domain.items.ItemCategory

@Composable
fun passItemColors(itemCategory: ItemCategory): PassItemColors = when (itemCategory) {
    ItemCategory.Unknown -> PassItemColors(
        norm = PassTheme.colors.interactionNorm,
        majorPrimary = PassTheme.colors.interactionNormMajor1,
        majorSecondary = PassTheme.colors.interactionNormMajor2,
        minorPrimary = PassTheme.colors.interactionNormMinor1,
        minorSecondary = PassTheme.colors.interactionNormMinor2
    )

    ItemCategory.Login -> PassItemColors(
        norm = PassTheme.colors.loginInteractionNorm,
        majorPrimary = PassTheme.colors.loginInteractionNormMajor1,
        majorSecondary = PassTheme.colors.loginInteractionNormMajor2,
        minorPrimary = PassTheme.colors.loginInteractionNormMinor1,
        minorSecondary = PassTheme.colors.loginInteractionNormMinor2
    )

    ItemCategory.Alias -> PassItemColors(
        norm = PassTheme.colors.aliasInteractionNorm,
        majorPrimary = PassTheme.colors.aliasInteractionNormMajor1,
        majorSecondary = PassTheme.colors.aliasInteractionNormMajor2,
        minorPrimary = PassTheme.colors.aliasInteractionNormMinor1,
        minorSecondary = PassTheme.colors.aliasInteractionNormMinor2
    )

    ItemCategory.Note -> PassItemColors(
        norm = PassTheme.colors.noteInteractionNorm,
        majorPrimary = PassTheme.colors.noteInteractionNormMajor1,
        majorSecondary = PassTheme.colors.noteInteractionNormMajor2,
        minorPrimary = PassTheme.colors.noteInteractionNormMinor1,
        minorSecondary = PassTheme.colors.noteInteractionNormMinor2
    )

    ItemCategory.Password -> PassItemColors(
        norm = PassTheme.colors.passwordInteractionNorm,
        majorPrimary = PassTheme.colors.passwordInteractionNormMajor1,
        majorSecondary = PassTheme.colors.passwordInteractionNormMajor2,
        minorPrimary = PassTheme.colors.passwordInteractionNormMinor1,
        minorSecondary = PassTheme.colors.passwordInteractionNormMinor2
    )

    ItemCategory.CreditCard -> PassItemColors(
        norm = PassTheme.colors.cardInteractionNorm,
        majorPrimary = PassTheme.colors.cardInteractionNormMajor1,
        majorSecondary = PassTheme.colors.cardInteractionNormMajor2,
        minorPrimary = PassTheme.colors.cardInteractionNormMinor1,
        minorSecondary = PassTheme.colors.cardInteractionNormMinor2
    )

    ItemCategory.Identity -> PassItemColors(
        norm = PassTheme.colors.interactionNorm,
        majorPrimary = PassTheme.colors.interactionNormMajor1,
        majorSecondary = PassTheme.colors.interactionNormMajor2,
        minorPrimary = PassTheme.colors.interactionNormMinor1,
        minorSecondary = PassTheme.colors.interactionNormMinor2
    )

    ItemCategory.WifiNetwork,
    ItemCategory.SSHKey,
    ItemCategory.Custom -> PassItemColors(
        norm = PassTheme.colors.interactionNorm,
        majorPrimary = PassTheme.colors.interactionNormMajor1,
        majorSecondary = PassTheme.colors.interactionNormMajor2,
        minorPrimary = PassTheme.colors.interactionNormMinor1,
        minorSecondary = PassTheme.colors.interactionNormMinor2
    )
}

data class PassItemColors(
    val norm: Color,
    val majorPrimary: Color,
    val majorSecondary: Color,
    val minorPrimary: Color,
    val minorSecondary: Color
)
