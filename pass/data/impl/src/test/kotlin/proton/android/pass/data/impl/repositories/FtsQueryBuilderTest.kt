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
 *
 * The trigram tokenizer is used; each token is wrapped in double quotes so FTS5 treats it as a
 * phrase and the trigram index performs contiguous substring matching (not prefix-only matching).
 */
class FtsQueryBuilderTest {

    @Test
    fun `single term is wrapped in double quotes for substring matching`() {
        assertThat(FtsQueryBuilder.build("github")).isEqualTo("\"github\"")
    }

    @Test
    fun `multiple terms are each wrapped in double quotes`() {
        assertThat(FtsQueryBuilder.build("git hub")).isEqualTo("\"git\" \"hub\"")
    }

    @Test
    fun `leading and trailing whitespace is trimmed and collapsed`() {
        assertThat(FtsQueryBuilder.build("  git   hub  ")).isEqualTo("\"git\" \"hub\"")
    }

    @Test
    fun `hyphen is treated as a separator`() {
        assertThat(FtsQueryBuilder.build("foo-bar")).isEqualTo("\"foo\" \"bar\"")
    }

    @Test
    fun `blank input produces an empty expression`() {
        // Callers treat a blank result as "no FTS filter"; it must never throw.
        assertThat(FtsQueryBuilder.build("")).isEmpty()
        assertThat(FtsQueryBuilder.build("    ")).isEmpty()
    }

    @Test
    fun `input made entirely of special characters produces an empty expression`() {
        // Must not produce a lone quoted empty string like '""' which is an invalid FTS5 MATCH.
        assertThat(FtsQueryBuilder.build("*()[]+^:{}~")).isEmpty()
    }

    @Test
    fun `fts5 syntax characters are stripped`() {
        // Parentheses, brackets, column filter ':' and the boost '^' must not survive into MATCH.
        assertThat(FtsQueryBuilder.build("(title:foo)^2")).isEqualTo("\"titlefoo2\"")
    }

    @Test
    fun `embedded quotes are escaped by doubling inside the phrase`() {
        // A bare double-quote inside a phrase is doubled per FTS5 phrase escaping rules.
        assertThat(FtsQueryBuilder.build("a\"b")).isEqualTo("\"a\"\"b\"")
    }

    @Test
    fun `standalone NOT operator is removed`() {
        assertThat(FtsQueryBuilder.build("NOT secret")).isEqualTo("\"secret\"")
    }

    @Test
    fun `query with a token of 3 or more chars is matchable`() {
        assertThat(FtsQueryBuilder.hasMatchableToken("git")).isTrue()
        assertThat(FtsQueryBuilder.hasMatchableToken("gi hub")).isTrue()
    }

    @Test
    fun `query made only of shorter tokens is not matchable`() {
        // The trigram tokenizer produces no trigrams for tokens under 3 chars.
        assertThat(FtsQueryBuilder.hasMatchableToken("g")).isFalse()
        assertThat(FtsQueryBuilder.hasMatchableToken("gi")).isFalse()
        assertThat(FtsQueryBuilder.hasMatchableToken("a b")).isFalse()
        assertThat(FtsQueryBuilder.hasMatchableToken("")).isFalse()
    }
}
