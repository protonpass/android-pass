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

package proton.android.pass.features.itemcreate.creditcard

import proton.android.pass.common.api.CommonRegex.NON_DIGIT_REGEX

object ExpirationDateProtoMapper {

    private const val EXPIRATION_DATE_LENGTH = 4
    private val ProtoRegex = Regex("""^\d{4}-\d{2}$""")

    fun toProto(value: String): String {
        if (value.length != EXPIRATION_DATE_LENGTH && !NON_DIGIT_REGEX.matches(value)) return ""
        val month = value.substring(0, 2)
        val year = value.substring(2)
        return "20$year-$month"
    }

    fun fromProto(value: String): String {
        if (!value.matches(ProtoRegex)) return ""
        return value.split("-").let { it[1] + it[0].substring(2) }
    }
}
