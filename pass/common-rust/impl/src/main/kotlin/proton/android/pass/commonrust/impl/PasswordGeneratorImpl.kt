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

package proton.android.pass.commonrust.impl

import proton.android.pass.commonrust.PassphraseGenerator
import proton.android.pass.commonrust.RandomPasswordConfig
import proton.android.pass.commonrust.RandomPasswordGenerator
import proton.android.pass.commonrust.api.PassphraseConfig
import proton.android.pass.commonrust.api.PasswordGenerator
import proton.android.pass.commonrust.api.PasswordGeneratorConfig
import proton.android.pass.commonrust.api.WordSeparator
import javax.inject.Inject
import proton.android.pass.commonrust.PassphraseConfig as RustPassphraseConfig
import proton.android.pass.commonrust.WordSeparator as RustWordSeparator

class PasswordGeneratorImpl @Inject constructor() : PasswordGenerator {

    override fun generatePassword(config: PasswordGeneratorConfig): String =
        RandomPasswordGenerator()
            .generate(
                RandomPasswordConfig(
                    length = config.length,
                    numbers = config.numbers,
                    uppercaseLetters = config.uppercaseLetters,
                    symbols = config.symbols
                )
            )

    override fun generatePassphrase(config: PassphraseConfig): String =
        PassphraseGenerator().generateRandomPassphrase(
            RustPassphraseConfig(
                separator = config.separator.toBind(),
                capitalise = config.capitalise,
                includeNumbers = config.numbers,
                count = config.count
            )
        )

    private fun WordSeparator.toBind(): RustWordSeparator = when (this) {
        WordSeparator.Hyphen -> RustWordSeparator.HYPHENS
        WordSeparator.Space -> RustWordSeparator.SPACES
        WordSeparator.Period -> RustWordSeparator.PERIODS
        WordSeparator.Comma -> RustWordSeparator.COMMAS
        WordSeparator.Underscore -> RustWordSeparator.UNDERSCORES
        WordSeparator.Numbers -> RustWordSeparator.NUMBERS
        WordSeparator.NumbersAndSymbols -> RustWordSeparator.NUMBERS_AND_SYMBOLS
    }
}
