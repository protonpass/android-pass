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

import androidx.compose.runtime.Immutable

@Immutable
enum class GeneratePasswordMode {
    CopyAndClose,
    CancelConfirm;
}

@Immutable
data class GeneratePasswordUiState(
    val password: String,
    val mode: GeneratePasswordMode,
    val content: GeneratePasswordContent
)

sealed interface GeneratePasswordContent {
    @Immutable
    data class RandomPassword(
        val length: Int,
        val hasSpecialCharacters: Boolean,
        val hasCapitalLetters: Boolean,
        val includeNumbers: Boolean
    ) : GeneratePasswordContent

    @Immutable
    data class WordsPassword(
        val count: Int,
        val wordSeparator: proton.android.pass.commonrust.api.WordSeparator,
        val capitalise: Boolean,
        val includeNumbers: Boolean
    ) : GeneratePasswordContent

}
