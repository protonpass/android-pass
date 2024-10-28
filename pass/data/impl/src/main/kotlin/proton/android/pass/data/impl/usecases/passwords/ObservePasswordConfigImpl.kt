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

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Some
import proton.android.pass.commonrust.api.passwords.PasswordConfig
import proton.android.pass.commonrust.api.passwords.PasswordWordSeparator
import proton.android.pass.data.api.usecases.organization.ObserveOrganizationPasswordPolicy
import proton.android.pass.data.api.usecases.passwords.ObservePasswordConfig
import proton.android.pass.domain.organizations.OrganizationPasswordPolicy
import proton.android.pass.preferences.PasswordGenerationMode
import proton.android.pass.preferences.PasswordGenerationPreference
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.preferences.WordSeparator
import javax.inject.Inject

class ObservePasswordConfigImpl @Inject constructor(
    private val observeOrganizationPasswordPolicy: ObserveOrganizationPasswordPolicy,
    private val userPreferencesRepository: UserPreferencesRepository
) : ObservePasswordConfig {

    override fun invoke(): Flow<PasswordConfig> = combine(
        observeOrganizationPasswordPolicy(),
        userPreferencesRepository.getPasswordGenerationPreference()
    ) { organizationPasswordPolicyOption, passwordGenerationPreference ->
        when (organizationPasswordPolicyOption) {
            None -> {
                passwordGenerationPreference.asPasswordConfig()
            }

            is Some -> {
                organizationPasswordPolicyOption.value.asPasswordConfig(passwordGenerationPreference)
            }
        }
    }

    private fun PasswordGenerationPreference.asPasswordConfig() = when (mode) {
        PasswordGenerationMode.Random -> PasswordConfig.Random(
            passwordLength = randomPasswordLength,
            includeUppercase = randomHasCapitalLetters,
            includeNumbers = randomIncludeNumbers,
            includeSymbols = randomHasSpecialCharacters
        )

        PasswordGenerationMode.Words -> PasswordConfig.Memorable(
            passwordWordsCount = wordsCount,
            passwordWordsSeparator = wordsSeparator.toDomain(),
            capitalizeWords = wordsCapitalise,
            includeNumbers = wordsIncludeNumbers
        )
    }

    private fun OrganizationPasswordPolicy.asPasswordConfig(preference: PasswordGenerationPreference) =
        when (getMode(preference)) {
            PasswordGenerationMode.Random -> PasswordConfig.Random(
                passwordLength = preference.randomPasswordLength,
                passwordMinLength = randomPasswordMinLength,
                passwordMaxLength = randomPasswordMaxLength,
                includeNumbers = randomPasswordIncludeNumbers ?: preference.randomIncludeNumbers,
                includeSymbols = randomPasswordIncludeSymbols
                    ?: preference.randomHasSpecialCharacters,
                includeUppercase = randomPasswordIncludeUppercase
                    ?: preference.randomHasCapitalLetters,
                canToggleNumbers = canToggleRandomPasswordNumbers,
                canToggleSymbols = canToggleRandomPasswordSymbols,
                canToggleUppercase = canToggleRandomPasswordUppercase,
                canToggleMode = canToggleRandomPasswordToMemorable
            )

            PasswordGenerationMode.Words -> PasswordConfig.Memorable(
                passwordWordsCount = preference.wordsCount,
                passwordWordsSeparator = preference.wordsSeparator.toDomain(),
                passwordMinWords = memorablePasswordMinWords,
                passwordMaxWords = memorablePasswordMaxWords,
                capitalizeWords = memorablePasswordCapitalize ?: preference.wordsCapitalise,
                includeNumbers = memorablePasswordIncludeNumbers ?: preference.wordsIncludeNumbers,
                canToggleCapitalise = canToggleMemorablePasswordCapitalize,
                canToggleNumbers = canToggleMemorablePasswordNumbers,
                canToggleMode = canToggleMemorablePasswordToRandom
            )
        }

    private fun OrganizationPasswordPolicy.getMode(preference: PasswordGenerationPreference) = when {
        canToggleRandomPasswordToMemorable && canToggleMemorablePasswordToRandom -> preference.mode
        canToggleRandomPasswordToMemorable -> PasswordGenerationMode.Words
        canToggleMemorablePasswordToRandom -> PasswordGenerationMode.Random
        else -> preference.mode
    }

    private fun WordSeparator.toDomain(): PasswordWordSeparator = when (this) {
        WordSeparator.Hyphen -> PasswordWordSeparator.Hyphen
        WordSeparator.Space -> PasswordWordSeparator.Space
        WordSeparator.Period -> PasswordWordSeparator.Period
        WordSeparator.Comma -> PasswordWordSeparator.Comma
        WordSeparator.Underscore -> PasswordWordSeparator.Underscore
        WordSeparator.Numbers -> PasswordWordSeparator.Numbers
        WordSeparator.NumbersAndSymbols -> PasswordWordSeparator.NumbersAndSymbols
    }

}
