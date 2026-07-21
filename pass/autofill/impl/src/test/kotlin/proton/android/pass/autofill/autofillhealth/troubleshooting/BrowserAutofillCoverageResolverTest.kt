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

package proton.android.pass.autofill.autofillhealth.troubleshooting

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import proton.android.pass.autofill.autofillhealth.troubleshooting.data.BrowserAutofillCoverage
import proton.android.pass.autofill.autofillhealth.troubleshooting.data.BrowserAutofillCoverageResolver

class BrowserAutofillCoverageResolverTest {

    @Test
    fun `ready when version is at or below the compat cap`() {
        assertThat(BrowserAutofillCoverageResolver.resolve(500, 1000, hasBeenSeenWorking = false))
            .isEqualTo(BrowserAutofillCoverage.Ready)
        assertThat(BrowserAutofillCoverageResolver.resolve(1000, 1000, hasBeenSeenWorking = false))
            .isEqualTo(BrowserAutofillCoverage.Ready)
    }

    @Test
    fun `needs setup when version is above the compat cap`() {
        assertThat(BrowserAutofillCoverageResolver.resolve(1001, 1000, hasBeenSeenWorking = false))
            .isEqualTo(BrowserAutofillCoverage.NeedsSetup)
    }

    @Test
    fun `needs setup when browser is not in the compat list`() {
        assertThat(BrowserAutofillCoverageResolver.resolve(500, null, hasBeenSeenWorking = false))
            .isEqualTo(BrowserAutofillCoverage.NeedsSetup)
    }

    @Test
    fun `working takes precedence once the browser has been seen working`() {
        assertThat(BrowserAutofillCoverageResolver.resolve(1001, 1000, hasBeenSeenWorking = true))
            .isEqualTo(BrowserAutofillCoverage.Working)
        assertThat(BrowserAutofillCoverageResolver.resolve(500, null, hasBeenSeenWorking = true))
            .isEqualTo(BrowserAutofillCoverage.Working)
    }
}
