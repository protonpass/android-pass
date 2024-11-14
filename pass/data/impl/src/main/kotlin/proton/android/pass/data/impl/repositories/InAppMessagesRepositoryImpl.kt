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

import android.content.Context
import coil.ImageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.repositories.InAppMessagesRepository
import proton.android.pass.data.impl.extensions.toDomain
import proton.android.pass.data.impl.local.inappmessages.LocalInAppMessagesDataSource
import proton.android.pass.data.impl.remote.inappmessages.RemoteInAppMessagesDataSource
import proton.android.pass.domain.inappmessages.InAppMessage
import proton.android.pass.domain.inappmessages.InAppMessageId
import proton.android.pass.domain.inappmessages.InAppMessageStatus
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

class InAppMessagesRepositoryImpl @Inject constructor(
    private val remote: RemoteInAppMessagesDataSource,
    private val local: LocalInAppMessagesDataSource,
    @ApplicationContext private val context: Context
) : InAppMessagesRepository {

    override fun observeDeliverableUserMessages(userId: UserId, currentTimestamp: Long): Flow<List<InAppMessage>> =
        local.observeDeliverableUserMessages(userId, currentTimestamp)
            .onStart { refreshUserMessages(userId) }
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

                preloadImages(context, allMessages.mapNotNull { it.imageUrl.value() }.toSet())
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

        val updatedMessage = local.observeUserMessage(userId, messageId).first()
            .copy(state = status)

        local.updateMessage(userId, updatedMessage)
    }

    override fun observeUserMessage(userId: UserId, inAppMessageId: InAppMessageId): Flow<InAppMessage> =
        local.observeUserMessage(userId, inAppMessageId)
            .distinctUntilChanged()

    private fun preloadImages(context: Context, imageUrls: Set<String>) {
        val imageLoader = ImageLoader.Builder(context)
            .diskCachePolicy(CachePolicy.ENABLED)
            .build()
        imageUrls.forEach { url ->
            val request = ImageRequest.Builder(context)
                .data(url)
                .build()
            imageLoader.enqueue(request)
        }
    }

    companion object {
        private const val TAG = "InAppMessagesRepositoryImpl"
    }
}
