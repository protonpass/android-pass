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

package proton.android.pass.autofill.autofillhealth.troubleshooting.ui

import android.content.Context
import android.content.Intent
import android.os.Build
import proton.android.pass.autofill.autofillhealth.troubleshooting.presentation.AutofillAttempt
import proton.android.pass.autofill.autofillhealth.troubleshooting.presentation.AutofillAttemptOutcome
import proton.android.pass.autofill.autofillhealth.troubleshooting.presentation.AutofillDiagnosticsState
import proton.android.pass.autofill.autofillhealth.troubleshooting.presentation.AutofillServiceStatus
import proton.android.pass.log.api.PassLogger
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AutofillDiagnosticsActions {

    private const val TAG = "AutofillDiagnosticsActions"

    fun share(context: Context, state: AutofillDiagnosticsState) {
        val text = buildReport(context, state)
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        val chooser = Intent.createChooser(sendIntent, null)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(chooser) }
            .onFailure { PassLogger.w(TAG, "Could not share autofill diagnostics") }
    }

    private fun buildReport(context: Context, state: AutofillDiagnosticsState): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        return buildString {
            appendLine("Proton Pass autofill diagnostics")
            appendLine("Default autofill service: ${isDefault(state.serviceStatus)}")
            appendLine("Android SDK: ${Build.VERSION.SDK_INT}")
            appendLine("Device: ${Build.MANUFACTURER} ${Build.MODEL}")
            appendLine("App version: ${appVersion(context)}")
            appendLine()
            if (state.recentAttempts.isEmpty()) {
                appendLine("No autofill attempts recorded.")
            } else {
                appendLine("Recent autofill attempts:")
                state.recentAttempts.forEach { attempt ->
                    appendLine(
                        "- ${dateFormat.format(Date(attempt.timestamp))} " +
                            "${reportTarget(attempt)}: ${outcome(attempt)}"
                    )
                }
            }
        }
    }

    private fun reportTarget(attempt: AutofillAttempt): String {
        val label = when {
            !attempt.webDomain.isNullOrBlank() -> attempt.webDomain
            attempt.appLabel.isNotBlank() -> attempt.appLabel
            else -> attempt.packageName
        }
        return if (label == attempt.packageName) label else "$label (${attempt.packageName})"
    }

    private fun outcome(attempt: AutofillAttempt): String = when (attempt.outcome) {
        AutofillAttemptOutcome.Offered -> "suggestions offered"
        AutofillAttemptOutcome.NothingOffered -> "nothing offered"
        AutofillAttemptOutcome.Error -> "error"
    }

    private fun isDefault(status: AutofillServiceStatus): Boolean = status == AutofillServiceStatus.EnabledByOurService

    private fun appVersion(context: Context): String = runCatching {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName.orEmpty()
    }.getOrDefault("")
}
