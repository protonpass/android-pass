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

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class StringMatcherTest {

    @Test
    fun `simple test`() {
        val matches = StringMatcher.match("This is an example test", "is test")
        assertThat(matches).isEqualTo(
            listOf(
                MatchSpan(2, 4), // is
                MatchSpan(5, 7), // is
                MatchSpan(19, 23) // test
            )
        )
    }

    @Test
    fun `with accented characters`() {
        val matches = StringMatcher.match("cáda vez què pruébo és ùná ìndïcácîóN", "es indi")
        assertThat(matches).isEqualTo(
            listOf(
                MatchSpan(20, 22), // és
                MatchSpan(27, 31) // ìndï
            )
        )
    }

    @Test
    fun `with special characters`() {
        val matches = StringMatcher.match(
            "this is (a (*string) that is/full of/&chars",
            "string * chars"
        )

        assertThat(matches).isEqualTo(
            listOf(
                MatchSpan(12, 13), // *
                MatchSpan(13, 19), // string
                MatchSpan(38, 43) // chars
            )
        )
    }

    @Test
    fun `with squared brackets`() {
        val matches = StringMatcher.match(
            "this is [a [string] that is/full of/&chars",
            "string ] [ chars"
        )

        assertThat(matches).isEqualTo(
            listOf(
                MatchSpan(8, 9), // [
                MatchSpan(11, 12), // [
                MatchSpan(12, 18), // string
                MatchSpan(18, 19), // ]
                MatchSpan(37, 42), // chars
            )
        )
    }
}
