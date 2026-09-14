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

package proton.android.pass.data.impl.repositories

internal object FtsQueryBuilder {

    // The trigram tokenizer indexes overlapping 3-character sequences, so a query token shorter
    // than this can never match anything, even though the term is present in the indexed text.
    private const val MIN_MATCHABLE_TOKEN_LENGTH = 3

    fun hasMatchableToken(query: String): Boolean = query
        .trim()
        .split("\\s+".toRegex())
        .any { it.length >= MIN_MATCHABLE_TOKEN_LENGTH }

    fun build(query: String): String {
        val escaped = query
            .trim()
            .replace("\"", "\"\"")
            .replace("*", "")
            .replace("(", "")
            .replace(")", "")
            .replace("[", "")
            .replace("]", "")
            .replace("-", " ")
            .replace("+", "")
            .replace("^", "")
            .replace(":", "")
            .replace("{", "")
            .replace("}", "")
            .replace("~", "")
            .replace("NOT ", " ")

        return escaped.split("\\s+".toRegex())
            .filter { it.isNotBlank() }
            .joinToString(" ") { "\"$it\"" }
    }
}
