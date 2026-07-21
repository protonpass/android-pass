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

/**
 * Best-effort coverage status for autofill in a given browser.
 *
 * Android does not expose whether autofill is actually enabled inside a browser, so this is a
 * heuristic based on whether the installed browser version is handled by our autofill
 * compatibility list ([BrowserAutofillCoverage.Ready]) or whether the user likely needs to turn
 * autofill on in the browser settings ([BrowserAutofillCoverage.NeedsSetup]).
 */
enum class BrowserAutofillCoverage {
    /** Autofill has actually been observed working in this browser. */
    Working,

    /** Covered by our compatibility list; autofill should work without browser setup. */
    Ready,

    /** Likely needs autofill turned on in the browser settings. */
    NeedsSetup
}

data class BrowserInfo(
    val packageName: String,
    val label: String,
    val coverage: BrowserAutofillCoverage
)

interface InstalledBrowsersProvider {
    suspend fun getInstalledBrowsers(): List<BrowserInfo>
}
