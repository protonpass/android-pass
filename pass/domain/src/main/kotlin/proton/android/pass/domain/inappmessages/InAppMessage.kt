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

package proton.android.pass.domain.inappmessages

import proton.android.pass.common.api.Option

enum class InAppMessageMode(val value: Int) {
    Banner(0),
    Modal(1),
    Unknown(Integer.MAX_VALUE)
    ;

    companion object {
        fun fromValue(value: Int): InAppMessageMode = entries.find { it.value == value } ?: Unknown
    }
}

@JvmInline
value class InAppMessageId(val value: String)

@JvmInline
value class InAppMessageCTARoute(val value: String)

data class InAppMessage(
    val id: InAppMessageId,
    val mode: InAppMessageMode,
    val title: String,
    val message: Option<String>,
    val imageUrl: Option<String>,
    val ctaRoute: Option<InAppMessageCTARoute>,
    val ctaText: Option<String>
)
