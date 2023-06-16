/*
 * Copyright (c) 2023 Proton AG
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

package proton.android.pass.data.api.url

import proton.android.pass.common.api.flatMap
import java.net.URI
import java.net.URISyntaxException

object UrlSanitizer {

    private val TRAILING_DOTS_REGEX = Regex("\\.+$")

    private val FORBIDDEN_SCHEMES = listOf(
        "javascript:",
        "data:",
        "file:",
        "about:",
        "blob:"
    )

    fun sanitize(url: String): Result<String> {
        if (url.isBlank()) return Result.failure(IllegalArgumentException("url cannot be empty"))
        if (url.all { !it.isLetterOrDigit() })
            return Result.failure(IllegalArgumentException("url cannot be all symbols"))

        FORBIDDEN_SCHEMES.forEach {
            if (url.startsWith(it)) {
                return Result.failure(IllegalArgumentException("url cannot start with $it"))
            }
        }

        // If it doesn't have a scheme, add https://
        val urlWithScheme = if (!url.contains("://")) {
            "https://$url"
        } else {
            url
        }

        return try {
            val parsed = URI(urlWithScheme)
            if (parsed.host == null) return Result.failure(IllegalArgumentException("url cannot be parsed: [url=$url]"))

            val sanitizedHost = parsed.host.replace(TRAILING_DOTS_REGEX, "")
            val meaningfulSection = "${parsed.scheme}://${sanitizedHost}${parsed.path}"
            Result.success(meaningfulSection)
        } catch (e: URISyntaxException) {
            Result.failure(e)
        }
    }

    fun getProtocol(url: String): Result<String> = sanitizeAndParse(url).map { it.scheme }
    fun getDomain(url: String): Result<String> = sanitizeAndParse(url).map { it.host }

    private fun sanitizeAndParse(url: String): Result<URI> = sanitize(url).flatMap {
        try {
            Result.success(URI(it))
        } catch (e: URISyntaxException) {
            Result.failure(e)
        }
    }
}
