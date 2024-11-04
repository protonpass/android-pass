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

import proton.android.pass.common.api.toOption
import proton.android.pass.data.impl.responses.NotificationResponse
import proton.android.pass.domain.inappmessages.InAppMessage
import proton.android.pass.domain.inappmessages.InAppMessageCTARoute
import proton.android.pass.domain.inappmessages.InAppMessageId
import proton.android.pass.domain.inappmessages.InAppMessageMode

fun List<NotificationResponse>.toDomain(): List<InAppMessage> = this.map(NotificationResponse::toDomain)

fun NotificationResponse.toDomain(): InAppMessage = InAppMessage(
    id = InAppMessageId(this.id),
    mode = InAppMessageMode.fromValue(this.content.displayType),
    title = this.content.title,
    message = this.content.message.toOption(),
    imageUrl = this.content.imageUrl.toOption(),
    ctaRoute = this.content.cta.ref.toOption().map(::InAppMessageCTARoute),
    ctaText = this.content.cta.text.toOption()
)
