/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.data.impl.usecases.inappmessages

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.repositories.InAppMessagesRepository
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.android.pass.data.api.usecases.inappmessages.ObserveInAppMessage
import proton.android.pass.domain.inappmessages.InAppMessage
import proton.android.pass.domain.inappmessages.InAppMessageId
import javax.inject.Inject

class ObserveInAppMessageImpl @Inject constructor(
    private val inAppMessagesRepository: InAppMessagesRepository,
    private val observeCurrentUser: ObserveCurrentUser
) : ObserveInAppMessage {

    override fun invoke(userId: UserId?, inAppMessageId: InAppMessageId): Flow<InAppMessage> =
        getUserId(userId).flatMapLatest { resolvedUserId ->
            inAppMessagesRepository.observeUserMessage(resolvedUserId, inAppMessageId)
        }

    private fun getUserId(userId: UserId?): Flow<UserId> = if (userId != null) {
        flowOf(userId)
    } else {
        observeCurrentUser().map { it.userId }
    }
}
