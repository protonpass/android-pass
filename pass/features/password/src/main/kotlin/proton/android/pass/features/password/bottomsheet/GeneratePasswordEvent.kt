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

package proton.android.pass.features.password.bottomsheet

import proton.android.pass.commonrust.api.passwords.PasswordConfig
import proton.android.pass.preferences.PasswordGenerationMode
import proton.android.pass.preferences.WordSeparator

internal sealed interface GeneratePasswordEvent {
    data object OnRegeneratePasswordClick : GeneratePasswordEvent
    data object OnPasswordModeChangeClick : GeneratePasswordEvent
    data class OnPasswordModeChange(val mode: PasswordGenerationMode) : GeneratePasswordEvent

    data class OnRandomUseSpecialCharactersChange(val value: Boolean) : GeneratePasswordEvent
    data class OnRandomUseCapitalLettersChange(val value: Boolean) : GeneratePasswordEvent
    data class OnRandomIncludeNumbersChange(val value: Boolean) : GeneratePasswordEvent

    data class OnWordsCountChange(val count: Int) : GeneratePasswordEvent
    data class OnWordsCapitalizeChange(val value: Boolean) : GeneratePasswordEvent
    data object OnWordsSeparatorClick : GeneratePasswordEvent
    data class OnWordsSeparatorChange(val separator: WordSeparator) : GeneratePasswordEvent
    data class OnWordsIncludeNumbersChange(val value: Boolean) : GeneratePasswordEvent

    @JvmInline
    value class OnPasswordConfigChanged(val config: PasswordConfig) : GeneratePasswordEvent

}
