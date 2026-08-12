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

package proton.android.pass.autofill.autofillhealth.troubleshooting.presentation

import proton.android.pass.autofill.autofillhealth.model.AutofillHealthEvent
import proton.android.pass.autofill.autofillhealth.model.AutofillHealthEventType

object AutofillAttemptResolver {

    private const val MAX_RECENT_ATTEMPTS = 10

    fun resolve(events: List<AutofillHealthEvent>, appLabelResolver: (String?) -> String): List<AutofillAttempt> =
        events
            .mapNotNull { event ->
                val outcome = event.type.toOutcome() ?: return@mapNotNull null
                AutofillAttempt(
                    packageName = event.packageName.orEmpty(),
                    appLabel = appLabelResolver(event.packageName),
                    webDomain = event.webDomain,
                    outcome = outcome,
                    timestamp = event.timestamp
                )
            }
            .sortedByDescending { it.timestamp }
            .take(MAX_RECENT_ATTEMPTS)

    private fun AutofillHealthEventType.toOutcome(): AutofillAttemptOutcome? = when (this) {
        AutofillHealthEventType.FILL_REQUEST_INLINE,
        AutofillHealthEventType.FILL_REQUEST_MENU -> AutofillAttemptOutcome.Offered
        AutofillHealthEventType.FILL_REQUEST_NONE -> AutofillAttemptOutcome.NothingOffered
        AutofillHealthEventType.FILL_REQUEST_ERROR -> AutofillAttemptOutcome.Error
        AutofillHealthEventType.FILL_REQUEST_SKIPPED,
        AutofillHealthEventType.CREATED,
        AutofillHealthEventType.CONNECTED,
        AutofillHealthEventType.DISCONNECTED -> null
    }
}
