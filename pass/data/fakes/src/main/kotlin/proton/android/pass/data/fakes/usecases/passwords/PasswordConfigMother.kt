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

package proton.android.pass.data.fakes.usecases.passwords

import proton.android.pass.commonrust.api.passwords.PasswordConfig
import proton.android.pass.commonrust.api.passwords.PasswordWordSeparator
import kotlin.random.Random

object PasswordConfigMother {

    fun random(): PasswordConfig = if (Random.nextBoolean()) {
        RandomPassword.create()
    } else {
        MemorablePassword.create()
    }

    object RandomPassword {

        fun create(
            canToggleMode: Boolean = Random.nextBoolean(),
            includeUppercase: Boolean = Random.nextBoolean(),
            includeNumbers: Boolean = Random.nextBoolean(),
            includeSymbols: Boolean = Random.nextBoolean(),
            canToggleUppercase: Boolean = Random.nextBoolean(),
            canToggleNumbers: Boolean = Random.nextBoolean(),
            canToggleSymbols: Boolean = Random.nextBoolean(),
            passwordLength: Int = Random.nextInt(),
            passwordMinLength: Int = Random.nextInt(),
            passwordMaxLength: Int = Random.nextInt()
        ): PasswordConfig.Random = PasswordConfig.Random(
            canToggleMode = canToggleMode,
            includeUppercase = includeUppercase,
            includeNumbers = includeNumbers,
            includeSymbols = includeSymbols,
            canToggleUppercase = canToggleUppercase,
            canToggleNumbers = canToggleNumbers,
            canToggleSymbols = canToggleSymbols,
            passwordLength = passwordLength,
            passwordMinLength = passwordMinLength,
            passwordMaxLength = passwordMaxLength
        )

    }

    object MemorablePassword {

        fun create(
            canToggleMode: Boolean = Random.nextBoolean(),
            capitalizeWords: Boolean = Random.nextBoolean(),
            includeNumbers: Boolean = Random.nextBoolean(),
            canToggleCapitalise: Boolean = Random.nextBoolean(),
            canToggleNumbers: Boolean = Random.nextBoolean(),
            passwordWordsCount: Int = Random.nextInt(),
            passwordWordsSeparator: PasswordWordSeparator = PasswordWordSeparator.entries.random(),
            passwordMinWords: Int = Random.nextInt(),
            passwordMaxWords: Int = Random.nextInt()
        ): PasswordConfig.Memorable = PasswordConfig.Memorable(
            canToggleMode = canToggleMode,
            capitalizeWords = capitalizeWords,
            includeNumbers = includeNumbers,
            canToggleCapitalise = canToggleCapitalise,
            canToggleNumbers = canToggleNumbers,
            passwordWordsCount = passwordWordsCount,
            passwordWordsSeparator = passwordWordsSeparator,
            passwordMinWords = passwordMinWords,
            passwordMaxWords = passwordMaxWords
        )

    }

}
