package proton.android.pass.data.impl.util

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

object DimensionsSerializer {

    fun serialize(dimensions: Map<String, String>): String =
        buildJsonObject {
            dimensions.forEach { key, value ->
                put(key, JsonPrimitive(value))
            }
        }.toString()

    fun deserialize(dimensions: String): Map<String, JsonPrimitive> =
        Json.decodeFromString(dimensions)
}
