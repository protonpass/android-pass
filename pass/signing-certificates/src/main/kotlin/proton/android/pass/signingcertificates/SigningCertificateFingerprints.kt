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

package proton.android.pass.signingcertificates

import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.SigningInfo
import android.os.Build
import java.security.MessageDigest

object SigningCertificateFingerprints {

    fun of(context: Context, packageName: String): Set<String> = runCatching {
        val certificateBytes = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            context.packageManager
                .getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
                .signingInfo
                ?.let(::certificateBytesOf)
                .orEmpty()
        } else {
            @Suppress("DEPRECATION")
            context.packageManager
                .getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
                .signatures
                ?.map { it.toByteArray() }
                .orEmpty()
        }

        fingerprintsOf(certificateBytes)
    }.getOrDefault(emptySet())

    fun of(signingInfo: SigningInfo): Set<String> = fingerprintsOf(certificateBytesOf(signingInfo))

    fun fingerprintsOf(certificateBytes: List<ByteArray>): Set<String> {
        val messageDigest = MessageDigest.getInstance("SHA-256")
        return certificateBytes.map { certificate ->
            messageDigest.reset()
            messageDigest.digest(certificate).joinToString(":") { byte -> "%02X".format(byte) }
        }.toSet()
    }

    fun hasSingleSigner(signingInfo: SigningInfo): Boolean =
        hasSingleSigner(signingInfo.apkContentsSigners.map { it.toByteArray() }, signingInfo.hasMultipleSigners())

    fun hasSingleSigner(signingCertificateBytes: List<ByteArray>, hasMultipleSigners: Boolean): Boolean =
        !hasMultipleSigners && signingCertificateBytes.size == 1

    private fun certificateBytesOf(signingInfo: SigningInfo): List<ByteArray> = if (signingInfo.hasMultipleSigners()) {
        signingInfo.apkContentsSigners.map { it.toByteArray() }
    } else {
        (signingInfo.signingCertificateHistory ?: signingInfo.apkContentsSigners)
            .map { it.toByteArray() }
    }
}
