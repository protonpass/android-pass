/*
 * Copyright (c) 2024-2026 Proton AG
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

package proton.android.pass.data.impl

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.content.pm.SigningInfo
import android.os.Build
import proton.android.pass.log.api.PassLogger
import java.security.MessageDigest

object AppCertificate {

    private const val TAG = "AppCertificate"

    /**
     * Normalizes SHA-256 cert fingerprints for comparison. Asset Links JSON and the OS may use
     * different casing, optional colon separators, spaces, or a `sha256:` prefix.
     */
    fun normalizeSha256Fingerprint(value: String): String = value
        .trim()
        .lowercase()
        .removePrefix("sha256:")
        .replace(":", "")
        .replace(" ", "")

    fun getAppSigningCertificates(context: Context, packageName: String): List<String> = try {
        val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            context.packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
        } else {
            context.packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
        }

        collectSigningSignatures(packageInfo)
            .map { signature ->
                val md = MessageDigest.getInstance("SHA-256")
                val digest = md.digest(signature.toByteArray())
                digest.joinToString("") { "%02x".format(it) }
            }
            .distinct()
    } catch (e: PackageManager.NameNotFoundException) {
        PassLogger.w(TAG, "Package not found during fingerprint lookup, DAL matching skipped")
        PassLogger.w(TAG, e)
        emptyList()
    }

    private fun collectSigningSignatures(packageInfo: PackageInfo): List<Signature> {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            return packageInfo.signatures?.filterNotNull().orEmpty()
        }
        val signingInfo: SigningInfo = packageInfo.signingInfo ?: return emptyList()
        val apkSigners = signingInfo.apkContentsSigners?.filterNotNull().orEmpty()
        val historySigners = signingInfo.signingCertificateHistory?.filterNotNull().orEmpty()
        // Merge lineage certs: Play/App updates can rotate the signing key while `assetlinks.json`
        // (and our DB) still list older SHA-256 entries. Matching is only against DB rows we stored
        // from TLS-fetched `/.well-known/assetlinks.json` for this package.
        return buildList {
            apkSigners.toCollection(this)
            historySigners.toCollection(this)
        }.distinct()
    }
}
