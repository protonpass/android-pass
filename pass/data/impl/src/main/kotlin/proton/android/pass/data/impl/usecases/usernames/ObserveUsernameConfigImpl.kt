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

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import proton.android.pass.commonrust.api.passwords.PasswordWordSeparator
import proton.android.pass.commonrust.api.usernames.UsernameConfig
import proton.android.pass.commonrust.api.usernames.UsernameWordTypes
import proton.android.pass.data.api.usecases.usernames.ObserveUsernameConfig
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.preferences.UsernameGenerationPreference
import proton.android.pass.preferences.UsernameWordTypesPreference
import proton.android.pass.preferences.WordSeparator
import javax.inject.Inject

class ObserveUsernameConfigImpl @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ObserveUsernameConfig {

    override fun invoke(): Flow<UsernameConfig> = userPreferencesRepository.getUsernameGenerationPreference()
        .map { it.asUsernameConfig() }

    private fun UsernameGenerationPreference.asUsernameConfig() = UsernameConfig(
        wordCount = wordCount,
        includeNumbers = includeNumbers,
        capitalise = capitalise,
        wordSeparator = wordsSeparator.toDomain(),
        leetspeak = leetspeak,
        wordTypes = wordTypes.toDomain()
    )

    private fun WordSeparator.toDomain(): PasswordWordSeparator = when (this) {
        WordSeparator.Hyphen -> PasswordWordSeparator.Hyphen
        WordSeparator.Space -> PasswordWordSeparator.Space
        WordSeparator.Period -> PasswordWordSeparator.Period
        WordSeparator.Comma -> PasswordWordSeparator.Comma
        WordSeparator.Underscore -> PasswordWordSeparator.Underscore
        WordSeparator.Numbers -> PasswordWordSeparator.Numbers
        WordSeparator.NumbersAndSymbols -> PasswordWordSeparator.NumbersAndSymbols
    }

    private fun UsernameWordTypesPreference.toDomain() = UsernameWordTypes(
        adjectives = adjectives,
        nouns = nouns,
        verbs = verbs
    )
}
