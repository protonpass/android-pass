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
 * Decides whether a browser is covered by our autofill compatibility handling.
 *
 * A browser is [BrowserAutofillCoverage.Ready] when it is declared in our autofill compatibility
 * list and the installed version is at or below the declared `maxLongVersionCode` (so compatibility
 * mode applies automatically). Otherwise the user likely needs to enable autofill in the browser,
 * so it is [BrowserAutofillCoverage.NeedsSetup].
 */
object BrowserAutofillCoverageResolver {

    fun resolve(
        browserVersionCode: Long,
        compatMaxVersionCode: Long?,
        hasBeenSeenWorking: Boolean
    ): BrowserAutofillCoverage = when {
        hasBeenSeenWorking -> BrowserAutofillCoverage.Working
        compatMaxVersionCode != null && browserVersionCode <= compatMaxVersionCode ->
            BrowserAutofillCoverage.Ready
        else -> BrowserAutofillCoverage.NeedsSetup
    }
}
