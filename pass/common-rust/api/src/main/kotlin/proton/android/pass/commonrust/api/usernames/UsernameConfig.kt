/*
 * Copyright (c) 2026 Proton AG
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

package proton.android.pass.commonrust.api.usernames

import proton.android.pass.commonrust.api.passwords.PasswordWordSeparator

data class UsernameConfig(
    private val wordCount: Int,
    val includeNumbers: Boolean,
    val capitalise: Boolean,
    val wordSeparator: PasswordWordSeparator,
    val leetspeak: Boolean,
    val wordTypes: UsernameWordTypes
) {

    val wordsCount: Int = wordCount.coerceIn(USERNAME_MIN_WORDS..USERNAME_MAX_WORDS)

    companion object {

        const val USERNAME_MIN_WORDS = 1
        const val USERNAME_MAX_WORDS = 5

        val Default = UsernameConfig(
            wordCount = 2,
            includeNumbers = true,
            capitalise = false,
            wordSeparator = PasswordWordSeparator.Hyphen,
            leetspeak = false,
            wordTypes = UsernameWordTypes.Default
        )
    }
}
