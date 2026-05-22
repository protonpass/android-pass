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

/**
 * Turns a user's free-text search box input into an FTS5 MATCH expression. The output is bound
 * as a query argument (never concatenated into SQL), but it still must be a *valid* FTS5
 * expression or the MATCH throws at runtime — so special FTS5 syntax characters are stripped
 * and each remaining token gets a prefix wildcard. Behaviour is pinned by FtsQueryBuilderTest.
 */
internal object FtsQueryBuilder {

    fun build(query: String): String {
        // Escape special FTS5 characters and add prefix matching
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

        // Split into words and add prefix matching to each
        return escaped.split("\\s+".toRegex())
            .filter { it.isNotBlank() }
            .joinToString(" ") { "$it*" }
    }
}
