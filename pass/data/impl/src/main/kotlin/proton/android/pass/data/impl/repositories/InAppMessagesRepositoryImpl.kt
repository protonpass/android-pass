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
import kotlinx.coroutines.flow.map
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.FlowUtils.oneShot
import proton.android.pass.data.api.repositories.InAppMessageStatus
import proton.android.pass.data.api.repositories.InAppMessagesRepository
import proton.android.pass.data.impl.extensions.toDomain
import proton.android.pass.data.impl.remote.inappmessages.RemoteInAppMessagesDataSource
import proton.android.pass.data.impl.responses.NotificationResponse
import proton.android.pass.domain.inappmessages.InAppMessage
import proton.android.pass.domain.inappmessages.InAppMessageId
import javax.inject.Inject

class InAppMessagesRepositoryImpl @Inject constructor(
    private val remote: RemoteInAppMessagesDataSource
) : InAppMessagesRepository {

    override fun observeUserMessages(userId: UserId): Flow<List<InAppMessage>> =
        oneShot { remote.fetchInAppMessages(userId) }
            .map(List<NotificationResponse>::toDomain)

    override suspend fun changeMessageStatus(
        userId: UserId,
        messageId: InAppMessageId,
        status: InAppMessageStatus
    ) {
        remote.changeMessageStatus(userId, messageId, status)
    }
}
