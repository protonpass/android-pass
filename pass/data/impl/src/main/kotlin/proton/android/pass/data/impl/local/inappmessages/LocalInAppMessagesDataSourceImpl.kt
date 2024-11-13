/*
 * Copyright (c) 2023-2024 Proton AG
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

package proton.android.pass.data.impl.local.inappmessages

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.toOption
import proton.android.pass.data.impl.db.PassDatabase
import proton.android.pass.data.impl.db.entities.InAppMessageEntity
import proton.android.pass.domain.inappmessages.InAppMessage
import proton.android.pass.domain.inappmessages.InAppMessageCTA
import proton.android.pass.domain.inappmessages.InAppMessageCTAType
import proton.android.pass.domain.inappmessages.InAppMessageId
import proton.android.pass.domain.inappmessages.InAppMessageKey
import proton.android.pass.domain.inappmessages.InAppMessageMode
import proton.android.pass.domain.inappmessages.InAppMessageRange
import proton.android.pass.domain.inappmessages.InAppMessageStatus
import javax.inject.Inject

class LocalInAppMessagesDataSourceImpl @Inject constructor(
    private val database: PassDatabase
) : LocalInAppMessagesDataSource {

    override fun observeDeliverableUserMessages(userId: UserId, currentTimestamp: Long): Flow<List<InAppMessage>> =
        database.inAppMessagesDao()
            .observeDeliverableUserMessages(userId.id, currentTimestamp)
            .map { entities -> entities.map(InAppMessageEntity::toDomain) }

    override fun observeUserMessage(userId: UserId, id: InAppMessageId): Flow<InAppMessage> =
        database.inAppMessagesDao().observeUserMessage(userId.id, id.value)
            .map(InAppMessageEntity::toDomain)

    override suspend fun storeMessages(userId: UserId, messages: List<InAppMessage>) {
        database.inTransaction(name = "storeMessages") {
            database.inAppMessagesDao().deleteAll(userId.id)
            database.inAppMessagesDao().insertOrUpdate(*messages.map(InAppMessage::toEntity).toTypedArray())
        }
    }

    override suspend fun updateMessage(userId: UserId, message: InAppMessage) {
        database.inAppMessagesDao().insertOrUpdate(message.toEntity())
    }
}

private fun InAppMessageEntity.toDomain(): InAppMessage = InAppMessage(
    id = InAppMessageId(id),
    key = InAppMessageKey(key),
    mode = InAppMessageMode.fromValue(mode),
    priority = priority,
    title = title,
    message = message.toOption(),
    imageUrl = imageUrl.toOption(),
    cta = if (ctaText != null && ctaRoute != null && ctaType != null) {
        Some(InAppMessageCTA(ctaText, ctaRoute, InAppMessageCTAType.fromValue(ctaType)))
    } else {
        None
    },
    state = InAppMessageStatus.fromValue(state),
    range = InAppMessageRange(
        start = Instant.fromEpochSeconds(rangeStart),
        end = rangeEnd?.let(Instant.Companion::fromEpochSeconds).toOption()
    ),
    userId = UserId(userId)
)

private fun InAppMessage.toEntity(): InAppMessageEntity = InAppMessageEntity(
    id = id.value,
    key = key.value,
    mode = mode.value,
    priority = priority,
    title = title,
    message = message.value(),
    imageUrl = imageUrl.value(),
    ctaText = cta.map(InAppMessageCTA::text).value(),
    ctaRoute = cta.map(InAppMessageCTA::route).value(),
    ctaType = cta.map(InAppMessageCTA::type).map(InAppMessageCTAType::value).value(),
    state = state.value,
    rangeStart = range.start.epochSeconds,
    rangeEnd = range.end.map(Instant::epochSeconds).value(),
    userId = userId.id
)
