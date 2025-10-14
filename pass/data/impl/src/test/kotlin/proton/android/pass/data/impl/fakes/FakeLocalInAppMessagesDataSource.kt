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

    private val deliverableMessagesFlow = testFlow<List<InAppMessage>>()
    private val promoMessagesFlow = testFlow<List<InAppMessage>>()
    private val userMessageFlow = testFlow<InAppMessage>()
    private var storeMessagesResult: Result<Unit> = Result.success(Unit)
    private var updateMessageResult: Result<Unit> = Result.success(Unit)
    private var observeDeliverableMessagesResult: Result<Unit> = Result.success(Unit)
    private var observePromoMessagesResult: Result<Unit> = Result.success(Unit)
    private var observeUserMessageResult: Result<Unit> = Result.success(Unit)
    private var storedMessages: List<InAppMessage> = emptyList()

    fun emitDeliverableMessages(messages: List<InAppMessage>) {
        deliverableMessagesFlow.tryEmit(messages)
    }

    fun emitPromoMessages(messages: List<InAppMessage>) {
        promoMessagesFlow.tryEmit(messages)
    }

    fun emitUserMessage(message: InAppMessage) {
        userMessageFlow.tryEmit(message)
    }

    fun setStoreMessagesResult(result: Result<Unit>) {
        storeMessagesResult = result
    }

    fun setUpdateMessageResult(result: Result<Unit>) {
        updateMessageResult = result
    }

    fun setObserveDeliverableMessagesResult(result: Result<Unit>) {
        observeDeliverableMessagesResult = result
    }

    fun setObservePromoMessagesResult(result: Result<Unit>) {
        observePromoMessagesResult = result
    }

    fun setObserveUserMessageResult(result: Result<Unit>) {
        observeUserMessageResult = result
    }

    fun getStoredMessages(): List<InAppMessage> = storedMessages

    override fun observeDeliverableUserMessages(userId: UserId, currentTimestamp: Long): Flow<List<InAppMessage>> {
        observeDeliverableMessagesResult.getOrThrow()
        return deliverableMessagesFlow
    }

    override fun observePromoUserMessages(userId: UserId, currentTimestamp: Long): Flow<List<InAppMessage>> {
        observePromoMessagesResult.getOrThrow()
        return promoMessagesFlow
    }

    override fun observeUserMessage(userId: UserId, id: InAppMessageId): Flow<InAppMessage> {
        observeUserMessageResult.getOrThrow()
        return userMessageFlow
    }

    override suspend fun storeMessages(userId: UserId, messages: List<InAppMessage>) {
        storeMessagesResult.getOrThrow()
        storedMessages = messages
    }

    override suspend fun updateMessage(userId: UserId, message: InAppMessage) {
        updateMessageResult.getOrThrow()
    }
}
