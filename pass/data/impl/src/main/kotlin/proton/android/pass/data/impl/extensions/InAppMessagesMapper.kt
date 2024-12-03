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

package proton.android.pass.data.impl.extensions

import kotlinx.datetime.Instant
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.toOption
import proton.android.pass.data.impl.responses.CtaResponse
import proton.android.pass.data.impl.responses.NotificationResponse
import proton.android.pass.domain.inappmessages.InAppMessage
import proton.android.pass.domain.inappmessages.InAppMessageCTA
import proton.android.pass.domain.inappmessages.InAppMessageCTAType
import proton.android.pass.domain.inappmessages.InAppMessageId
import proton.android.pass.domain.inappmessages.InAppMessageKey
import proton.android.pass.domain.inappmessages.InAppMessageMode
import proton.android.pass.domain.inappmessages.InAppMessageRange
import proton.android.pass.domain.inappmessages.InAppMessageStatus

fun List<NotificationResponse>.toDomain(userId: UserId): List<InAppMessage> = this.map { it.toDomain(userId) }

fun NotificationResponse.toDomain(userId: UserId): InAppMessage = InAppMessage(
    id = InAppMessageId(this.id),
    key = InAppMessageKey(this.notificationKey),
    userId = userId,
    mode = InAppMessageMode.fromValue(this.content.displayType),
    title = this.content.title,
    message = this.content.message.toOption(),
    imageUrl = this.content.imageUrl.toOption(),
    cta = this.content.cta.toOption().map(CtaResponse::toDomain),
    state = InAppMessageStatus.fromValue(this.state),
    priority = this.priority,
    range = InAppMessageRange(
        start = Instant.fromEpochSeconds(this.startTime),
        end = this.endTime?.let(Instant.Companion::fromEpochSeconds).toOption()
    )
)

private fun CtaResponse.toDomain(): InAppMessageCTA = InAppMessageCTA(
    text = this.text,
    route = this.ref,
    type = InAppMessageCTAType.fromValue(this.type)
)

