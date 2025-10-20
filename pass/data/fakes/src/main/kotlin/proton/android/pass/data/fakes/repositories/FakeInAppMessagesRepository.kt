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

package proton.android.pass.data.fakes.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.repositories.InAppMessagesRepository
import proton.android.pass.domain.inappmessages.InAppMessage
import proton.android.pass.domain.inappmessages.InAppMessageId
import proton.android.pass.domain.inappmessages.InAppMessageStatus
import javax.inject.Inject

class FakeInAppMessagesRepository @Inject constructor() : InAppMessagesRepository {

    private val messagesFlow = MutableStateFlow<Map<UserId, List<InAppMessage>>>(emptyMap())

    override fun observePromoMinimizedUserMessages(userId: UserId, currentTimestamp: Long): Flow<InAppMessage.Promo?> =
        messagesFlow.map {
            it[userId]?.filterIsInstance<InAppMessage.Promo>()?.firstOrNull()
        }

    override fun observeTopDeliverableUserMessage(
        userId: UserId,
        currentTimestamp: Long,
        refreshOnStart: Boolean
    ): Flow<InAppMessage?> = messagesFlow.map { it[userId]?.firstOrNull() }

    override suspend fun refreshUserMessages(userId: UserId) {
        // no-op
    }

    override suspend fun changeMessageStatus(
        userId: UserId,
        messageId: InAppMessageId,
        status: InAppMessageStatus
    ) {
        messagesFlow.value = messagesFlow.value.toMutableMap().apply {
            this[userId] = this[userId]?.map { message ->
                if (message.id == messageId) {
                    when (message) {
                        is InAppMessage.Banner -> message.copy(state = status)
                        is InAppMessage.Modal -> message.copy(state = status)
                        is InAppMessage.Promo -> message.copy(state = status)
                    }
                } else {
                    message
                }
            } ?: emptyList()
        }
    }

    override fun observeUserMessage(userId: UserId, inAppMessageId: InAppMessageId): Flow<InAppMessage> =
        messagesFlow.map { it[userId]?.find { it.id == inAppMessageId }!! }

    fun addMessage(userId: UserId, message: InAppMessage) {
        messagesFlow.value = messagesFlow.value.toMutableMap().apply {
            this[userId] = (this[userId] ?: emptyList()) + message
        }
    }
}
