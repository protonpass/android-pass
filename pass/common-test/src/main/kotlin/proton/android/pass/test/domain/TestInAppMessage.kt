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

package proton.android.pass.test.domain

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.domain.inappmessages.InAppMessage
import proton.android.pass.domain.inappmessages.InAppMessageCTA
import proton.android.pass.domain.inappmessages.InAppMessageCTAType
import proton.android.pass.domain.inappmessages.InAppMessageId
import proton.android.pass.domain.inappmessages.InAppMessageKey
import proton.android.pass.domain.inappmessages.InAppMessageMode
import proton.android.pass.domain.inappmessages.InAppMessageRange
import proton.android.pass.domain.inappmessages.InAppMessageStatus

object TestInAppMessage {

    fun create(
        id: String = "default-id",
        mode: InAppMessageMode = InAppMessageMode.Banner,
        title: String = "Default Title",
        message: Option<String> = None,
        imageUrl: Option<String> = None,
        cta: Option<InAppMessageCTA> = None,
        state: InAppMessageStatus = InAppMessageStatus.Unread,
        range: InAppMessageRange = createInAppMessageRange(),
        userId: UserId = UserId("default-user-id"),
        priority: Int = 1
    ): InAppMessage = InAppMessage(
        id = InAppMessageId(id),
        key = InAppMessageKey("default-key"),
        mode = mode,
        title = title,
        message = message,
        imageUrl = imageUrl,
        cta = cta,
        state = state,
        range = range,
        userId = userId,
        priority = priority,
        promoContents = None
    )

    fun createInAppMessageRange(start: Instant = Clock.System.now(), end: Option<Instant> = None): InAppMessageRange =
        InAppMessageRange(start = start, end = end)

    fun createInAppMessageCTA(
        text: String = "Default CTA Text",
        route: String = "default/route",
        type: InAppMessageCTAType = InAppMessageCTAType.Internal
    ): InAppMessageCTA = InAppMessageCTA(
        text = text,
        route = route,
        type = type
    )
}
