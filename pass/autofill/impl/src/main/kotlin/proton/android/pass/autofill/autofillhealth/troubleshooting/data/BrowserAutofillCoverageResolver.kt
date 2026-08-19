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
 * [ThirdPartyMode.Disabled] is our only authoritative negative signal and outranks everything else,
 * including the sticky `hasBeenSeenWorking` flag which would otherwise keep the browser green
 * forever. It cannot conflict with the compatibility list: only browsers shipping
 * `AutofillThirdPartyModeContentProvider` ever report it, and Chromium added that provider when it
 * dropped compatibility mode, so a browser answering [ThirdPartyMode.Disabled] is by construction
 * past the point where compatibility mode covered it. Its declared `maxLongVersionCode` may say
 * otherwise, but most entries carry a placeholder cap that no version ever exceeds, so the list is
 * not evidence of anything for these browsers. Versions predating the provider report
 * [ThirdPartyMode.Unknown] and still fall back to the compatibility list.
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
            thirdPartyMode == ThirdPartyMode.Disabled ->
                BrowserAutofillCoverage.NeedsSetup
            hasBeenSeenWorking -> BrowserAutofillCoverage.Working
            thirdPartyMode == ThirdPartyMode.Enabled -> BrowserAutofillCoverage.Ready
            compatModeApplies -> BrowserAutofillCoverage.Ready
            else -> BrowserAutofillCoverage.NeedsSetup
        }
    }
}
