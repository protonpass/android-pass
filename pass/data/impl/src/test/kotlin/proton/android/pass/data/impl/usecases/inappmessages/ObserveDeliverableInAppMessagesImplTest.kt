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

package proton.android.pass.data.impl.usecases.inappmessages

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import me.proton.core.domain.entity.UserId
import org.junit.Before
import proton.android.pass.data.fakes.repositories.FakeInAppMessagesRepository
import proton.android.pass.data.fakes.usecases.TestObserveCurrentUser
import proton.android.pass.domain.inappmessages.InAppMessageStatus
import proton.android.pass.preferences.LastTimeUserHasSeenIAMPreference
import proton.android.pass.preferences.TestInternalSettingsRepository
import proton.android.pass.test.domain.TestInAppMessage
import proton.android.pass.test.domain.TestUser
import kotlin.test.Test
import kotlin.time.Duration.Companion.minutes

internal class ObserveDeliverableInAppMessagesImplTest {

    private lateinit var instance: ObserveDeliverableInAppMessagesImpl
    private lateinit var observeCurrentUser: TestObserveCurrentUser
    private lateinit var inAppMessagesRepository: FakeInAppMessagesRepository
    private lateinit var internalSettingsRepository: TestInternalSettingsRepository
    private lateinit var clock: Clock
    private lateinit var userId: UserId

    @Before
    fun setup() {
        userId = UserId("test-user")
        observeCurrentUser = TestObserveCurrentUser().apply {
            sendUser(TestUser.create())
        }
        inAppMessagesRepository = FakeInAppMessagesRepository()
        internalSettingsRepository = TestInternalSettingsRepository()
        clock = Clock.System
        instance = ObserveDeliverableInAppMessagesImpl(
            observeCurrentUser = observeCurrentUser,
            inAppMessagesRepository = inAppMessagesRepository,
            internalSettingsRepository = internalSettingsRepository,
            clock = clock
        )
    }


    @Test
    fun `test no unread messages returns empty list`() = runTest {
        instance(userId).test {
            awaitItem().isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `test messages are not returned if last time is less than 30 minutes`() = runTest {
        val now = clock.now()
        internalSettingsRepository.setLastTimeUserHasSeenIAM(
            LastTimeUserHasSeenIAMPreference(userId, now.minus(29.minutes).epochSeconds)
        )
        val message = TestInAppMessage.create(
            state = InAppMessageStatus.Unread,
            range = TestInAppMessage.createInAppMessageRange()
        )
        inAppMessagesRepository.addMessage(userId, message)
        instance(userId).test {
            awaitItem().isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `test messages are returned if last time is more than 30 minutes`() = runTest {
        val now = clock.now()
        internalSettingsRepository.setLastTimeUserHasSeenIAM(
            LastTimeUserHasSeenIAMPreference(userId, now.minus(31.minutes).epochSeconds)
        )
        val message = TestInAppMessage.create(
            state = InAppMessageStatus.Unread,
            range = TestInAppMessage.createInAppMessageRange()
        )
        inAppMessagesRepository.addMessage(userId, message)
        instance(userId).test {
            val item = awaitItem()
            assertThat(item).hasSize(1)
            assertThat(item.first()).isEqualTo(message)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
