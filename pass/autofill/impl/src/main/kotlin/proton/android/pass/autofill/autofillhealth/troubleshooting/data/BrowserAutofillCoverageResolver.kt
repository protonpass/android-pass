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

import proton.android.pass.autofill.ThirdPartyMode

/**
 * Decides whether a browser is covered by our autofill compatibility handling.
 *
 * [ThirdPartyMode.Disabled] is our only authoritative negative signal, but it only breaks autofill
 * for browsers the platform compatibility mode does not cover. Chrome dropped compatibility mode, so
 * third-party mode off really means broken there, and that outranks the sticky `hasBeenSeenWorking`
 * flag which would otherwise keep the browser green forever. Browsers still under their declared
 * `maxLongVersionCode` keep working through compatibility mode whatever that toggle says, so the
 * signal must not demote them.
 *
 * Failing that the user likely needs to turn autofill on in the browser, so it is
 * [BrowserAutofillCoverage.NeedsSetup].
 */
object BrowserAutofillCoverageResolver {

    fun resolve(
        browserVersionCode: Long,
        compatMaxVersionCode: Long?,
        hasBeenSeenWorking: Boolean,
        thirdPartyMode: ThirdPartyMode = ThirdPartyMode.Unknown
    ): BrowserAutofillCoverage {
        val compatModeApplies =
            compatMaxVersionCode != null && browserVersionCode <= compatMaxVersionCode
        return when {
            thirdPartyMode == ThirdPartyMode.Disabled && !compatModeApplies ->
                BrowserAutofillCoverage.NeedsSetup
            hasBeenSeenWorking -> BrowserAutofillCoverage.Working
            thirdPartyMode == ThirdPartyMode.Enabled -> BrowserAutofillCoverage.Ready
            compatModeApplies -> BrowserAutofillCoverage.Ready
            else -> BrowserAutofillCoverage.NeedsSetup
        }
    }
}
