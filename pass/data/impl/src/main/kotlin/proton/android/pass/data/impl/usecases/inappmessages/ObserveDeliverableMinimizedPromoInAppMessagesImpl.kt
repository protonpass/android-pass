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
import kotlinx.datetime.Clock
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.repositories.InAppMessagesRepository
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.android.pass.data.api.usecases.inappmessages.ObserveDeliverableMinimizedPromoInAppMessages
import proton.android.pass.domain.inappmessages.InAppMessage
import javax.inject.Inject

class ObserveDeliverableMinimizedPromoInAppMessagesImpl @Inject constructor(
    private val observeCurrentUser: ObserveCurrentUser,
    private val inAppMessagesRepository: InAppMessagesRepository,
    private val clock: Clock
) : ObserveDeliverableMinimizedPromoInAppMessages {

    override fun invoke(userId: UserId?): Flow<InAppMessage.Promo?> =
        InAppMessageUtils.getUserId(userId, observeCurrentUser).flatMapLatest { resolvedUserId ->
            inAppMessagesRepository.observePromoMinimizedUserMessages(
                userId = resolvedUserId,
                currentTimestamp = clock.now().epochSeconds
            )
        }
}
