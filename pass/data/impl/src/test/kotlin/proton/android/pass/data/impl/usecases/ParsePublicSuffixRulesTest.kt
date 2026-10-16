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
 * along with Proton Pass. If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.data.impl.usecases

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParsePublicSuffixRulesTest {

    @Test
    fun `unicode exact rules are indexed in both unicode and punycode forms`() {
        val rules = parsePublicSuffixRules(sequenceOf("no", "ålesund.no", "co.uk"))

        assertTrue(rules.exact.contains("ålesund.no"))
        assertTrue(rules.exact.contains("xn--lesund-hua.no"))
        assertTrue(rules.exact.contains("co.uk"))
    }

    @Test
    fun `wildcard and exception rules are indexed in both forms`() {
        val rules = parsePublicSuffixRules(sequenceOf("*.dynamic.日本.example", "!city.dynamic.日本.example"))

        assertTrue(rules.wildcardBases.contains("dynamic.日本.example"))
        assertTrue(rules.wildcardBases.contains("dynamic.xn--wgv71a.example"))
        assertTrue(rules.exceptions.contains("city.dynamic.日本.example"))
        assertTrue(rules.exceptions.contains("city.dynamic.xn--wgv71a.example"))
    }

    @Test
    fun `blank lines are ignored and ascii rules are stored unchanged`() {
        val rules = parsePublicSuffixRules(sequenceOf("com", "", "org"))

        assertEquals(setOf("com", "org"), rules.exact)
    }
}
