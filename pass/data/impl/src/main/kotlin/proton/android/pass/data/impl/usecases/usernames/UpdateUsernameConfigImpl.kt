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

package proton.android.pass.data.impl.usecases.usernames

import proton.android.pass.commonrust.api.passwords.PasswordWordSeparator
import proton.android.pass.commonrust.api.usernames.UsernameConfig
import proton.android.pass.data.api.usecases.usernames.UpdateUsernameConfig
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.preferences.UsernameGenerationPreference
import proton.android.pass.preferences.UsernameWordTypesPreference
import proton.android.pass.preferences.WordSeparator
import javax.inject.Inject

class UpdateUsernameConfigImpl @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : UpdateUsernameConfig {

    override suspend fun invoke(config: UsernameConfig): Result<Unit> =
        userPreferencesRepository.setUsernameGenerationPreference(config.toPreference())

    private fun UsernameConfig.toPreference() = UsernameGenerationPreference(
        wordCount = wordsCount,
        includeNumbers = includeNumbers,
        capitalise = capitalise,
        wordsSeparator = wordSeparator.toPref(),
        leetspeak = leetspeak,
        wordTypes = UsernameWordTypesPreference(
            adjectives = wordTypes.adjectives,
            nouns = wordTypes.nouns,
            verbs = wordTypes.verbs
        )
    )

    private fun PasswordWordSeparator.toPref(): WordSeparator = when (this) {
        PasswordWordSeparator.Hyphen -> WordSeparator.Hyphen
        PasswordWordSeparator.Space -> WordSeparator.Space
        PasswordWordSeparator.Period -> WordSeparator.Period
        PasswordWordSeparator.Comma -> WordSeparator.Comma
        PasswordWordSeparator.Underscore -> WordSeparator.Underscore
        PasswordWordSeparator.Numbers -> WordSeparator.Numbers
        PasswordWordSeparator.NumbersAndSymbols -> WordSeparator.NumbersAndSymbols
    }
}
