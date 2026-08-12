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

package proton.android.pass.autofill.autofillhealth.troubleshooting.data

import android.content.Context
import android.content.Intent
import proton.android.pass.log.api.PassLogger

/**
 * Opens a browser directly on its autofill settings screen.
 *
 * Chromium exposes `AutofillOptionsLauncher` on [Intent.ACTION_APPLICATION_PREFERENCES], which lands
 * on the "Autofill services" screen holding the "Autofill using another service" toggle.
 *
 * That action is the generic "app preferences" entry point though: any app may declare it and point
 * it anywhere, and Chromium forks may reroute or stub it (Edge declares the filter but its launcher
 * finishes without showing anything). So this is an allow-list of packages verified to land on the
 * autofill screen, not a resolve-and-hope. Resolution is still checked on top, to cover browser
 * versions predating the launcher.
 */
object BrowserAutofillSettingsLauncher {

    private const val TAG = "BrowserAutofillSettingsLauncher"

    private val packagesWithAutofillSettings: Set<String> = setOf(
        "com.android.chrome",
        "com.chrome.beta",
        "com.chrome.dev",
        "com.chrome.canary",
        "com.google.android.apps.chrome",
        "com.brave.browser",
        "com.brave.browser_beta",
        "com.brave.browser_default",
        "com.brave.browser_dev",
        "com.brave.browser_nightly",
        "com.vivaldi.browser",
        "com.vivaldi.browser.snapshot",
        "com.vivaldi.browser.sopranos"
    )

    fun canOpenAutofillSettings(context: Context, packageName: String): Boolean {
        if (packageName !in packagesWithAutofillSettings) return false
        return intentFor(packageName).resolveActivity(context.packageManager) != null
    }

    fun openAutofillSettings(context: Context, packageName: String): Boolean {
        if (!canOpenAutofillSettings(context, packageName)) return false
        return runCatching {
            context.startActivity(intentFor(packageName).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            true
        }.getOrElse { throwable ->
            PassLogger.w(TAG, throwable)
            PassLogger.w(TAG, "Could not open autofill settings for $packageName")
            false
        }
    }

    private fun intentFor(packageName: String): Intent =
        Intent(Intent.ACTION_APPLICATION_PREFERENCES).setPackage(packageName)
}
