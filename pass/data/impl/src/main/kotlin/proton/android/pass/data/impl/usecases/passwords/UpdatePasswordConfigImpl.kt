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

package proton.android.pass.data.impl.usecases.passwords

import kotlinx.coroutines.flow.firstOrNull
import proton.android.pass.commonrust.api.WordSeparator
import proton.android.pass.commonrust.api.passwords.PasswordConfig
import proton.android.pass.data.api.usecases.passwords.UpdatePasswordConfig
import proton.android.pass.preferences.UserPreferencesRepository
import javax.inject.Inject

class UpdatePasswordConfigImpl @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : UpdatePasswordConfig {

    override suspend fun invoke(config: PasswordConfig) {
        userPreferencesRepository.getPasswordGenerationPreference()
            .firstOrNull()
            ?.let { passwordGenerationPreference ->
                when (config) {
                    is PasswordConfig.Memorable -> passwordGenerationPreference.copy(
                        wordsCount = config.wordsCount,
                        wordsSeparator = config.wordSeparator.toPreference(),
                        wordsCapitalise = config.capitalizeWords,
                        wordsIncludeNumbers = config.includeNumbers
                    )

                    is PasswordConfig.Random -> passwordGenerationPreference.copy(
                        randomPasswordLength = config.length,
                        randomHasSpecialCharacters = config.includeSymbols,
                        randomHasCapitalLetters = config.includeUppercase,
                        randomIncludeNumbers = config.includeNumbers
                    )
                }.also(userPreferencesRepository::setPasswordGenerationPreference)
            }
    }

    private fun WordSeparator.toPreference(): proton.android.pass.preferences.WordSeparator = when (this) {
        WordSeparator.Hyphen -> proton.android.pass.preferences.WordSeparator.Hyphen
        WordSeparator.Space -> proton.android.pass.preferences.WordSeparator.Space
        WordSeparator.Period -> proton.android.pass.preferences.WordSeparator.Period
        WordSeparator.Comma -> proton.android.pass.preferences.WordSeparator.Comma
        WordSeparator.Underscore -> proton.android.pass.preferences.WordSeparator.Underscore
        WordSeparator.Numbers -> proton.android.pass.preferences.WordSeparator.Numbers
        WordSeparator.NumbersAndSymbols -> proton.android.pass.preferences.WordSeparator.NumbersAndSymbols
    }

}
