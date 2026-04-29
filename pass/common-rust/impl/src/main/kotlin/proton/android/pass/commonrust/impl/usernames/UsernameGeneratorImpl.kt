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

package proton.android.pass.commonrust.impl.usernames

import kotlinx.coroutines.withContext
import proton.android.pass.common.api.AppDispatchers
import proton.android.pass.commonrust.api.passwords.PasswordWordSeparator
import proton.android.pass.commonrust.api.usernames.UsernameConfig
import proton.android.pass.commonrust.api.usernames.UsernameGenerator
import javax.inject.Inject
import proton.android.pass.commonrust.UsernameGenerator as RustUsernameGenerator
import proton.android.pass.commonrust.UsernameGeneratorConfig as RustUsernameGeneratorConfig
import proton.android.pass.commonrust.WordSeparator as RustWordSeparator
import proton.android.pass.commonrust.WordTypes as RustWordTypes

class UsernameGeneratorImpl @Inject constructor(
    private val appDispatchers: AppDispatchers
) : UsernameGenerator {

    override suspend fun generateUsername(config: UsernameConfig): String = withContext(appDispatchers.default) {
        require(config.wordsCount > 0) { "wordsCount must be positive" }
        require(config.wordTypes.isValid) { "at least one word type must be enabled" }
        RustUsernameGenerator().use { generator ->
            generator.generate(
                RustUsernameGeneratorConfig(
                    wordCount = config.wordsCount.toUInt(),
                    includeNumbers = config.includeNumbers,
                    capitalise = config.capitalise,
                    separator = config.wordSeparator.asRustWordSeparator(),
                    leetspeak = config.leetspeak,
                    wordTypes = RustWordTypes(
                        adjectives = config.wordTypes.adjectives,
                        nouns = config.wordTypes.nouns,
                        verbs = config.wordTypes.verbs
                    )
                )
            )
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
