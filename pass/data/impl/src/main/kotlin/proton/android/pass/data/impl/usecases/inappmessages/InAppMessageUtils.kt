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
import kotlinx.datetime.Clock
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.android.pass.preferences.InternalSettingsRepository
import kotlin.time.Duration.Companion.minutes

object InAppMessageUtils {

    fun <T> observeDeliverableMessages(
        userId: UserId?,
        observeCurrentUser: ObserveCurrentUser,
        internalSettingsRepository: InternalSettingsRepository,
        clock: Clock,
        getMessage: (UserId, Long) -> Flow<T?>
    ): Flow<T?> = getUserId(userId, observeCurrentUser).flatMapLatest { resolvedUserId ->
        internalSettingsRepository.getLastTimeUserHasSeenIAM(resolvedUserId)
            .flatMapLatest { preference ->
                val now = clock.now()
                val lastSeenTime = preference.value()?.timestamp ?: 0L
                val thirtyMinutesAgo = now.minus(30.minutes)
                val shouldShowInAppMessage = lastSeenTime < thirtyMinutesAgo.epochSeconds
                if (shouldShowInAppMessage) {
                    getMessage(resolvedUserId, now.epochSeconds)
                } else {
                    flowOf(null)
                }
            }
    }

    fun getUserId(userId: UserId?, observeCurrentUser: ObserveCurrentUser): Flow<UserId> = if (userId != null) {
        flowOf(userId)
    } else {
        observeCurrentUser().map { it.userId }
    }
}
