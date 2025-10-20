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

package proton.android.pass.data.impl.fakes

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.FlowUtils.testFlow
import proton.android.pass.data.impl.local.inappmessages.LocalInAppMessagesDataSource
import proton.android.pass.domain.inappmessages.InAppMessage
import proton.android.pass.domain.inappmessages.InAppMessageId

class FakeLocalInAppMessagesDataSource : LocalInAppMessagesDataSource {

    private val promoMessagesFlow = testFlow<InAppMessage.Promo?>()
    private val userMessageFlow = testFlow<InAppMessage>()
    private val topMessageFlow = testFlow<InAppMessage?>()
    private var storeMessagesResult: Result<Unit> = Result.success(Unit)
    private var updateMessageResult: Result<Unit> = Result.success(Unit)
    private var observePromoMessagesResult: Result<Unit> = Result.success(Unit)
    private var observeUserMessageResult: Result<Unit> = Result.success(Unit)
    private var observeTopMessageResult: Result<Unit> = Result.success(Unit)
    private var storedMessages: List<InAppMessage> = emptyList()


    fun emitPromoMessages(message: InAppMessage.Promo?) {
        promoMessagesFlow.tryEmit(message)
    }

    fun emitUserMessage(message: InAppMessage) {
        userMessageFlow.tryEmit(message)
    }

    fun emitTopMessage(message: InAppMessage?) {
        topMessageFlow.tryEmit(message)
    }

    fun setStoreMessagesResult(result: Result<Unit>) {
        storeMessagesResult = result
    }

    fun setUpdateMessageResult(result: Result<Unit>) {
        updateMessageResult = result
    }

    fun setObservePromoMessagesResult(result: Result<Unit>) {
        observePromoMessagesResult = result
    }

    fun setObserveUserMessageResult(result: Result<Unit>) {
        observeUserMessageResult = result
    }

    fun setObserveTopMessageResult(result: Result<Unit>) {
        observeTopMessageResult = result
    }

    fun getStoredMessages(): List<InAppMessage> = storedMessages

    override fun observePromoMinimizedUserMessages(userId: UserId, currentTimestamp: Long): Flow<InAppMessage.Promo?> {
        observePromoMessagesResult.getOrThrow()
        return promoMessagesFlow
    }

    override fun observeUserMessage(userId: UserId, id: InAppMessageId): Flow<InAppMessage> {
        observeUserMessageResult.getOrThrow()
        return userMessageFlow
    }

    override fun observeTopDeliverableUserMessage(userId: UserId, currentTimestamp: Long): Flow<InAppMessage?> {
        observeTopMessageResult.getOrThrow()
        return topMessageFlow
    }

    override suspend fun storeMessages(userId: UserId, messages: List<InAppMessage>) {
        storeMessagesResult.getOrThrow()
        storedMessages = messages
    }

    override suspend fun updateMessage(userId: UserId, message: InAppMessage) {
        updateMessageResult.getOrThrow()
    }
}
