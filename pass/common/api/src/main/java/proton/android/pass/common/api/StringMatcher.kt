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

package proton.android.pass.common.api

import java.text.Normalizer

data class MatchSpan(val start: Int, val end: Int)

object StringMatcher {

    fun match(
        haystack: String,
        needle: String,
        removeDiacritics: Boolean = true
    ): List<MatchSpan> {
        if (needle.isBlank()) return emptyList()

        val normalizedNeedle = normalize(needle, removeDiacritics)
        val normalizedHaystack = normalize(haystack, removeDiacritics)

        val searchWords = escapeRegex(normalizedNeedle)
            .trim()
            .replace("\\s+".toRegex(), "|")

        val regex = Regex(searchWords, RegexOption.IGNORE_CASE)
        return getMatches(regex, normalizedHaystack)
    }

    private fun normalize(value: String, removeDiacritics: Boolean = false): String {
        var normalized = value.lowercase().trim()
        if (removeDiacritics) {
            normalized = Normalizer.normalize(normalized, Normalizer.Form.NFD)
                .replace(Regex("[\\u0300-\\u036f]"), "")
        }
        return normalized
    }

    private fun escapeRegex(string: String) = Regex("[-/\\\\^\$*+?.()|\\[\\]{}]")
        .replace(string) { "\\${it.value}" }

    private fun getMatches(regex: Regex, b: String): List<MatchSpan> = regex.findAll(b)
        .map { match ->
            val index = match.range.first
            MatchSpan(index, index + match.value.length)
        }
        .toList()


}
