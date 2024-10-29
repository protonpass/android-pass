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

package proton.android.pass.commonrust.impl.passwords

import kotlinx.coroutines.withContext
import proton.android.pass.common.api.AppDispatchers
import proton.android.pass.commonrust.PassphraseConfig
import proton.android.pass.commonrust.PassphraseGenerator
import proton.android.pass.commonrust.RandomPasswordConfig
import proton.android.pass.commonrust.RandomPasswordGenerator
import proton.android.pass.commonrust.api.passwords.PasswordConfig
import proton.android.pass.commonrust.api.passwords.PasswordGenerator
import proton.android.pass.commonrust.api.passwords.PasswordWordSeparator
import javax.inject.Inject
import proton.android.pass.commonrust.WordSeparator as RustWordSeparator

class PasswordGeneratorImpl @Inject constructor(
    private val appDispatchers: AppDispatchers,
    private val randomPasswordGenerator: RandomPasswordGenerator,
    private val passphraseGenerator: PassphraseGenerator
) : PasswordGenerator {

    override suspend fun generatePassword(config: PasswordConfig): String = withContext(appDispatchers.default) {
        when (config) {
            is PasswordConfig.Random -> {
                RandomPasswordConfig(
                    length = config.length.toUInt(),
                    numbers = config.includeNumbers,
                    uppercaseLetters = config.includeUppercase,
                    symbols = config.includeSymbols
                ).let(randomPasswordGenerator::generate)
            }

            is PasswordConfig.Memorable -> {
                PassphraseConfig(
                    separator = config.wordSeparator.asRustWordSeparator(),
                    capitalise = config.capitalizeWords,
                    includeNumbers = config.includeNumbers,
                    count = config.wordsCount.toUInt()
                ).let(passphraseGenerator::generateRandomPassphrase)
            }
        }
    }

    private fun PasswordWordSeparator.asRustWordSeparator() = when (this) {
        PasswordWordSeparator.Hyphen -> RustWordSeparator.HYPHENS
        PasswordWordSeparator.Space -> RustWordSeparator.SPACES
        PasswordWordSeparator.Period -> RustWordSeparator.PERIODS
        PasswordWordSeparator.Comma -> RustWordSeparator.COMMAS
        PasswordWordSeparator.Underscore -> RustWordSeparator.UNDERSCORES
        PasswordWordSeparator.Numbers -> RustWordSeparator.NUMBERS
        PasswordWordSeparator.NumbersAndSymbols -> RustWordSeparator.NUMBERS_AND_SYMBOLS
    }

}
