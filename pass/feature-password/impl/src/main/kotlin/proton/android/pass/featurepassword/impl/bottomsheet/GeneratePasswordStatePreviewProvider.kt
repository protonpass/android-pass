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

package proton.android.pass.featurepassword.impl.bottomsheet

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

class GeneratePasswordStatePreviewProvider : PreviewParameterProvider<GeneratePasswordUiState> {
    override val values: Sequence<GeneratePasswordUiState>
        get() = sequenceOf(
            GeneratePasswordUiState(
                password = "a1b!c_d3e#fg",
                content = GeneratePasswordContent.RandomPassword(
                    length = 12,
                    hasSpecialCharacters = true,
                    hasCapitalLetters = false,
                    includeNumbers = true
                ),
                mode = GeneratePasswordMode.CopyAndClose
            ),
            GeneratePasswordUiState(
                password = "a1!2",
                content = GeneratePasswordContent.RandomPassword(
                    length = 4,
                    hasSpecialCharacters = false,
                    hasCapitalLetters = false,
                    includeNumbers = true
                ),
                mode = GeneratePasswordMode.CopyAndClose
            ),
            GeneratePasswordUiState(
                password = buildString { repeat(64) { append("a") } },
                content = GeneratePasswordContent.RandomPassword(
                    length = 64,
                    hasSpecialCharacters = false,
                    hasCapitalLetters = true,
                    includeNumbers = false
                ),
                mode = GeneratePasswordMode.CancelConfirm
            )
        )
}
