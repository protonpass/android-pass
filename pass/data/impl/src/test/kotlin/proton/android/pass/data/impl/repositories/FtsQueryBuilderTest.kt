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

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Pins the contract for turning search-box text into an FTS5 MATCH expression. The output is
 * bound as a query argument, but it must still be valid FTS5 syntax or the MATCH throws — so
 * these cases guard against regressions that would crash search or change matching semantics.
 */
class FtsQueryBuilderTest {

    @Test
    fun `single term gets a prefix wildcard`() {
        assertThat(FtsQueryBuilder.build("github")).isEqualTo("github*")
    }

    @Test
    fun `multiple terms each get a prefix wildcard`() {
        assertThat(FtsQueryBuilder.build("git hub")).isEqualTo("git* hub*")
    }

    @Test
    fun `leading and trailing whitespace is trimmed and collapsed`() {
        assertThat(FtsQueryBuilder.build("  git   hub  ")).isEqualTo("git* hub*")
    }

    @Test
    fun `hyphen is treated as a separator`() {
        assertThat(FtsQueryBuilder.build("foo-bar")).isEqualTo("foo* bar*")
    }

    @Test
    fun `blank input produces an empty expression`() {
        // Callers treat a blank result as "no FTS filter"; it must never throw.
        assertThat(FtsQueryBuilder.build("")).isEmpty()
        assertThat(FtsQueryBuilder.build("    ")).isEmpty()
    }

    @Test
    fun `input made entirely of special characters produces an empty expression`() {
        // Must not yield a lone wildcard like "*" which is an invalid FTS5 MATCH.
        assertThat(FtsQueryBuilder.build("*()[]+^:{}~")).isEmpty()
    }

    @Test
    fun `fts5 syntax characters are stripped`() {
        // Parentheses, brackets, column filter ':' and the boost '^' must not survive into MATCH.
        assertThat(FtsQueryBuilder.build("(title:foo)^2")).isEqualTo("titlefoo2*")
    }

    @Test
    fun `embedded quotes are escaped by doubling`() {
        // A bare double-quote would open an unterminated FTS5 phrase; it is doubled to stay literal.
        assertThat(FtsQueryBuilder.build("a\"b")).isEqualTo("a\"\"b*")
    }

    @Test
    fun `standalone NOT operator is removed`() {
        assertThat(FtsQueryBuilder.build("NOT secret")).isEqualTo("secret*")
    }
}
