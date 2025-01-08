/*
 * Copyright (c) 2023-2024 Proton AG
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

package proton.android.pass.features.password.dialog.separator

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.some
import proton.android.pass.commonrust.api.passwords.PasswordConfig
import proton.android.pass.commonrust.api.passwords.PasswordWordSeparator

internal sealed interface WordSeparatorUiEvent {

    data object Idle : WordSeparatorUiEvent

    data object Close : WordSeparatorUiEvent

}

@Immutable
internal data class WordSeparatorUiState(
    internal val configOption: Option<PasswordConfig.Memorable>,
    internal val event: WordSeparatorUiEvent
) {

    private val areNumbersAllowed: Boolean = when (configOption) {
        None -> false
        is Some -> configOption.value.canWordsContainNumbers
    }

    internal val options: PersistentList<PasswordWordSeparator> = PasswordWordSeparator.entries
        .filter { passwordWordSeparator ->
            if (areNumbersAllowed) true
            else when (passwordWordSeparator) {
                PasswordWordSeparator.Comma,
                PasswordWordSeparator.Hyphen,
                PasswordWordSeparator.Period,
                PasswordWordSeparator.Space,
                PasswordWordSeparator.Underscore -> true

                PasswordWordSeparator.Numbers,
                PasswordWordSeparator.NumbersAndSymbols -> false
            }
        }
        .toPersistentList()

    internal val selected: Option<PasswordWordSeparator> = when (configOption) {
        None -> None
        is Some -> configOption.value.wordSeparator.some()
    }

    internal companion object {

        internal val Initial = WordSeparatorUiState(
            configOption = None,
            event = WordSeparatorUiEvent.Idle
        )

    }

}
