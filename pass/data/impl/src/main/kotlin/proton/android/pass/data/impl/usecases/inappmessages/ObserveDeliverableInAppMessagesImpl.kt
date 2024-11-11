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
import proton.android.pass.data.api.repositories.InAppMessagesRepository
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.android.pass.data.api.usecases.inappmessages.ObserveDeliverableInAppMessages
import proton.android.pass.domain.inappmessages.InAppMessage
import proton.android.pass.domain.inappmessages.InAppMessageStatus
import proton.android.pass.preferences.FeatureFlag
import proton.android.pass.preferences.FeatureFlagsPreferencesRepository
import proton.android.pass.preferences.InternalSettingsRepository
import javax.inject.Inject
import kotlin.time.Duration.Companion.minutes

class ObserveDeliverableInAppMessagesImpl @Inject constructor(
    private val observeCurrentUser: ObserveCurrentUser,
    private val inAppMessagesRepository: InAppMessagesRepository,
    private val featureFlagsPreferencesRepository: FeatureFlagsPreferencesRepository,
    private val internalSettingsRepository: InternalSettingsRepository,
    private val clock: Clock
) : ObserveDeliverableInAppMessages {

    override fun invoke(userId: UserId?): Flow<List<InAppMessage>> =
        featureFlagsPreferencesRepository.get<Boolean>(FeatureFlag.IN_APP_MESSAGES_V1)
            .flatMapLatest { isFeatureActive ->
                if (isFeatureActive) {
                    getUserId(userId).flatMapLatest { resolvedUserId ->
                        internalSettingsRepository.getLastTimeUserHasSeenIAM(resolvedUserId)
                            .flatMapLatest { preference ->
                                val now = clock.now()
                                val lastSeenTime = preference.value()?.timestamp ?: 0L
                                val thirtyMinutesInMillis = 30.minutes.inWholeMilliseconds
                                if (now.toEpochMilliseconds() - lastSeenTime > thirtyMinutesInMillis) {
                                    inAppMessagesRepository.observeUserMessages(resolvedUserId)
                                        .map { list ->
                                            list.filter { message ->
                                                message.state == InAppMessageStatus.Unread &&
                                                    message.range.inRange(now)
                                            }
                                        }
                                } else {
                                    flowOf(emptyList())
                                }
                            }
                    }
                } else {
                    flowOf(emptyList())
                }
            }

    fun getUserId(userId: UserId?): Flow<UserId> = if (userId != null) {
        flowOf(userId)
    } else {
        observeCurrentUser().map { it.userId }
    }
}
