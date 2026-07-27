/*
 * Copyright (c) 2023-2026 Proton AG
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

package proton.android.pass.log.impl

import kotlin.text.Typography.ellipsis
import proton.android.pass.log.api.PassLogger
import proton.android.pass.log.api.PrivacySanitizer
import javax.inject.Inject

class PrivacySanitizerImpl @Inject constructor() : PrivacySanitizer {

    private val plainEmailRegex = Regex(
        pattern = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}",
        option = RegexOption.IGNORE_CASE
    )

    private val encodedEmailRegex = Regex(
        pattern = "[a-zA-Z0-9._%+-]+%40[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}",
        option = RegexOption.IGNORE_CASE
    )

    // Match domain values in URL query parameters (e.g., ?Domain=example.com)
    private val domainQueryParamRegex = Regex(
        pattern = "([?&]Domain)=[^&\\s]+",
        option = RegexOption.IGNORE_CASE
    )

    // Match share IDs (88 characters)
    private val shareIdRegex = Regex("[a-zA-Z0-9_=-]{88}")

    private val bearerTokenRegex = Regex(
        pattern = "Bearer\\s+[A-Za-z0-9._~+/-]+=*",
        option = RegexOption.IGNORE_CASE
    )

    override fun sanitize(message: String): String {
        var sanitized = message
        if (sanitized.contains(BEARER_TOKEN_PREFIX, ignoreCase = true)) {
            sanitized = sanitized.replace(bearerTokenRegex, "Bearer [REDACTED]")
        }
        if (sanitized.contains(ENCODED_EMAIL_MARKER)) {
            sanitized = sanitized.replace(encodedEmailRegex, EMAIL_REDACTED)
        }
        if (sanitized.contains(EMAIL_MARKER)) {
            sanitized = sanitized.replace(plainEmailRegex, EMAIL_REDACTED)
        }
        if (
            sanitized.contains(DOMAIN_QUERY_PARAM_PREFIX, ignoreCase = true) ||
            sanitized.contains(DOMAIN_QUERY_PARAM_SEPARATOR, ignoreCase = true)
        ) {
            sanitized = sanitized.replace(domainQueryParamRegex) { matchResult ->
                "${matchResult.groupValues[1]}=$DOMAIN_REDACTED"
            }
        }
        if (sanitized.length >= SHARE_ID_LENGTH) {
            sanitized = sanitizeIds(sanitized)
        }
        return sanitized
    }

    private fun sanitizeIds(message: String): String = runCatching {
        shareIdRegex.replace(message) { matchResult ->
            val id = matchResult.value
            "${id.take(ID_OFFSET)}$ellipsis${id.takeLast(ID_OFFSET)}"
        }
    }.onFailure {
        PassLogger.w(TAG, "Could not sanitise id")
        PassLogger.w(TAG, it)
    }.getOrDefault(message)

    companion object {
        private const val TAG = "PrivacySanitizerImpl"
        private const val EMAIL_REDACTED = "[EMAIL_REDACTED]"
        private const val DOMAIN_REDACTED = "[DOMAIN_REDACTED]"
        private const val BEARER_TOKEN_PREFIX = "Bearer"
        private const val ENCODED_EMAIL_MARKER = "%40"
        private const val EMAIL_MARKER = "@"
        private const val DOMAIN_QUERY_PARAM_PREFIX = "?Domain="
        private const val DOMAIN_QUERY_PARAM_SEPARATOR = "&Domain="
        private const val SHARE_ID_LENGTH = 88
        private const val ID_OFFSET = 4
    }
}
