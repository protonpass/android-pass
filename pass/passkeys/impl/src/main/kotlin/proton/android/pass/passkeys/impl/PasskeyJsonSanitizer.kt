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

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

internal object PasskeyJsonSanitizer {

    private val sanitizers: List<SiteJsonSanitizer> = listOf(
        EqualSignSanitizer,
        PaypalSanitizer,
        EbaySanitizer
    )

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

private object EqualSignSanitizer : SiteJsonSanitizer {
    override fun shouldSanitize(json: String): Boolean = json.contains("\\u003d")

    override fun sanitize(json: String): String = json.replace("\\u003d", "=")
}

private object PaypalSanitizer : SiteJsonSanitizer {
    override fun shouldSanitize(json: String): Boolean = json.contains("paypal")

    /**
     * Paypal has the following special cases:
     * 1. Sends the timeout as a float instead of an integer. We convert it to an integer.
     */
    override fun sanitize(json: String): String {
        return when (val parsed = Json.parseToJsonElement(json)) {
            is JsonObject -> {
                val timeout = parsed["timeout"] ?: return json
                val timeoutValue = timeout.jsonPrimitive.floatOrNull
                if (timeoutValue != null) {
                    val editableJson = parsed.toMutableMap()
                    editableJson.replace("timeout", JsonPrimitive(timeoutValue.toInt()))
                    val asJson = JsonObject(editableJson)
                    Json.encodeToString(asJson)
                } else {
                    json
                }
            }

            else -> json
        }
    }
}

private object EbaySanitizer : SiteJsonSanitizer {
    override fun shouldSanitize(json: String): Boolean = json.contains("ebay.")

    /**
     * Ebay has the following special cases:
     * 1. Sends the algorithm as a string instead of a number. We convert it to a number.
     * 2. Sends a -1 algorithm while is not defined in the spec. We remove it.
     */
    override fun sanitize(json: String): String {
        return when (val parsed = Json.parseToJsonElement(json)) {
            is JsonObject -> {
                val pubKeyCredParams = parsed["pubKeyCredParams"]?.jsonArray ?: return json
                if (pubKeyCredParams.isEmpty()) return json

                val params = pubKeyCredParams.mapNotNull { param ->
                    val alg = param.jsonObject["alg"]?.jsonPrimitive?.content
                        ?: return@mapNotNull null
                    val algValue = alg.toIntOrNull() ?: return@mapNotNull null
                    if (algValue == -1) return@mapNotNull null

                    val editableParam = param.jsonObject.toMutableMap()
                    editableParam.replace("alg", JsonPrimitive(algValue))
                    JsonObject(editableParam)
                }

                val editableJson = parsed.toMutableMap()
                editableJson.replace("pubKeyCredParams", JsonArray(params))
                val asJson = JsonObject(editableJson)
                Json.encodeToString(asJson)
            }

            else -> json
        }
    }

}
