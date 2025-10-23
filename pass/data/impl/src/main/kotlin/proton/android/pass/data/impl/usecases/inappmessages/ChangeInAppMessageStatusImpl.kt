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

import kotlinx.datetime.Clock
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.repositories.InAppMessagesRepository
import proton.android.pass.data.api.usecases.inappmessages.ChangeInAppMessageStatus
import proton.android.pass.domain.inappmessages.InAppMessageId
import proton.android.pass.domain.inappmessages.InAppMessageStatus
import proton.android.pass.preferences.InternalSettingsRepository
import proton.android.pass.preferences.LastTimeUserHasSeenIAMPreference
import javax.inject.Inject

class ChangeInAppMessageStatusImpl @Inject constructor(
    private val inAppMessagesRepository: InAppMessagesRepository,
    private val internalSettingsRepository: InternalSettingsRepository,
    private val clock: Clock
) : ChangeInAppMessageStatus {

    override suspend fun invoke(
        userId: UserId,
        messageId: InAppMessageId,
        status: InAppMessageStatus
    ) {
        inAppMessagesRepository.changeMessageStatus(userId, messageId, status)
        internalSettingsRepository.setLastTimeUserHasSeenIAM(
            LastTimeUserHasSeenIAMPreference(userId, clock.now().epochSeconds)
        )
    }
}
