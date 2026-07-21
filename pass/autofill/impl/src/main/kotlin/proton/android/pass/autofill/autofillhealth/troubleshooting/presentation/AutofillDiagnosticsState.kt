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

import androidx.compose.runtime.Immutable

enum class AutofillAttemptOutcome {
    Offered,
    NothingOffered,
    Error
}

@Immutable
data class AutofillAttempt(
    val packageName: String,
    val appLabel: String,
    val webDomain: String?,
    val outcome: AutofillAttemptOutcome,
    val timestamp: Long
) {
    val displayTarget: String
        get() = when {
            !webDomain.isNullOrBlank() && appLabel.isNotBlank() -> "$appLabel – $webDomain"
            !webDomain.isNullOrBlank() -> webDomain
            appLabel.isNotBlank() && appLabel != packageName -> "$appLabel ($packageName)"
            else -> packageName
        }
}

@Immutable
data class AutofillDiagnosticsState(
    val isDiagnosticsEnabled: Boolean = false,
    val serviceStatus: AutofillServiceStatus = AutofillServiceStatus.Loading,
    val lastAttempt: AutofillAttempt? = null,
    val recentAttempts: List<AutofillAttempt> = emptyList()
) {
    companion object {
        val Initial = AutofillDiagnosticsState()
    }
}
