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

package proton.android.pass.data.impl.db

import androidx.room.TypeConverter
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import proton.android.pass.domain.breach.BreachAction
import proton.android.pass.domain.breach.BreachActionCode

@Serializable
private data class BreachActionSerializable(
    val name: String,
    val code: String,
    val url: String?
)

class BreachTypeConverters {
    // Note: List<String> converters are provided by CommonConverters, so we don't duplicate them here

    @TypeConverter
    fun fromBreachActionList(value: String?): List<BreachAction>? {
        return value?.let { json ->
            val serializableList: List<BreachActionSerializable> = Json.decodeFromString(json)
            serializableList.map { serializable ->
                BreachAction(
                    name = serializable.name,
                    code = BreachActionCode.from(serializable.code),
                    url = serializable.url
                )
            }
        }
    }

    @TypeConverter
    fun toBreachActionList(list: List<BreachAction>?): String? {
        return list?.let { actions ->
            val serializableList = actions.map { action ->
                // Map enum to code string manually since code property is private
                val codeString = when (action.code) {
                    BreachActionCode.StayAlert -> "stay_alert"
                    BreachActionCode.PasswordSource -> "password_source"
                    BreachActionCode.PasswordExposed -> "password_exposed"
                    BreachActionCode.PasswordsAll -> "passwords_all"
                    BreachActionCode.Twofa -> "2fa"
                    BreachActionCode.Aliases -> "aliases"
                }
                BreachActionSerializable(
                    name = action.name,
                    code = codeString,
                    url = action.url
                )
            }
            Json.encodeToString(serializableList)
        }
    }
}

