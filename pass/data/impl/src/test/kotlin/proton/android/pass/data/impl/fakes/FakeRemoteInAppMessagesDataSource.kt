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

import me.proton.core.domain.entity.UserId
import proton.android.pass.data.impl.remote.inappmessages.RemoteInAppMessagesDataSource
import proton.android.pass.data.impl.responses.NotificationsResponse
import proton.android.pass.domain.inappmessages.InAppMessageId
import proton.android.pass.domain.inappmessages.InAppMessageStatus

class FakeRemoteInAppMessagesDataSource : RemoteInAppMessagesDataSource {

    private var fetchResult: Result<NotificationsResponse> =
        Result.failure(IllegalStateException("FakeRemoteInAppMessagesDataSource not initialized"))
    private var changeStatusResult: Result<Unit> = Result.success(Unit)

    fun setFetchResult(result: Result<NotificationsResponse>) {
        fetchResult = result
    }

    fun setChangeStatusResult(result: Result<Unit>) {
        changeStatusResult = result
    }

    override suspend fun fetchInAppMessages(userId: UserId, lastToken: String?): NotificationsResponse =
        fetchResult.getOrThrow()

    override suspend fun changeMessageStatus(
        userId: UserId,
        messageId: InAppMessageId,
        status: InAppMessageStatus
    ) {
        changeStatusResult.getOrThrow()
    }
}
