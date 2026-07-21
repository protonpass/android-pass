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
import proton.android.pass.autofill.autofillhealth.model.AutofillHealthEvent
import proton.android.pass.autofill.autofillhealth.model.AutofillHealthEventType
import proton.android.pass.autofill.autofillhealth.troubleshooting.presentation.AutofillAttemptOutcome
import proton.android.pass.autofill.autofillhealth.troubleshooting.presentation.AutofillAttemptResolver

class AutofillAttemptResolverTest {

    private val labelResolver: (String?) -> String = { it ?: "" }

    @Test
    fun `ignores lifecycle events and keeps only fill requests`() {
        val events = listOf(
            AutofillHealthEvent(timestamp = 1, type = AutofillHealthEventType.CREATED),
            AutofillHealthEvent(timestamp = 2, type = AutofillHealthEventType.CONNECTED),
            AutofillHealthEvent(
                timestamp = 3,
                type = AutofillHealthEventType.FILL_REQUEST_NONE,
                packageName = "com.android.chrome"
            )
        )

        val result = AutofillAttemptResolver.resolve(events, labelResolver)

        assertThat(result).hasSize(1)
        assertThat(result.first().packageName).isEqualTo("com.android.chrome")
        assertThat(result.first().outcome).isEqualTo(AutofillAttemptOutcome.NothingOffered)
    }

    @Test
    fun `maps each fill request type to the matching outcome`() {
        val events = listOf(
            AutofillHealthEvent(timestamp = 1, type = AutofillHealthEventType.FILL_REQUEST_INLINE),
            AutofillHealthEvent(timestamp = 2, type = AutofillHealthEventType.FILL_REQUEST_MENU),
            AutofillHealthEvent(timestamp = 3, type = AutofillHealthEventType.FILL_REQUEST_NONE),
            AutofillHealthEvent(timestamp = 4, type = AutofillHealthEventType.FILL_REQUEST_ERROR)
        )

        val outcomes = AutofillAttemptResolver.resolve(events, labelResolver).map { it.outcome }

        assertThat(outcomes).containsExactly(
            AutofillAttemptOutcome.Error,
            AutofillAttemptOutcome.NothingOffered,
            AutofillAttemptOutcome.Offered,
            AutofillAttemptOutcome.Offered
        )
    }

    @Test
    fun `keeps the web domain and builds a readable display target`() {
        val events = listOf(
            AutofillHealthEvent(
                timestamp = 1,
                type = AutofillHealthEventType.FILL_REQUEST_MENU,
                packageName = "com.android.chrome",
                webDomain = "example.com"
            )
        )

        val attempt = AutofillAttemptResolver.resolve(events) { "Chrome" }.first()

        assertThat(attempt.webDomain).isEqualTo("example.com")
        assertThat(attempt.displayTarget).isEqualTo("Chrome – example.com")
    }

    @Test
    fun `display target falls back to app label when no domain`() {
        val events = listOf(
            AutofillHealthEvent(
                timestamp = 1,
                type = AutofillHealthEventType.FILL_REQUEST_MENU,
                packageName = "com.example.app"
            )
        )

        val attempt = AutofillAttemptResolver.resolve(events) { "Sample App" }.first()

        assertThat(attempt.displayTarget).isEqualTo("Sample App (com.example.app)")
    }

    @Test
    fun `sorts attempts most recent first`() {
        val events = listOf(
            AutofillHealthEvent(timestamp = 10, type = AutofillHealthEventType.FILL_REQUEST_MENU),
            AutofillHealthEvent(timestamp = 30, type = AutofillHealthEventType.FILL_REQUEST_NONE),
            AutofillHealthEvent(timestamp = 20, type = AutofillHealthEventType.FILL_REQUEST_ERROR)
        )

        val timestamps = AutofillAttemptResolver.resolve(events, labelResolver).map { it.timestamp }

        assertThat(timestamps).containsExactly(30L, 20L, 10L).inOrder()
    }
}
