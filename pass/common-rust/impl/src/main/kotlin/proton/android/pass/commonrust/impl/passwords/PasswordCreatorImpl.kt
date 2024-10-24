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
import proton.android.pass.commonrust.api.WordSeparator
import proton.android.pass.commonrust.api.passwords.PasswordConfig
import proton.android.pass.commonrust.api.passwords.PasswordCreator
import javax.inject.Inject
import proton.android.pass.commonrust.WordSeparator as RustWordSeparator

class PasswordCreatorImpl @Inject constructor(
    private val appDispatchers: AppDispatchers
) : PasswordCreator {

    override suspend fun createPassword(config: PasswordConfig): String = withContext(appDispatchers.default) {
        when (config) {
            is PasswordConfig.Random -> {
                RandomPasswordConfig(
                    length = config.length.toUInt(),
                    numbers = config.includeNumbers,
                    uppercaseLetters = config.includeUppercase,
                    symbols = config.includeSymbols
                ).let(RandomPasswordGenerator()::generate)
            }

            is PasswordConfig.Memorable -> {
                PassphraseConfig(
                    separator = config.wordSeparator.asRustWordSeparator(),
                    capitalise = config.capitalizeWords,
                    includeNumbers = config.includeNumbers,
                    count = config.wordsCount.toUInt()
                ).let(PassphraseGenerator()::generateRandomPassphrase)
            }
        }
    }

    private fun WordSeparator?.asRustWordSeparator() = when (this) {
        WordSeparator.Hyphen -> RustWordSeparator.HYPHENS
        WordSeparator.Space -> RustWordSeparator.SPACES
        WordSeparator.Period -> RustWordSeparator.PERIODS
        WordSeparator.Comma -> RustWordSeparator.COMMAS
        WordSeparator.Underscore -> RustWordSeparator.UNDERSCORES
        WordSeparator.Numbers -> RustWordSeparator.NUMBERS
        WordSeparator.NumbersAndSymbols -> RustWordSeparator.NUMBERS_AND_SYMBOLS
        else -> RustWordSeparator.SPACES
    }

}
