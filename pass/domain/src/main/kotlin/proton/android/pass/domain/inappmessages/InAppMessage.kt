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

import kotlinx.datetime.Instant
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some

@JvmInline
value class InAppMessageId(val value: String)

@JvmInline
value class InAppMessageKey(val value: String)

data class InAppMessage(
    val id: InAppMessageId,
    val key: InAppMessageKey,
    val mode: InAppMessageMode,
    val title: String,
    val message: Option<String>,
    val imageUrl: Option<String>,
    val cta: Option<InAppMessageCTA>,
    val state: InAppMessageStatus,
    val range: InAppMessageRange,
    val userId: UserId
)

enum class InAppMessageStatus(val value: Int) {
    Unread(0),
    Read(1),
    Dismissed(2),
    Unknown(Integer.MAX_VALUE)
    ;

    companion object {
        fun fromValue(value: Int): InAppMessageStatus = entries.find { it.value == value } ?: Unknown
    }
}

data class InAppMessageCTA(
    val text: String,
    val route: String,
    val type: InAppMessageCTAType
)

enum class InAppMessageCTAType(val value: String) {
    Internal("internal_navigation"),
    External("external_link"),
    Unknown("unknown")
    ;

    companion object {
        fun fromValue(value: String): InAppMessageCTAType = entries.find { it.value == value } ?: Unknown
    }
}

data class InAppMessageRange(
    val start: Instant,
    val end: Option<Instant>
) {
    private val isClosedRange: Boolean
        get() = end is Some

    fun inRange(now: Instant): Boolean = if (isClosedRange) {
        now in start..(end as Some).value
    } else {
        now >= start
    }
}

enum class InAppMessageMode(val value: Int) {
    Banner(0),
    Modal(1),
    Unknown(Integer.MAX_VALUE)
    ;

    companion object {
        fun fromValue(value: Int): InAppMessageMode = entries.find { it.value == value } ?: Unknown
    }
}
