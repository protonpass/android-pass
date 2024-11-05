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

package proton.android.pass.data.impl.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.repositories.InAppMessagesRepository
import proton.android.pass.data.impl.extensions.toDomain
import proton.android.pass.data.impl.remote.inappmessages.RemoteInAppMessagesDataSource
import proton.android.pass.domain.inappmessages.InAppMessage
import proton.android.pass.domain.inappmessages.InAppMessageId
import proton.android.pass.domain.inappmessages.InAppMessageStatus
import javax.inject.Inject

class InAppMessagesRepositoryImpl @Inject constructor(
    private val remote: RemoteInAppMessagesDataSource
) : InAppMessagesRepository {

    private val messageCache = MutableStateFlow<Map<UserId, List<InAppMessage>>>(emptyMap())

    override fun observeUserMessages(userId: UserId): Flow<List<InAppMessage>> = messageCache
        .map { it[userId] ?: emptyList() }
        .distinctUntilChanged()
        .onStart {
            if (!messageCache.value.containsKey(userId)) {
                val messages = remote.fetchInAppMessages(userId).toDomain(userId)
                messageCache.value += userId to messages
            }
        }

    override suspend fun changeMessageStatus(
        userId: UserId,
        messageId: InAppMessageId,
        status: InAppMessageStatus
    ) {
        remote.changeMessageStatus(userId, messageId, status)

        messageCache.value[userId]?.let { cachedMessages ->
            val updatedMessages = cachedMessages.map { message ->
                if (message.id == messageId) message.copy(state = status) else message
            }
            messageCache.value += userId to updatedMessages
        }
    }
}
