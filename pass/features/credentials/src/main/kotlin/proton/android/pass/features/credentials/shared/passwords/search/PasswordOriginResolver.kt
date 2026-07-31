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

import java.net.URI
import java.util.Locale
import javax.inject.Inject

internal class PasswordOriginResolver @Inject constructor() {

    internal fun canonicalizeLoginUrl(url: String): String? = runCatching {
        URI(url).let { uri ->
            val host = uri.host ?: return null
            if (!uri.isSupportedOrigin(host)) return null
            "$HTTPS_SCHEME://${host.lowercase(Locale.ROOT)}"
        }
    }.getOrNull()

    private companion object {

        private const val HTTPS_SCHEME = "https"
        private const val NO_PORT = -1

        private const val MAX_DNS_LABEL_LENGTH = 63

        private fun URI.isSupportedOrigin(host: String): Boolean = listOf(
            scheme.equals(HTTPS_SCHEME, ignoreCase = true),
            userInfo == null,
            port == NO_PORT,
            isDnsHost(host)
        ).all { it }

        private fun isDnsHost(host: String): Boolean = host.any { it.isLetter() } && host.split('.').all(::isDnsLabel)

        private fun isDnsLabel(label: String): Boolean = label.length in 1..MAX_DNS_LABEL_LENGTH &&
            label.first().isLetterOrDigit() &&
            label.last().isLetterOrDigit() &&
            label.all { it.isLetterOrDigit() || it == '-' }

    }

}
