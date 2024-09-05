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

package proton.android.pass.commonrust.api

@Suppress("UnusedPrivateMember")
sealed class AliasPrefixError(override val message: String) : RuntimeException(message) {
    data object DotAtTheBeginning : AliasPrefixError("DotAtTheBeginning") {
        private fun readResolve(): Any = DotAtTheBeginning
    }

    data object DotAtTheEnd : AliasPrefixError("DotAtTheEnd") {
        private fun readResolve(): Any = DotAtTheEnd
    }

    data object InvalidCharacter : AliasPrefixError("InvalidCharacter") {
        private fun readResolve(): Any = InvalidCharacter
    }

    data object PrefixEmpty : AliasPrefixError("PrefixEmpty") {
        private fun readResolve(): Any = PrefixEmpty
    }

    data object PrefixTooLong : AliasPrefixError("PrefixTooLong") {
        private fun readResolve(): Any = PrefixTooLong
    }

    data object TwoConsecutiveDots : AliasPrefixError("TwoConsecutiveDots") {
        private fun readResolve(): Any = TwoConsecutiveDots
    }
}

interface AliasPrefixValidator {
    fun validate(prefix: String): Result<Unit>
}
