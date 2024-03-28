/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.passkeys.impl

internal object PasskeyJsonSanitizer {

    private val sanitizers: List<SiteJsonSanitizer> = listOf(PaypalSanitizer)

    fun sanitize(input: String): String {
        var content = input
        for (sanitizer in sanitizers) {
            if (sanitizer.shouldSanitize(content)) {
                content = sanitizer.sanitize(content)
            }
        }

        return content
    }

}

private interface SiteJsonSanitizer {
    fun shouldSanitize(json: String): Boolean
    fun sanitize(json: String): String
}

private object PaypalSanitizer : SiteJsonSanitizer {
    override fun shouldSanitize(json: String): Boolean = json.contains("paypal")

    override fun sanitize(json: String): String = json
        .replace("1800000.0", "1800000")
        .replace("\\u003d", "=")

}
