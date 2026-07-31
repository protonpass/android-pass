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

package proton.android.pass.autofill.extensions

import kotlinx.coroutines.flow.first
import proton.android.pass.domain.entity.PackageInfo
import proton.android.pass.preferences.InternalSettingsRepository

enum class BrowserVerification {
    NOT_A_BROWSER,
    VERIFIED,
    UNVERIFIED
}

fun PackageInfo.verifyBrowser(allowedFingerprints: Set<String>): BrowserVerification {
    if (!packageName.isBrowser()) return BrowserVerification.NOT_A_BROWSER
    return if (allowedFingerprints.isNotEmpty() && hashes.any { it in allowedFingerprints }) {
        BrowserVerification.VERIFIED
    } else {
        BrowserVerification.UNVERIFIED
    }
}

suspend fun InternalSettingsRepository.isTrustedAutofillPackage(packageInfo: PackageInfo): Boolean {
    val trustedFingerprints = getTrustedAutofillPackages().first()[packageInfo.packageName.value].orEmpty()
    return trustedFingerprints.isNotEmpty() && packageInfo.hashes.any { it in trustedFingerprints }
}
