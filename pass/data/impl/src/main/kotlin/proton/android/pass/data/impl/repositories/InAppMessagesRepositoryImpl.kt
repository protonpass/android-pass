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

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.first
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.repositories.InAppMessagesRepository
import proton.android.pass.data.impl.extensions.toDomain
import proton.android.pass.data.impl.local.inappmessages.LocalInAppMessagesDataSource
import proton.android.pass.data.impl.remote.inappmessages.RemoteInAppMessagesDataSource
import proton.android.pass.data.impl.utils.ImagePreloader
import proton.android.pass.domain.inappmessages.InAppMessage
import proton.android.pass.domain.inappmessages.InAppMessageId
import proton.android.pass.domain.inappmessages.InAppMessageStatus
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

class InAppMessagesRepositoryImpl @Inject constructor(
    private val remote: RemoteInAppMessagesDataSource,
    private val local: LocalInAppMessagesDataSource,
    private val imagePreloader: ImagePreloader
) : InAppMessagesRepository {

    override fun observePromoMinimizedUserMessages(userId: UserId, currentTimestamp: Long): Flow<InAppMessage.Promo?> =
        local.observePromoMinimizedUserMessages(userId, currentTimestamp)
            .distinctUntilChanged()

    override fun observeTopDeliverableUserMessage(
        userId: UserId,
        currentTimestamp: Long,
        refreshOnStart: Boolean
    ): Flow<InAppMessage?> = local.observeTopDeliverableUserMessage(userId, currentTimestamp)
        .onStart { if (refreshOnStart) refreshUserMessages(userId) }
        .distinctUntilChanged()

    override suspend fun refreshUserMessages(userId: UserId) {
        coroutineScope {
            val allMessages = mutableListOf<InAppMessage>()
            var lastID: String? = null
            runCatching {
                do {
                    ensureActive()
                    val response =
                        remote.fetchInAppMessages(userId = userId, lastToken = lastID)
                    val newMessages = response.list.map { it.toDomain(userId) }
                    allMessages.addAll(newMessages)
                    lastID = if (response.list.isNotEmpty()) response.lastID else null
                } while (lastID != null)

                local.storeMessages(userId, allMessages)

                val promoImages = allMessages
                    .filterIsInstance<InAppMessage.Promo>()
                    .flatMap { promo ->
                        listOf(
                            promo.promoContents.lightThemeContents.backgroundImageUrl,
                            promo.promoContents.lightThemeContents.contentImageUrl,
                            promo.promoContents.darkThemeContents.backgroundImageUrl,
                            promo.promoContents.darkThemeContents.contentImageUrl
                        )
                    }
                val regularImages = allMessages.mapNotNull { message ->
                    when (message) {
                        is InAppMessage.Banner -> message.imageUrl.value()
                        is InAppMessage.Modal -> message.imageUrl.value()
                        is InAppMessage.Promo -> message.imageUrl.value()
                    }
                }
                imagePreloader.preloadImages((promoImages + regularImages).toSet())
            }.onFailure {
                PassLogger.w(TAG, "Failed to fetch in-app messages for user $userId")
                PassLogger.w(TAG, it)
            }
        }

    }

    override suspend fun changeMessageStatus(
        userId: UserId,
        messageId: InAppMessageId,
        status: InAppMessageStatus
    ) {
        remote.changeMessageStatus(userId, messageId, status)

        val originalMessage = local.observeUserMessage(userId, messageId).first()
        val updatedMessage = when (originalMessage) {
            is InAppMessage.Banner -> originalMessage.copy(state = status)
            is InAppMessage.Modal -> originalMessage.copy(state = status)
            is InAppMessage.Promo -> originalMessage.copy(state = status)
        }

        local.updateMessage(userId, updatedMessage)
    }

    override fun observeUserMessage(userId: UserId, inAppMessageId: InAppMessageId): Flow<InAppMessage> =
        local.observeUserMessage(userId, inAppMessageId)
            .distinctUntilChanged()


    companion object {
        private const val TAG = "InAppMessagesRepositoryImpl"
    }
}
