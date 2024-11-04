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

package proton.android.pass.data.impl.remote.inappmessages

import me.proton.core.domain.entity.UserId
import me.proton.core.network.data.ApiProvider
import proton.android.pass.data.api.repositories.InAppMessageStatus
import proton.android.pass.data.impl.api.PasswordManagerApi
import proton.android.pass.data.impl.requests.ChangeNotificationStatusRequest
import proton.android.pass.data.impl.responses.NotificationResponse
import proton.android.pass.domain.inappmessages.InAppMessageId
import javax.inject.Inject

class RemoteInAppMessagesDataSourceImpl @Inject constructor(
    private val api: ApiProvider
) : RemoteInAppMessagesDataSource {

    override suspend fun fetchInAppMessages(userId: UserId, lastToken: String?): List<NotificationResponse> =
        api.get<PasswordManagerApi>(userId)
            .invoke { fetchUserNotifications(lastToken) }
            .valueOrThrow
            .response
            .list

    override suspend fun changeMessageStatus(
        userId: UserId,
        messageId: InAppMessageId,
        status: InAppMessageStatus
    ) {
        val request = ChangeNotificationStatusRequest(status.value)
        api.get<PasswordManagerApi>(userId)
            .invoke { changeNotificationStatus(messageId.value, request) }
            .valueOrThrow
    }

}
