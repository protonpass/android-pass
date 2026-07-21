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
import proton.android.pass.autofill.autofillhealth.troubleshooting.data.AutofillDiagnosticsBuilder
import proton.android.pass.autofill.autofillhealth.troubleshooting.data.AutofillDiagnosticsInput
import proton.android.pass.autofill.autofillhealth.troubleshooting.presentation.AutofillServiceStatus
import proton.android.pass.autofill.autofillhealth.troubleshooting.presentation.AutofillTroubleshootingState
import proton.android.pass.log.api.PassLogger

object AutofillTroubleshootingActions {

    private const val TAG = "AutofillTroubleshootingActions"

    fun shareDiagnostics(context: Context, state: AutofillTroubleshootingState) {
        val text = AutofillDiagnosticsBuilder.build(
            AutofillDiagnosticsInput(
                isDefaultAutofillService =
                state.serviceStatus == AutofillServiceStatus.EnabledByOurService,
                androidSdkInt = Build.VERSION.SDK_INT,
                deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}",
                appVersion = appVersion(context),
                installedBrowsers = state.installedBrowsers,
                supportsInlineSuggestions = state.supportsInlineSuggestions
            )
        )
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        val chooser = Intent.createChooser(sendIntent, null)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(chooser) }
            .onFailure { PassLogger.w(TAG, "Could not share diagnostics") }
    }

    private fun appVersion(context: Context): String = runCatching {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName.orEmpty()
    }.getOrDefault("")
}
