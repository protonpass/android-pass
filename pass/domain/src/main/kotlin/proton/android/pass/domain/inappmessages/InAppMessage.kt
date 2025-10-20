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

@JvmInline
value class InAppMessageId(val value: String)

@JvmInline
value class InAppMessageKey(val value: String)

sealed class InAppMessage {
    abstract val id: InAppMessageId
    abstract val key: InAppMessageKey
    abstract val priority: Int
    abstract val title: String
    abstract val message: Option<String>
    abstract val imageUrl: Option<String>
    abstract val cta: Option<InAppMessageCTA>
    abstract val state: InAppMessageStatus
    abstract val range: InAppMessageRange
    abstract val userId: UserId

    data class Banner(
        override val id: InAppMessageId,
        override val key: InAppMessageKey,
        override val priority: Int,
        override val title: String,
        override val message: Option<String>,
        override val imageUrl: Option<String>,
        override val cta: Option<InAppMessageCTA>,
        override val state: InAppMessageStatus,
        override val range: InAppMessageRange,
        override val userId: UserId
    ) : InAppMessage()

    data class Modal(
        override val id: InAppMessageId,
        override val key: InAppMessageKey,
        override val priority: Int,
        override val title: String,
        override val message: Option<String>,
        override val imageUrl: Option<String>,
        override val cta: Option<InAppMessageCTA>,
        override val state: InAppMessageStatus,
        override val range: InAppMessageRange,
        override val userId: UserId
    ) : InAppMessage()

    data class Promo(
        override val id: InAppMessageId,
        override val key: InAppMessageKey,
        override val priority: Int,
        override val title: String,
        override val message: Option<String>,
        override val imageUrl: Option<String>,
        override val cta: Option<InAppMessageCTA>,
        override val state: InAppMessageStatus,
        override val range: InAppMessageRange,
        override val userId: UserId,
        val promoContents: InAppMessagePromoContents
    ) : InAppMessage()
}

const val STATUS_UNREAD = 0
const val STATUS_READ = 1
const val STATUS_DISMISSED = 2

const val MODE_BANNER = 0
const val MODE_MODAL = 1
const val MODE_PROMO = 2

enum class InAppMessageStatus(val value: Int) {
    Unread(STATUS_UNREAD),
    Read(STATUS_READ),
    Dismissed(STATUS_DISMISSED),
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
)


data class InAppMessagePromoContents(
    val startMinimised: Boolean,
    val closePromoText: String,
    val lightThemeContents: InAppMessagePromoThemedContents,
    val darkThemeContents: InAppMessagePromoThemedContents
)

data class InAppMessagePromoThemedContents(
    val backgroundImageUrl: String,
    val contentImageUrl: String,
    val closePromoTextColor: String
)
