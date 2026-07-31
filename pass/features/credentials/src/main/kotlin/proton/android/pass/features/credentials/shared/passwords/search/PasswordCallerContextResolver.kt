/*
 * Copyright (c) 2026 Proton AG
 * This file is part of Proton Pass.
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

package proton.android.pass.features.credentials.shared.passwords.search

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.credentials.provider.CallingAppInfo
import proton.android.pass.browserallowlist.api.PrivilegedBrowserAllowlistProvider
import proton.android.pass.signingcertificates.SigningCertificateFingerprints
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
internal sealed interface PasswordCallerContext {

    data class Native(
        val packageName: String,
        val certificateFingerprints: Set<String>
    ) : PasswordCallerContext

    /**
     * The framework validates this origin against the privileged-browser allowlist, so an
     * explicit password selection may be authorized without a native-app DAL check.
     */
    data class Browser(val origin: String) : PasswordCallerContext

}

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
internal class PasswordCallerContextResolver @Inject constructor(
    private val privilegedBrowserAllowlistProvider: PrivilegedBrowserAllowlistProvider,
    private val passwordOriginResolver: PasswordOriginResolver
) {

    internal fun resolve(callingAppInfo: CallingAppInfo): PasswordCallerContext? {
        if (callingAppInfo.isOriginPopulated()) {
            return getBrowserOrigin(callingAppInfo)
                ?.let(passwordOriginResolver::canonicalizeLoginUrl)
                ?.let(PasswordCallerContext::Browser)
        }

        val packageName = callingAppInfo.packageName.takeIf(String::isNotBlank) ?: return null
        val certificateFingerprints = runCatching {
            SigningCertificateFingerprints.of(callingAppInfo.signingInfo)
        }.getOrNull()?.takeIf(Set<String>::isNotEmpty) ?: return null

        return PasswordCallerContext.Native(
            packageName = packageName,
            certificateFingerprints = certificateFingerprints
        )
    }

    private fun getBrowserOrigin(callingAppInfo: CallingAppInfo): String? = runCatching {
        callingAppInfo.getOrigin(privilegedBrowserAllowlistProvider.json)
    }.getOrNull()

}
